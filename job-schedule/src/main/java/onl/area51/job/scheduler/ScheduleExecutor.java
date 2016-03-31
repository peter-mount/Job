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
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;
import static onl.area51.job.cluster.Constants.EXCEPTION;
import onl.area51.job.cluster.JobCluster;
import uk.trainwatch.scheduler.Cron;
import uk.trainwatch.util.Consumers;
import uk.trainwatch.util.config.Database;
import uk.trainwatch.util.sql.SQL;
import uk.trainwatch.util.sql.UncheckedSQLException;

/**
 * This class manages the execution of jobs as they become available by the database.
 * <p>
 * It operates by calling the database once per minute to get the next available jobs and submitting them.
 * <p>
 * When the job's complete, they are then marked as success or fail.
 *
 * @author peter
 */
@ApplicationScoped
public class ScheduleExecutor
{

    private static final Logger LOG = Logger.getLogger( ScheduleExecutor.class.getName() );

    @Database("job")
    @Inject
    private DataSource dataSource;

    @Inject
    private JobCluster jobCluster;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @PreDestroy
    void stop()
    {
        executorService.shutdownNow();
    }

    /**
     * Run any ready jobs.
     * <p>
     * This method is invoked once every minute. We offset at 10s as it tends to be busy at the top of each minute.
     *
     * @throws SQLException
     */
    @Cron("10 * * * * ?")
    public void executeReadySchedules()
    {
        try( Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement( "SELECT * FROM nextjobs()" ) ) {
            SQL.stream( ps, JobRequest::new )
                    .forEach( Consumers.executeWith( executorService, this::runSchedule ) );
        }
        catch( SQLException ex ) {
            LOG.log( Level.SEVERE, null, ex );
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
    private void runSchedule( JobRequest s )
    {
        try {
            try {
                LOG.log( Level.INFO, () -> "Executing job " + s.getNode() + "." + s.getName() + " timeout " + s.getDuration() );

                jobCluster.execute( s.getNode().toLowerCase(),
                                    s.getName().toLowerCase(),
                                    new HashMap<>(),
                                    s.getTimeout(), TimeUnit.SECONDS,
                                    ret -> setStatus( s.getJobid(), ret == null ? null : (String) ret.get( EXCEPTION ) )
                );
            }
            catch( TimeoutException ex ) {
                // If this doesn't run then 1 minute later the database will time out the job.
                LOG.log( Level.WARNING, () -> "Job timeout " + s.getNode() + "." + s.getName() );
                setStatus( s.getJobid(), "Timed out" );
            }
            catch( IOException ex ) {
                // Note: Objects.toString() as if message() is null we need a string not null here
                LOG.log( Level.SEVERE, ex, () -> "Job failure " + s.getNode() + "." + s.getName() );
                setStatus( s.getJobid(), Objects.toString( ex.getMessage() ) );
            }
        }
        catch( UncheckedSQLException ex ) {
            LOG.log( Level.SEVERE, null, ex );
        }
    }

    private void setStatus( long jobid, String cause )
    {
        try( Connection con = dataSource.getConnection() ) {
            if( cause == null ) {
                SQL.executeFunction( con, "jobsuccess(?)", jobid );
            }
            else {
                SQL.executeFunction( con, "jobfail(?,?)", jobid, cause );
            }
        }
        catch( SQLException ex ) {
            throw new UncheckedSQLException( ex );
        }
    }
}
