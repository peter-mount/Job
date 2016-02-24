/*
 * Copyright 2016 peter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package onl.area51.job.cluster;

import com.rabbitmq.client.LongString;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import static onl.area51.job.cluster.Constants.*;
import onl.area51.job.jcl.Jcl;
import onl.area51.job.jcl.JclScript;
import uk.trainwatch.job.lang.Compiler;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.Scope;
import uk.trainwatch.util.MapBuilder;

/**
 * Handles the execution of queued jobs.
 * <p>
 * This will sit on a queue and submit job entries to a local script engine. Any responses, if any have been requested will be sent back to the sender.
 * <p>
 * <h2>Inbound message format</h2>
 * The inbound message is in JSON and consists of a JsonObject:
 * <table>
 * <tr><th>Key</th><th>Value</th></tr>
 * <tr><td>cluster</td><td>The cluster name to run on</td></tr>
 * <tr><td>job</td><td>The job to execute</td></tr>
 * <tr><td>replyTo</td><td>If not null then the queue to send a reply to</td></tr>
 * <tr><td>corrId</td><td>If not null then the correlationId to set in any reply</td></tr>
 * <tr><td>args</td><td>JsonObject that contains any arguments. These will become global variables to the invoked script</td></tr>
 * </table>
 * <p>
 * <h2>Response</h2>
 * If replyTo is defined then a response will be sent back as a JsonObject:
 * <table>
 * <tr><th>Key</th><th>Value</th></tr>
 * <tr><td>response</td><td>A JsonObject containing the global variables of the job</td></tr>
 * <tr><td>exception</td><td>If not null then the exception message issued by the job.</td></tr>
 * </table>
 *
 * @author peter
 */
public class ClusterExecutor
        implements UnaryOperator<Map<String, Object>>
{

    private static final Logger LOG = Logger.getLogger( ClusterExecutor.class.getName() );
    private final String node;
    private final ClusterJobRetriever jobRetriever;

    /**
     *
     * @param node         Cluster name to listen under
     * @param jobRetriever function to retrieve a {@link Job} from it's cluster & job names
     */
    public ClusterExecutor( String node, ClusterJobRetriever jobRetriever )
    {
        this.node = node;
        this.jobRetriever = jobRetriever;
    }

    @Override
    public Map<String, Object> apply( Map<String, Object> request )
    {
        try {
            String requestedCluster = Objects.toString( request.get( CLUSTER ), null );
            if( node.equals( requestedCluster ) ) {
                return execute( request );
            }
            else {
                LOG.log( Level.WARNING, () -> "Received job for wrong cluster " + requestedCluster + " was expecting " + this.node );
            }
        }
        catch( Throwable ex ) {
            LOG.log( Level.SEVERE, "Fatal error unmarshalling job request", ex );
        }
        return null;
    }

    private Map<String, Object> execute( Map<String, Object> request )
    {
        String jobName = Objects.toString( request.get( JOB ), null );

        try {
            String src = jobRetriever.retrieveJob( Jcl.create( node, jobName ) );
            if( src == null ) {
                LOG.log( Level.WARNING, () -> "Cannot find Job " + node + ":" + jobName );
                return null;
            }

            JclScript<Job> jclScript = JclScript.read( src, Compiler::compile );
            Job job = jclScript.getScript();

            // globalScope contains var's in declare section. These will be returned if needed
            try( Scope.GlobalScope globalScope = (Scope.GlobalScope) Scope.newInstance( LOG ) ) {
                globalScope.setJob( job );
                globalScope.setJcl( jclScript.getJcl() );

                try {
                    LOG.log( Level.INFO, () -> "Executing Job " + node + ":" + jobName );
                    run( (Map<String, Object>) request.get( ARGS ), job, globalScope );
                    return globalScope;
                }
                catch( Throwable t ) {
                    LOG.log( Level.SEVERE, t, () -> "Exception in job " + node + ":" + jobName );
                    globalScope.put( EXCEPTION, t.toString() );
                    return globalScope;
                }
            }
        }
        catch( Exception ex ) {
            LOG.log( Level.SEVERE, ex, () -> "Failed to execute Job " + node + ":" + jobName );
            return MapBuilder.<String, Object>builder()
                    .add( EXCEPTION, ex.toString() )
                    .build();
        }
    }

    private void run( Map<String, Object> args, Job job, Scope outerScope )
            throws Exception
    {
        try( Scope scope = outerScope.begin() ) {
            if( args != null ) {
                args.forEach( ( k, v ) -> {
                    if( v instanceof LongString ) {
                        scope.setVar( k, v.toString() );
                    }
                    else {
                        scope.setVar( k, v );
                    }
                } );
            }
            job.invokeStatement( scope );
        }
    }
}
