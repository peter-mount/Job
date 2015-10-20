/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.script;

import java.io.InputStream;
import java.io.InputStreamReader;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.trainwatch.job.Scope;

/**
 *
 * @author peter
 */
public class JobScriptEngineTest
{

    private static ScriptEngineManager scriptEngineManager;

    @BeforeClass
    public static void beforeClass()
    {
        scriptEngineManager = new ScriptEngineManager();
    }

    @Test(timeout = 5000L)
    public void execute()
            throws Exception
    {
        ScriptEngine engine = scriptEngineManager.getEngineByExtension( "job" );
        assertNotNull( engine );

        Bindings bindings = (Bindings) Scope.newInstance();
        bindings.put( "finalA", 0 );

        try( InputStream is = getClass().getResourceAsStream( "test.job" ) ) {
            assertNotNull( is );

            try( InputStreamReader r = new InputStreamReader( is ) ) {
                assertNull( engine.eval( r, bindings ) );
            }
        }

        Object finalA = bindings.get( "finalA" );
        assertNotNull( "No finalA", finalA );
        assertTrue( "finalA not an int", finalA instanceof Integer );
        assertEquals( 10, finalA );
    }
}
