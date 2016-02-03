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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * tests JclFactory
 *
 * @author peter
 */
public class JclFactoryTest
{

    /**
     * Test that a Jcl actually gets populated. This is the bare minimum Jcl
     */
    @Test
    public void job()
    {
        Jcl jcl = JclFactory.compileJcl( "## job Node.Name;" );
        assertNotNull( jcl );
        assertEquals( "Node", "Node", jcl.getNode() );
        assertEquals( "Name", "Name", jcl.getName() );
    }

}
