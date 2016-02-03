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

    /**
     * Test that a Jcl actually gets populated. This is the bare minimum Jcl
     */
    @Test
    public void job()
    {
        test( JclFactory.compileJcl( JOB ) );
    }

    private void test( Jcl jcl )
    {
        assertNotNull( jcl );
        assertEquals( "Node", "Node", jcl.getNode() );
        assertEquals( "Name", "Name", jcl.getName() );
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
        );
        test( jcl );

        String today = LocalDate.now().toString();
        String now = LocalTime.now().truncatedTo( ChronoUnit.MINUTES ).toString();

        assertEquals( "Schedule",
                      "<schedule>"
                      + "<once at=\"2016-02-10 18:24\"/>"
                      + "<once at=\"2016-02-10 18:24\"/>"
                      + "<once at=\"2016-02-10 " + now + "\"/>"
                      + "<once at=\"" + today + " 18:24\"/>"
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
        );
        test( jcl );

        LocalDateTime dt = LocalDateTime.now().truncatedTo( ChronoUnit.MINUTES );
        String now = dt.toLocalDate().toString() + " " + dt.toLocalTime().toString();

        assertEquals( "Schedule",
                      "<schedule>"
                      + "<repeat next=\"" + now + "\" step=\"1 minute\"/>"
                                            + "<repeat next=\""+now+"\" step=\"10 minute\"/>"
                                            + "<repeat next=\""+now+"\" step=\"1 hour\"/>"
                                            + "<repeat next=\""+now+"\" step=\"3 hour\"/>"
                                            + "<repeat next=\""+now+"\" step=\"1 day\"/>"
                                            + "<repeat next=\""+now+"\" step=\"2 day\"/>"
                                            + "<repeat next=\""+now+"\" step=\"2 day\"/>"
                      + "</schedule>",
                      jcl.getSchedule() );
    }
}
