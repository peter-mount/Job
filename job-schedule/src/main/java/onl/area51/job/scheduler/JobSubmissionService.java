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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;
import static onl.area51.job.cluster.Constants.CLUSTER_NAME;
import onl.area51.job.jcl.Jcl;
import onl.area51.job.jcl.JclScript;
import uk.trainwatch.job.ext.ExtensionManager;
import uk.trainwatch.rabbitmq.Rabbit;
import uk.trainwatch.rabbitmq.RabbitMQ;
import uk.trainwatch.util.config.Configuration;
import uk.trainwatch.util.config.impl.GlobalConfiguration;
import uk.trainwatch.util.sql.Database;
import uk.trainwatch.util.sql.SQL;

/**
 * This service receives messages on a global queue, parses the JCL and submits it to the database.
 *
 * @author peter
 */
@ApplicationScoped
public class JobSubmissionService
{

    private static final String QUEUE_NAME = "job.executor";
    private static final String ROUTING_KEY = QUEUE_NAME;

    private static final Logger LOG = Logger.getLogger( JobSubmissionService.class.getName() );

    @Inject
    private Rabbit rabbit;

    @Database("job")
    @Inject
    private DataSource dataSource;
    
    @Inject
    @GlobalConfiguration( "jobcluster" )
    private Configuration configuration;

    private String getClusterName()
    {
        String clusterName = System.getenv( "CLUSTERNAME" );
        if( clusterName == null )
        {
            clusterName = configuration.getString( "clustername" );
        }
        return clusterName;
    }

    @PostConstruct
    void start()
    {

        // We must ensure extensions have been initialised
        ExtensionManager.INSTANCE.init();

        // clusterName is global to the VM
        String clusterName = getClusterName();
        LOG.log( Level.INFO, () -> "Initialising Job Cluster " + clusterName );
        Objects.requireNonNull( clusterName, "No cluster name defined. Set with " + CLUSTER_NAME + " system property." );

        // Create the required number of client threads
        Map<String, Object> props = new HashMap<>();
        // Don't append host name to queue - we want to share the cluster queue across nodes
        props.put( RabbitMQ.NO_HOSTNAME, true );

        rabbit.queueDurableConsumer( QUEUE_NAME, ROUTING_KEY, props, RabbitMQ.toString, this::submit );
    }

    private void submit( String script )
    {
        try {
            JclScript jclScript = JclScript.read( script );
            Jcl jcl = jclScript.getJcl();

            LOG.log( Level.INFO, () -> jcl.getType() + " " + jcl.getNode() + "." + jcl.getName() );
            
            switch( jcl.getType() ) {
                case EXECUTABLE:
                case SCHEDULABLE:
                    try( Connection con = dataSource.getConnection();
                         PreparedStatement ps = con.prepareStatement( "SELECT storejob(?::NAME,?::NAME,?::TEXT,?::XML)" ) ) {
                        SQL.executeFunction( ps, jcl.getNode(), jcl.getName(), script, jcl.getSchedule() );
                    }
                    catch( SQLException ex ) {
                        LOG.log( Level.SEVERE, ex, () -> "Failed to persist job " + jcl.getNode() + "." + jcl.getName() );
                    }
                    break;

                case SUBROUTINE:
                    try( Connection con = dataSource.getConnection();
                         PreparedStatement ps = con.prepareStatement( "SELECT storejob(?::NAME,?::NAME,?::TEXT,'<s/>'::XML)" ) ) {
                        SQL.executeFunction( ps, jcl.getNode(), jcl.getName(), script );
                    }
                    catch( SQLException ex ) {
                        LOG.log( Level.SEVERE, ex, () -> "Failed to persist job " + jcl.getNode() + "." + jcl.getName() );
                    }
                    break;

                case DELETE:
                    try( Connection con = dataSource.getConnection();
                         PreparedStatement ps = con.prepareStatement( "SELECT deletejob(?::NAME,?::NAME)" ) ) {
                        SQL.executeFunction( ps, jcl.getNode(), jcl.getName() );
                    }
                    catch( SQLException ex ) {
                        LOG.log( Level.SEVERE, ex, () -> "Failed to delete job " + jcl.getNode() + "." + jcl.getName() );
                    }
                    break;

                default:
                    LOG.log( Level.WARNING, () -> "Unsupported type " + jcl.getType() );
                    break;
            }
        }
        catch( IOException ex ) {
            LOG.log( Level.SEVERE, ex, () -> "Failed to parse JCL" );
        }

    }
}
