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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;
import uk.trainwatch.util.StateEngine;
import uk.trainwatch.util.sql.SQL;

/**
 *
 * @author peter
 */
public class Schedule
{

    private long id;

    private String cluster;
    private String jobName;
    private String description;
    private StateEngine.State mode;
    private LocalDateTime lastRun;
    private LocalDateTime nextRun;
    private Duration step;
    private String status;

    public Schedule()
    {
    }

    Schedule( ResultSet rs, Function<String, StateEngine.State> modeLookup )
            throws SQLException
    {
        id = rs.getLong( "id" );
        cluster = rs.getString( "clustername" );
        jobName = rs.getString( "jobname" );
        description = rs.getString( "descr" );
        mode = modeLookup.apply( rs.getString( "mode" ) );
        lastRun = SQL.getLocalDateTime( rs, "lastrun" );
        nextRun = SQL.getLocalDateTime( rs, "nextrun" );
        step = SQL.getDuration( rs, "step" );
        status = rs.getString( "status" );
    }

    public long getId()
    {
        return id;
    }

    /**
     * The Job cluster this job runs on
     *
     * @return
     */
    public String getCluster()
    {
        return cluster;
    }

    public void setCluster( String cluster )
    {
        this.cluster = cluster;
    }

    /**
     * The job name within it's cluster.
     *
     * @return
     */
    public String getJobName()
    {
        return jobName;
    }

    public void setJobName( String jobName )
    {
        this.jobName = jobName;
    }

    /**
     * Description of what this job does
     *
     * @return
     */
    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    /**
     * The schedule's run mode
     *
     * @return
     */
    public StateEngine.State getMode()
    {
        return mode;
    }

    public void setMode( StateEngine.State mode )
    {
        this.mode = mode;
    }

    public StateEngine.State next()
    {
        mode = mode.next();
        return mode;
    }

    public StateEngine.State fail()
    {
        mode = mode.fail();
        return mode;
    }

    /**
     * When this job last run
     *
     * @return
     */
    public LocalDateTime getLastRun()
    {
        return lastRun;
    }

    public void setLastRun( LocalDateTime lastRun )
    {
        this.lastRun = lastRun;
    }

    /**
     * When this job is next due to run (if not disabled)
     *
     * @return
     */
    public LocalDateTime getNextRun()
    {
        return nextRun;
    }

    public void setNextRun( LocalDateTime nextRun )
    {
        this.nextRun = nextRun;
    }

    /**
     * The step between job runs
     *
     * @return
     */
    public Duration getStep()
    {
        return step;
    }

    public void setStep( Duration step )
    {
        this.step = step;
    }

    public String getStatus()
    {
        return status;
    }

}
