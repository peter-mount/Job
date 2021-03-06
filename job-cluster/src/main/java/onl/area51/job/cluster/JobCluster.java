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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import static onl.area51.job.cluster.Constants.*;
import onl.area51.kernel.CommandArguments;
import uk.trainwatch.job.ext.ExtensionManager;
import uk.trainwatch.rabbitmq.Rabbit;
import uk.trainwatch.rabbitmq.RabbitConnection;
import uk.trainwatch.rabbitmq.RabbitMQ;
import uk.trainwatch.rabbitmq.RabbitRPCClient;
import uk.trainwatch.rabbitmq.RabbitRPCInvoker;
import uk.trainwatch.util.Consumers;

/**
 * Handles the execution of jobs on a remote cluster
 *
 * @author peter
 */
@ApplicationScoped
public class JobCluster
{

    private static final Logger LOG = Logger.getLogger( JobCluster.class.getName() );
    private ExecutorService executorService;

    @Inject
    private Rabbit rabbit;

    @Inject
    private MinioService minioService;
    
    @Inject private ClusterMonitor clusterMonitor;

    private String getClusterName()
    {
        return System.getenv( "CLUSTERNAME" );
    }

    private int getThreads()
    {
        try {
            return Integer.parseInt( System.getenv( "CLUSTER_THREADS" ) );
        }
        catch( Exception ex ) {
            return 1;
        }
    }

    void deploy( @Observes CommandArguments args )
    {
        // Nothing to do here, it's presence ensures the bean is instantiated by CDI
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

        RabbitConnection connection = rabbit.getConnection();
        ClusterExecutor clusterExecutor = new ClusterExecutor( clusterName, this, clusterMonitor );
        RabbitRPCInvoker rpcInvoker = new RabbitRPCInvoker( connection, clusterExecutor );

        // Create the required number of client threads
        Map<String, Object> props = new HashMap<>();
        // Don't append host name to queue - we want to share the cluster queue across nodes
        props.put( RabbitMQ.NO_HOSTNAME, true );
        int threads = getThreads();
        LOG.log( Level.INFO, () -> "Initialising " + threads + " Job execution threads" );
        for( int t = 0; t < threads; t++ ) {
            RabbitMQ.queueConsumer( connection, "job." + clusterName + ".exec", RabbitMQ.DEFAULT_TOPIC, ROUTING_KEY_PREFIX + clusterName, false, props,
                                    rpcInvoker );
        }

        LOG.log( Level.INFO, () -> "Initialising Sub Job execution thread" );
        executorService = Executors.newWorkStealingPool();
        RabbitMQ.queueConsumer( connection, "job." + clusterName + ".sub", RabbitMQ.DEFAULT_TOPIC, SUBROUTING_KEY_PREFIX + clusterName, false, props,
                                Consumers.executeWith( executorService, rpcInvoker )
        );
    }

    MinioService getMinioService()
    {
        return minioService;
    }

    
    /**
     * Is the String a valid cluster or job name.
     * <p>
     * A name must not be null, not empty and only contain characters 0-9 a-z A-Z _ or -
     *
     * @param s String
     *
     * @return true if valid
     */
    public boolean isValidName( String s )
    {
        return s != null && !s.isEmpty() && !s.matches( "^[0-9a-zA-Z_-]+$" );
    }

    /**
     * Execute a job on a remote cluster, returning immediately.
     *
     * @param cluster Cluster name
     * @param job     Job name
     * @param args    Arguments
     *
     * @throws IOException if the job could not be submitted
     */
    public void execute( String cluster, String job, Map<String, Object> args )
            throws IOException
    {
        try {
            call( cluster, job, args, 0, TimeUnit.SECONDS );
        }
        catch( TimeoutException ex ) {
            // Ignore
        }
    }

    /**
     * Execute a job on a remote cluster and then wait for a response
     *
     * @param cluster cluster name
     * @param job     job name
     * @param args    arguments
     * @param time    time to wait for a response
     * @param unit    unit of time
     *
     * @return returned arguments
     *
     * @throws TimeoutException    if no response after 1 minute.
     * @throws java.io.IOException
     */
    public Map<String, Object> call( String cluster, String job, Map<String, Object> args, long time, TimeUnit unit )
            throws TimeoutException,
                   IOException
    {
        return call( cluster, job, args, time, unit, "job.exec." + cluster.toLowerCase() );
    }

    Map<String, Object> call( String cluster, String job, Map<String, Object> args, long time, TimeUnit unit, String key )
            throws TimeoutException,
                   IOException
    {
        Map<String, Object> params = getParams( cluster, job, args );
        return RabbitRPCClient.mapCall( rabbit.getConnection(), key, (int) unit.toMillis( time ), params );
    }

    public void execute( String cluster, String job, Map<String, Object> args, long time, TimeUnit unit,
                         Consumer<Map<String, Object>> action )
            throws TimeoutException,
                   IOException
    {
        execute( cluster, job, args, time, unit, action, "job.exec." + cluster.toLowerCase() );
    }

    void execute( String cluster, String job, Map<String, Object> args, long time, TimeUnit unit,
                  Consumer<Map<String, Object>> action, String key )
            throws TimeoutException,
                   IOException
    {
        LOG.log( Level.INFO, () -> "execute " + cluster + "." + job + ":" + key );
        Map<String, Object> params = getParams( cluster, job, args );
        RabbitRPCClient.execute( rabbit.getConnection(), key, (int) unit.toMillis( time ), params,
                                 ret
                                 -> {
                             Map<String, Object> retArgs = (Map<String, Object>) ret.get( ARGS );
                             action.accept( retArgs == null ? Collections.emptyMap() : retArgs );
                         } );
    }

    private Map<String, Object> getParams( String cluster, String job, Map<String, Object> args )
    {
        Objects.requireNonNull( cluster, "Cluster not defined" );
        Objects.requireNonNull( job, "Job not defined" );

        Map<String, Object> params = new HashMap<>();
        params.put( CLUSTER, cluster.toLowerCase() );
        params.put( JOB, job.toLowerCase() );
        params.put( ARGS, args == null ? Collections.emptyMap() : args );
        return params;
    }

}
