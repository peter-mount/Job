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
import uk.trainwatch.util.sql.SQL;

/**
 * A Job Request is the entry returned by nextJobs() in the database
 *
 * @author peter
 */
public class JobRequest
{

    private final long jobid;
    private final String node;
    private final String name;
    private final Duration duration;
    private final long timeout;

    JobRequest( ResultSet rs )
            throws SQLException
    {
        jobid = rs.getLong( "jid" );
        node = rs.getString( "node" );
        name = rs.getString( "name" );

        duration = SQL.getDuration( rs, "timeout" );
        timeout = duration.getSeconds();
    }

    public long getJobid()
    {
        return jobid;
    }

    public String getNode()
    {
        return node;
    }

    public String getName()
    {
        return name;
    }

    public Duration getDuration()
    {
        return duration;
    }

    public long getTimeout()
    {
        return timeout;
    }

    @Override
    public int hashCode()
    {
        return (int) (this.jobid ^ (this.jobid >>> 32));
    }

    @Override
    public boolean equals( Object obj )
    {
        if( this == obj ) {
            return true;
        }
        if( obj == null || getClass() != obj.getClass() ) {
            return false;
        }
        final JobRequest other = (JobRequest) obj;
        return this.jobid == other.jobid;
    }

}
