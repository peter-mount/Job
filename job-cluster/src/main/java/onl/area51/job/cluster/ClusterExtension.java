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

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.kohsuke.MetaInfServices;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.ext.Extension;
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.expr.ExpressionOperation;
import uk.trainwatch.util.CDIUtils;

/**
 * Extends the job language by providing RPC calls.
 * <p>
 * execute() statements will invoke jobs using the usual queues as they start fresh jobs with no return. This means they will be throttle limited by the
 * number of consumers on the main queue for the cluster.
 * <p>
 * call() and execute with a lambda function will use the job.sub queue's for the cluster as these are not limited on the number of invocations. If we don't do
 * this then we will deadlock the cluster quickly as soon as a job starts.
 *
 * @author peter
 */
@MetaInfServices(Extension.class)
public class ClusterExtension
        implements Extension
{

    private static final Logger LOG = Logger.getLogger( ClusterExtension.class.getName() );

    @Override
    public String getName()
    {
        return "Cluster";
    }

    @Override
    public String getVersion()
    {
        return "1.0";
    }

    @Inject
    private JobCluster cluster;

    @Override
    public synchronized void init()
            throws Exception
    {
        if( cluster == null ) {
            CDIUtils.inject( this );
        }
    }

    @Override
    public ExpressionOperation getExpression( String name, ExpressionOperation... args )
    {
        switch( args.length ) {
            case 2:
                switch( name ) {
                    // map = callCluster(cluster,name);
                    case "callCluster":
                        return ( s, a ) -> {
                            String clusterName = args[0].getString( s ).toLowerCase();
                            return cluster.call( clusterName, args[1].getString( s ), new HashMap<>(), 1, TimeUnit.MINUTES, "job.sub." + clusterName );
                        };

                    default:
                        break;
                }
                break;

            case 3:
                switch( name ) {
                    // execute don't wait for reply
                    // executeCluster(cluster,name,map);
                    case "callCluster":
                        return ( s, a ) -> {
                            String clusterName = args[0].getString( s );
                            return cluster.call( clusterName, args[1].getString( s ), args[2].get( s ), 1, TimeUnit.MINUTES, "job.sub." + clusterName );
                        };

                    default:
                        break;
                }
                break;

            default:
                break;
        }
        return null;
    }

    @Override
    public Statement getStatement( String name, ExpressionOperation... args )
    {
        switch( args.length ) {
            case 2:
                switch( name ) {
                    // execute don't wait for reply
                    // executeCluster(cluster,name);
                    case "executeCluster":
                        return ( s, a ) -> cluster.execute( args[0].getString( s ), args[1].getString( s ), new HashMap<>() );

                    default:
                        break;
                }
                break;

            case 3:
                switch( name ) {
                    // execute don't wait for reply
                    // executeCluster(cluster,name,map);
                    case "executeCluster":
                        return ( s, a ) -> cluster.execute( args[0].getString( s ), args[1].getString( s ), args[2].get( s ) );

                    default:
                        break;
                }
                break;

            case 4:
                switch( name ) {
                    // execute and invoke code on response
                    // callCluster( cluster, jobName, map, lambda )
                    case "callCluster":
                        return executeLambda( args[0], args[1], args[2], args[3] );

                    default:
                        break;
                }
                break;

            default:
                break;
        }
        return null;
    }

    /**
     * Execute a remote Job executing the supplied lambda function when it returns. The response will be in the lambda's local variable scope.
     *
     * @param clusterName
     * @param jobName
     * @param args
     * @param lambda
     *
     * @return
     */
    private Statement executeLambda( ExpressionOperation clusterName, ExpressionOperation jobName, ExpressionOperation args, ExpressionOperation lambda )
    {
        return ( scope, a ) -> {
            try {
                String cname = clusterName.getString( scope );
                cluster.execute( cname, jobName.getString( scope ), args.get( scope ),
                                 1, TimeUnit.MINUTES,
                                 resp -> {
                                     try( Scope lambdaScope = scope.begin() ) {
                                         resp.forEach( lambdaScope::setLocalVar );
                                         lambda.invoke( lambdaScope );
                                     }
                                     catch( Exception ex ) {
                                         throw new RuntimeException();
                                     }
                                 },
                                 "job.sub." + cname );
            }
            catch( RuntimeException ex ) {
                Throwable t = ex.getCause();
                throw t instanceof Exception ? (Exception) t : ex;
            }
        };
    }
}
