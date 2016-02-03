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
package onl.area51.job.scheduler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import onl.area51.job.cluster.ClusterJobRetriever;
import onl.area51.job.cluster.JobCluster;
import onl.area51.job.jcl.Jcl;
import onl.area51.job.jcl.JclScript;
import uk.trainwatch.job.ext.ExtensionManager;
import uk.trainwatch.rabbitmq.Rabbit;
import uk.trainwatch.rabbitmq.RabbitMQ;

/**
 * For most purposes the JobScheduler accepts raw job scripts.
 * <p>
 * These scripts must have Jcl present, which it uses to determine what to do with
 * them.
 * <p>
 * First it will persist a job based on it's node/name using the defined persistence engine, usually a database.
 * <p>
 * Next if it's a plain job it then submits it directly to the queue.
 * <p>
 * If it has a schedule, then it schedules it instead.
 *
 * @author peter
 */
@ApplicationScoped
public class JobScheduler
{

    private static final Logger LOG = Logger.getLogger( JobScheduler.class.getName() );

    @Inject
    private Rabbit rabbit;

    @Inject
    private ClusterJobRetriever jobRetriever;

    @Inject
    private JobCluster jobCluster;

    @PostConstruct
    void start()
    {
        ExtensionManager.INSTANCE.init();;

        rabbit.queueDurableConsumer( "job.schedule", "job.schedule", RabbitMQ.toString, this::schedule );
    }

    private void schedule( String src )
    {
        try {
            JclScript<String> jclScript = JclScript.read( src );
            Jcl jcl = jclScript.getJcl();
            if( jcl.isValid() ) {
                // Store the job
                jobRetriever.storeJob( jcl, src );

                switch( jcl.getType() ) {
                    case EXECUTABLE:
                        runNow( jclScript );
                        break;

                    case SCHEDULABLE:
                        schedule( jclScript );
                        break;

                    default:
                        // Do nothing for this type
                        break;
                }
            }
            else {
                LOG.log( Level.WARNING, "Invalid Jcl provided" );
            }
        }
        catch( IOException ex ) {
            LOG.log( Level.SEVERE, null, ex );
        }
    }

    private void schedule( JclScript<String> jclScript )
    {
        Jcl jcl = jclScript.getJcl();
        LOG.log( Level.INFO, () -> "Received Job " + jcl.getNode() + ":" + jcl.getName() );
    }

    private void runNow( JclScript<String> jclScript )
            throws IOException
    {
        Jcl jcl = jclScript.getJcl();
        LOG.log( Level.INFO, () -> "Submitting Job " + jcl.getNode() + ":" + jcl.getName() );
        jobCluster.execute( jcl.getNode(), jcl.getName(), null );
    }

}
