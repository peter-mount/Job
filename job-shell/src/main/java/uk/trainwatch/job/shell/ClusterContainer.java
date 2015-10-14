/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.shell;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.apache.commons.configuration.Configuration;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.lang.Compiler;
import uk.trainwatch.rabbitmq.Rabbit;
import uk.trainwatch.rabbitmq.RabbitMQ;
import uk.trainwatch.util.CDIUtils;
import uk.trainwatch.util.config.PrivateConfiguration;

/**
 * {@link Container} that will accept jobs from the cluster
 * <p>
 * @author peter
 */
public class ClusterContainer
        implements Container
{

    private static final Logger LOG = Logger.getLogger( ClusterContainer.class.getName() );
    private static final String QUEUE = "job.submit";
    private static final String ROUTING_KEY = "job.";
    private final String clusterName;

    @Inject
    private Rabbit rabbit;

    @Inject
    @PrivateConfiguration("cluster")
    private Configuration configuration;

    public ClusterContainer( String clusterName )
    {
        this.clusterName = clusterName;
    }

    @Override
    public void open()
    {
        // As not CDI managed, do injection
        CDIUtils.inject( this );

        // The number of consumers to run = the number of concurrent jobs this instance can run
        int concurrency = Math.max( 1, Math.min( 10, configuration.getInt( clusterName + ".concurrency", 1 ) ) );
        LOG.log( Level.INFO, () -> "Cluster " + clusterName + " concurrency=" + concurrency );

        for( int i = 0; i < concurrency; i++ ) {
            rabbit.queueDurableConsumer( QUEUE, ROUTING_KEY + clusterName, RabbitMQ.toString, this::run );
        }
    }

    private void run( String script )
    {
        try {
            Job job = Compiler.compile( new ANTLRInputStream( script ) );
            LOG.log( Level.INFO, () -> "Job: " + job.getId() );

            try( Scope scope = Scope.newInstance( LOG ) ) {
                job.invoke( scope );
            }

            LOG.log( Level.INFO, () -> "Job: " + job.getId() + " completed" );
        }
        catch( Exception ex ) {
            LOG.log( Level.SEVERE, null, ex );
        }
    }
}
