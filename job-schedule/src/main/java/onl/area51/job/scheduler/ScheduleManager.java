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
import uk.trainwatch.util.StateEngine;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;
import static onl.area51.job.cluster.Constants.EXCEPTION;
import onl.area51.job.cluster.JobCluster;
import uk.trainwatch.scheduler.Cron;
import uk.trainwatch.util.Consumers;
import uk.trainwatch.util.sql.Database;
import uk.trainwatch.util.sql.SQL;
import uk.trainwatch.util.sql.SQLConsumer;

/**
 *
 * @author peter
 */
@ApplicationScoped
public class ScheduleManager
{

    // The job timeout
    private static final int TIMEOUT = 3;
    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.HOURS;

    private static final Logger LOG = Logger.getLogger( ScheduleManager.class.getName() );

    private static final StateEngine RUN_STATE_ENGINE = StateEngine.builder()
            // Disabled state
            .add( "DISABLED" )
            .desc( "Disabled" )
            .initial()
            .terminal()
            .build()
            // ------------------------------
            // Run once job waiting to run
            .add( "SINGLE_WAITING" )
            .next( "SINGLE_RUNNING" )
            .desc( "Run Once" )
            .initial()
            .executable()
            .build()
            // Run once actually running
            .add( "SINGLE_RUNNING" )
            .next( "SINGLE_COMPLETED" )
            .fail( "SINGLE_FAILED" )
            .desc( "Running Once" )
            .build()
            // Run once completed
            .add( "SINGLE_COMPLETED" )
            .desc( "Completed" )
            .terminal()
            .build()
            // Run once failed
            .add( "SINGLE_FAILED" )
            .terminal()
            .desc( "Failed" )
            .build()
            // ------------------------------
            // Repeated jobs
            // Repeated job waiting initial run
            .add( "REPEAT_WAITING" )
            .next( "REPEAT_RUNNING" )
            .desc( "Repeat Waiting" )
            .initial()
            .executable()
            .build()
            // Repeated job running
            .add( "REPEAT_RUNNING" )
            .next( "REPEAT_COMPLETED" )
            .fail( "REPEAT_FAILED" )
            .desc( "Repeat Running" )
            .build()
            // Repeated job completed, waiting for next run
            .add( "REPEAT_COMPLETED" )
            .next( "REPEAT_RUNNING" )
            .desc( "Repeat Completed" )
            .executable()
            .build()
            // Repeated job failed, waiting for next run
            .add( "REPEAT_FAILED" )
            .next( "REPEAT_RUNNING" )
            .desc( "Repeat Failed" )
            .executable()
            .build()
            // ------------------------------
            .build();

    private static final String SELECT_SCHEDULE = "SELECT c.name as clusterName, j.name as jobName, s.id, s.descr, s.mode::NAME, s.lastrun, s.nextrun, s.status, s.step FROM schedule s";
    
    @Database("job")
    @Inject
    private DataSource dataSource;

    @Inject
    private JobCluster jobCluster;

    private Collection<String> clusters;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @PostConstruct
    public void reset()
            throws SQLException
    {
        try( Connection con = dataSource.getConnection() ) {

            // Refresh the cluster list
            try( PreparedStatement ps = con.prepareStatement( "SELECT name FROM cluster" ) ) {
                clusters = Collections.unmodifiableCollection( SQL.stream( ps, SQL.STRING_LOOKUP )
                        .sorted( String.CASE_INSENSITIVE_ORDER )
                        .collect( Collectors.toList() )
                );
            }

        }
    }

    /**
     * Collection of available clusters
     *
     * @return
     */
    public Collection<String> getClusters()
    {
        return clusters;
    }

    /**
     * Collection of all schedules ordered by job name
     *
     * @param cluster
     *
     * @return
     *
     * @throws SQLException
     */
    public Collection<Schedule> getSchedules( String cluster )
            throws SQLException
    {
        try( Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_SCHEDULE
                                                          + " INNER JOIN cluster c ON s.cluster=c.id"
                                                          + " INNER JOIN job j ON s.job=j.id"
                                                          + " WHERE c.name=?" ) ) {
            return SQL.stream( ps, rs -> new Schedule( rs, RUN_STATE_ENGINE::lookup ) )
                    .sorted( ( a, b ) -> String.CASE_INSENSITIVE_ORDER.compare( a.getJobName(), b.getJobName() ) )
                    .collect( Collectors.toList() );
        }
    }

    public Collection<Schedule> getScheduleRunOrder( String cluster )
            throws SQLException
    {
        try( Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_SCHEDULE
                                                          + " INNER JOIN cluster c ON s.cluster=c.id"
                                                          + " INNER JOIN job j ON s.job=j.id"
                                                          + " WHERE c.name=? AND mode IN ('SINGLE_WAITING','REPEAT_WAITING','REPEAT_COMPLETED','REPEAT_FAILED')" ) ) {
            return SQL.stream( ps, rs -> new Schedule( rs, RUN_STATE_ENGINE::lookup ) )
                    .sorted( ( a, b ) -> b.getNextRun().compareTo( a.getNextRun() ) )
                    .collect( Collectors.toList() );
        }
    }

    /**
     * Returns a collection of Schedules that are ready to run.
     * <p>
     * A schedule is ready to run if its a single-waiting job or a repeat job in waiting, completed or failed states and its nextRun time is in the past.
     *
     * @return
     *
     * @throws java.sql.SQLException
     */
    public Collection<Schedule> getReadySchedules()
            throws SQLException
    {
        try( Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_SCHEDULE
                                                          + " INNER JOIN cluster c ON s.cluster=c.id"
                                                          + " INNER JOIN job j ON s.job=j.id"
                                                          + " WHERE mode IN ('SINGLE_WAITING', 'REPEAT_WAITING', 'REPEAT_COMPLETED', 'REPEAT_FAILED')"
                                                          + " AND nextRun < now()" ) ) {
            return SQL.stream( ps, rs -> new Schedule( rs, RUN_STATE_ENGINE::lookup ) )
                    .collect( Collectors.toList() );
        }
    }

    /**
     * Run any ready jobs.
     * 
     * This method is invoked once every minute. We offset at 10s as it tends to be busy at the top of each minute.
     *
     * @throws SQLException
     */
    @Cron("10 * * * * ?")
    public void executeReadySchedules()
            throws SQLException
    {
        try( Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_SCHEDULE
                                                          + " INNER JOIN cluster c ON s.cluster=c.id"
                                                          + " INNER JOIN job j ON s.job=j.id"
                                                          + " WHERE mode IN ('SINGLE_WAITING', 'REPEAT_WAITING', 'REPEAT_COMPLETED', 'REPEAT_FAILED')"
                                                          + " AND nextRun < now()" ) ) {
            SQL.stream( ps, rs -> new Schedule( rs, RUN_STATE_ENGINE::lookup ) )
                    // Only permit executable jobs
                    .filter( s -> s.getMode().isExecutable() )
                    .forEach( Consumers.executeWith( executorService, this::runSchedule ) );
        }
    }

    /**
     * Run a schedule.
     * <p>
     * This will set it to the next state (should be a running state) as well as mark the next execution time.
     * It will then execute and wait for a response. If a response is returned then we move to the next state. On timeout or IOException we fail.
     *
     * @param s
     */
    private void runSchedule( Schedule s )
    {
        try {
            try {
                setStateAndRunTime( s, s.next() );
                jobCluster.execute( s.getCluster(), s.getJobName(), null, TIMEOUT, TIMEOUT_UNIT,
                                    SQLConsumer.guard( ret -> {
                                        if( ret!=null && ret.containsKey( EXCEPTION ) ) {
                                            setState( s, s.fail(), (String) ret.get( EXCEPTION ) );
                                        }
                                        else {
                                            setState( s, s.next(), "Successful" );
                                        }
                                    } ) );
            }
            catch( TimeoutException |
                   IOException ex ) {
                setState( s, s.fail(), ex.getMessage() );

            }
        }
        catch( SQLException ex ) {

            LOG.log( Level.SEVERE, null, ex );
        }

    }

    private void setState( Schedule s, StateEngine.State state, String text )
            throws SQLException
    {
        try( Connection con = dataSource.getConnection();
             PreparedStatement update = con.prepareStatement( "UPDATE schedule SET mode=?::schedulerunmode, status=? WHERE id = ?" ) ) {
            SQL.executeUpdate( update,
                               state.getName(),
                               Objects.toString( text, "" ),
                               s.getId() );
        }
    }

    private void setStateAndRunTime( Schedule s, StateEngine.State state )
            throws SQLException
    {
        try( Connection con = dataSource.getConnection();
             PreparedStatement update = con.prepareStatement( "UPDATE schedule SET lastRun=now(), nextRun=now()+step, mode=?::schedulerunmode, status='Running' WHERE id = ?" ) ) {
            SQL.executeUpdate( update,
                               state.getName(),
                               s.getId() );
        }
    }
}
