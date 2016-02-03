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

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * tests JclScript
 *
 * @author peter
 */
public class JclScriptTest
{

    private static final String PREFIX = "#!/bin/bash\n";
    private static final String JOB = "## job Node.Name;\n";
    private static final String SCRIPT = "Test script\nLine2\n";

    private void test( JclScript<String> jclScript )
    {
        assertNotNull( jclScript );

        Jcl jcl = jclScript.getJcl();
        assertNotNull( jcl );
        assertTrue( "Jcl should be valid", jcl.isValid() );
        assertEquals( "Node", "Node", jcl.getNode() );
        assertEquals( "Name", "Name", jcl.getName() );

        String script = jclScript.getScript();
        assertNotNull( "Script", script );
        assertEquals( "Script", SCRIPT, script );
    }

    /**
     * Tests the basic JCL
     *
     * @throws IOException
     */
    @Test
    public void jcl()
            throws IOException
    {
        test( JclScript.read( JOB + SCRIPT ) );
    }

    /**
     * Tests we can process Jcl with various prefixes
     *
     * @throws IOException
     */
    @Test
    public void prefix()
            throws IOException
    {
        test( JclScript.read( PREFIX + JOB + SCRIPT ) );
        test( JclScript.read( "\n\n" + JOB + SCRIPT ) );
        test( JclScript.read( PREFIX + "\n\n" + JOB + SCRIPT ) );
    }

    /**
     * Test that we can handle a script with no jcl present
     *
     * @throws IOException
     */
    @Test
    public void noJcl()
            throws IOException
    {
        JclScript<String> jclScript = JclScript.read( SCRIPT );
        Jcl jcl = jclScript.getJcl();
        assertFalse( "Jcl should not be valid", jcl.isValid() );
        assertNotNull( jcl );

        String script = jclScript.getScript();
        assertNotNull( "Script", script );
        assertEquals( "Script", SCRIPT, script );
    }
}
