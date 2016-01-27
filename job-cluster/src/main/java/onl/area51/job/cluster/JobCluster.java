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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import static onl.area51.job.cluster.Constants.*;
import uk.trainwatch.rabbitmq.Rabbit;
import uk.trainwatch.rabbitmq.RabbitConnection;
import uk.trainwatch.rabbitmq.RabbitMQ;
import uk.trainwatch.rabbitmq.RabbitRPCClient;
import uk.trainwatch.rabbitmq.RabbitRPCInvoker;

/**
 * Handles the execution of jobs on a remote cluster
 *
 * @author peter
 */
@ApplicationScoped
public class JobCluster
{

    private static final Logger LOG = Logger.getLogger( JobCluster.class.getName() );

    @Inject
    private Rabbit rabbit;

    @Inject
    private ClusterJobRetriever jobRetriever;

    @PostConstruct
    void start()
    {
        // clusterName is global to the VM
        String clusterName = ClusterContextListener.getClusterName();
        LOG.log( Level.INFO, () -> "Initialising Job Cluster " + clusterName );
        Objects.requireNonNull( clusterName, "No cluster name defined. Set with " + CLUSTER_NAME + " system property." );

        String queueKey = QUEUE_NAME + clusterName;
        String routingKey = ROUTING_KEY_PREFIX + clusterName;

        // Create the required number of client threads
        RabbitConnection connection = rabbit.getConnection();
        Map<String, Object> props = new HashMap<>();
        // Don't append host name to queue - we want to share the cluster queue across nodes
        props.put( RabbitMQ.NO_HOSTNAME, true );
        int threads = ClusterContextListener.getThreadCount();
        LOG.log( Level.INFO, () -> "Initialising " + threads + " Job execution threads" );
        for( int t = 0; t < threads; t++ ) {
            RabbitMQ.queueConsumer( connection, queueKey, RabbitMQ.DEFAULT_TOPIC, routingKey, false, props,
                                    new RabbitRPCInvoker( connection, new ClusterExecutor( clusterName, jobRetriever ) )
            );
        }
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
     * Execute a job on a remote cluster and then wait for a response.
     *
     * @param cluster cluster name
     * @param job     job name
     * @param args    arguments
     *
     * @return returned arguments
     *
     * @throws TimeoutException    if no response after 1 minute.
     * @throws java.io.IOException
     */
    public Map<String, Object> call( String cluster, String job, Map<String, Object> args )
            throws TimeoutException,
                   IOException
    {
        return call( cluster, job, args, 1, TimeUnit.MINUTES );
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
        Map<String, Object> params = getParams( cluster, job, args );
        return RabbitRPCClient.mapCall( rabbit.getConnection(), "job.exec.test", (int) unit.toMillis( time ), params );
    }

    public void execute( String cluster, String job, Map<String, Object> args, Consumer<Map<String, Object>> action )
            throws TimeoutException,
                   IOException
    {
        execute( cluster, job, args, 1, TimeUnit.MINUTES, action );
    }

    public void execute( String cluster, String job, Map<String, Object> args, long time, TimeUnit unit, Consumer<Map<String, Object>> action )
            throws TimeoutException,
                   IOException
    {
        Map<String, Object> params = getParams( cluster, job, args );
        RabbitRPCClient.execute( rabbit.getConnection(), "job.exec.test", (int) unit.toMillis( time ), params,
                                 ret -> action.accept( (Map<String, Object>) ret.get( ARGS ) ) );
    }

    private Map<String, Object> getParams( String cluster, String job, Map<String, Object> args )
    {
        Objects.requireNonNull( cluster, "Cluster not defined" );
        Objects.requireNonNull( job, "Job not defined" );

        Map<String, Object> params = new HashMap<>();
        params.put( CLUSTER, cluster );
        params.put( JOB, job );
        params.put( ARGS, args == null ? Collections.emptyMap() : args );
        return params;
    }

}
