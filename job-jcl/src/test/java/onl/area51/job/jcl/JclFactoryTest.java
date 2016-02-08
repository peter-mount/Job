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
package onl.area51.job.jcl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * tests JclFactory
 *
 * @author peter
 */
public class JclFactoryTest
{

    private static final String JOB = "## job Node.Name\n";
    // subjob can have whitespace between sub & job
    private static final String SUBJOB[] = {
        "## subjob Node.Name\n",
        "## sub job Node.Name\n",
        "## sub  job Node.Name\n",
        "## sub\tjob Node.Name\n",
        "## sub\t job Node.Name\n",
        "## sub \tjob Node.Name\n"
    };
    // deletejob can have whitespace between delete & job
    private static final String DELETEJOB[] = {
        "## deletejob Node.Name\n",
        "## delete job Node.Name\n",
        "## delete  job Node.Name\n",
        "## delete\tjob Node.Name\n",
        "## delete \tjob Node.Name\n",
        "## delete\t job Node.Name\n"
    };

    /**
     * Test that a Jcl actually gets populated. This is the bare minimum Jcl
     */
    @Test
    public void job()
    {
        Jcl jcl = JclFactory.compileJcl( JOB );
        test( jcl );
        assertSame( JclType.EXECUTABLE, jcl.getType() );
    }

    @Test
    public void subjob()
    {
        for( String job: SUBJOB ) {
            Jcl jcl = JclFactory.compileJcl( job );
            test( jcl );
            assertSame( job, JclType.SUBROUTINE, jcl.getType() );
        }
    }

    @Test
    public void deletejob()
    {
        for( String job: DELETEJOB ) {
            Jcl jcl = JclFactory.compileJcl( job );
            test( jcl );
            assertSame( job, JclType.DELETE, jcl.getType() );
        }
    }

    private void test( Jcl jcl )
    {
        assertNotNull( jcl );
        assertEquals( "Node", "node", jcl.getNode() );
        assertEquals( "Name", "name", jcl.getName() );
    }

    /**
     * Test we schedule to run at a specific date/time
     */
    @Test
    public void runAt()
    {
        Jcl jcl = JclFactory.compileJcl( JOB
                                         + "## run at 2016-2-10 18:24\n"
                                         + "## run at 2016/2/10 18:24\n"
                                         + "## run at 2016-2-10\n"
                                         + "## run at 18:24\n"
                                         // retry on failure after 1 hour
                                         + "## run at 18:24 retry 1 hour\n"
                                         + "## run at 18:24 retry 1 hour maximum 3\n"
                                         + "## run at 2016/2/10 18:24 retry every 1 hour\n"
                                         + "## run at 2016/2/10 18:24 retry every 1 hour maximum 3 times\n"
        );
        test( jcl );

        System.out.println( "runat\n"+jcl.getSchedule().replace( "><", ">\n<" ) );
        
        assertSame( JclType.SCHEDULABLE, jcl.getType() );

        String today = LocalDate.now().toString();
        String now = LocalTime.now().truncatedTo( ChronoUnit.MINUTES ).toString();

        assertEquals( "Schedule",
                      "<schedule>"
                      + "<once at=\"2016-02-10 18:24\"/>"
                      + "<once at=\"2016-02-10 18:24\"/>"
                      + "<once at=\"2016-02-10 " + now + "\"/>"
                      + "<once at=\"" + today + " 18:24\"/>"
                      // retry on failure after 1 hour
                      + "<once at=\"" + today + " 18:24\" retry=\"1 hour\"/>"
                      + "<once at=\"" + today + " 18:24\" retry=\"1 hour\" max=\"3\"/>"
                      + "<once at=\"2016-02-10 18:24\" retry=\"1 hour\"/>"
                      + "<once at=\"2016-02-10 18:24\" retry=\"1 hour\" max=\"3\"/>"
                      + "</schedule>",
                      jcl.getSchedule() );
    }

    @Test
    public void runEvery()
    {
        Jcl jcl = JclFactory.compileJcl( JOB
                                         + "## run every minute\n"
                                         + "## run every 10 minutes\n"
                                         + "## run every hour\n"
                                         + "## run every 3 hours\n"
                                         + "## run every day\n"
                                         + "## run every 2 days\n"
                                         + "## run every 2 day\n"
                                         // retry on failure after 1 hour
                                         + "## run every day retry 1 hour\n"
                                         + "## run every day retry 1 hour maximum 3 times\n"
                                         + "## run every day retry every 1 hour\n"
                                         + "## run every day retry every 1 hour maximum 3\n"
                                         // Between
                                         + "## run every hour between 00:00 and 06:00\n"
                                         + "## run every hour between 00:00 and 06:00 retry 10 minutes maximum 3 times\n"
                                         + "## run every hour between 21:00 and 03:00\n"
                                         + "## run every hour between 21:00 and 03:00 retry 10 minutes maximum 3 times\n"
        );
        test( jcl );

        System.out.println( jcl.getSchedule().replace( "><", ">\n<" ) );
        
        assertSame( JclType.SCHEDULABLE, jcl.getType() );

        LocalDateTime dt = LocalDateTime.now().truncatedTo( ChronoUnit.MINUTES );
        String now = dt.toLocalDate().toString() + " " + dt.toLocalTime().toString();

        assertEquals( "Schedule",
                      "<schedule>"
                      + "<repeat next=\"" + now + "\" step=\"1 minute\"/>"
                      + "<repeat next=\"" + now + "\" step=\"10 minute\"/>"
                      + "<repeat next=\"" + now + "\" step=\"1 hour\"/>"
                      + "<repeat next=\"" + now + "\" step=\"3 hour\"/>"
                      + "<repeat next=\"" + now + "\" step=\"1 day\"/>"
                      + "<repeat next=\"" + now + "\" step=\"2 day\"/>"
                      + "<repeat next=\"" + now + "\" step=\"2 day\"/>"
                      // retry on failure after 1 hour
                      + "<repeat next=\"" + now + "\" step=\"1 day\" retry=\"1 hour\"/>"
                      + "<repeat next=\"" + now + "\" step=\"1 day\" retry=\"1 hour\" max=\"3\"/>"
                      // same but with every keyword
                      + "<repeat next=\"" + now + "\" step=\"1 day\" retry=\"1 hour\"/>"
                      + "<repeat next=\"" + now + "\" step=\"1 day\" retry=\"1 hour\" max=\"3\"/>"
                      // Between
                      + "<repeat next=\"" + now + "\" step=\"1 hour\" between=\"00:00-06:00\"/>"
                      + "<repeat next=\"" + now + "\" step=\"1 hour\" retry=\"10 minute\" max=\"3\" between=\"00:00-06:00\"/>"
                      // Between 2100-0300 crossing midnight
                      + "<repeat next=\"" + now + "\" step=\"1 hour\" between=\"21:00-03:00\"/>"
                      // Between 2100-0300 crossing midnight retry 10 minutes max 3
                      + "<repeat next=\"" + now + "\" step=\"1 hour\" retry=\"10 minute\" max=\"3\" between=\"21:00-03:00\"/>"
                      + "</schedule>",
                      jcl.getSchedule() );
    }

    @Test
    public void timeout()
    {
        Jcl jcl = JclFactory.compileJcl( JOB
                                         + "## run at 2016-2-10 18:24 timeout 1 minute\n"
                                         + "## run at 2016/2/10 18:24 timeout 1 hour\n"
                                         + "## run every minute timeout minute\n"
                                         + "## run every 10 minutes timeout 5 minutes\n"
        );
        test( jcl );

        System.out.println( jcl.getSchedule().replace( "><", ">\n<" ) );
        
        assertSame( JclType.SCHEDULABLE, jcl.getType() );

        LocalDateTime dt = LocalDateTime.now().truncatedTo( ChronoUnit.MINUTES );
        String now = dt.toLocalDate().toString() + " " + dt.toLocalTime().toString();

        assertEquals( "Schedule",
                      "<schedule>"
                      + "<once at=\"2016-02-10 18:24\" timeout=\"1 minute\"/>"
                      + "<once at=\"2016-02-10 18:24\" timeout=\"1 hour\"/>"
                      + "<repeat next=\"" + now + "\" step=\"1 minute\" timeout=\"1 minute\"/>"
                      + "<repeat next=\"" + now + "\" step=\"10 minute\" timeout=\"5 minute\"/>"
                      + "</schedule>",
                      jcl.getSchedule() );
    }
}
