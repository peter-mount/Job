/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onl.area51.job.jcl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.ANTLRInputStream;
import static org.junit.Assert.*;

/**
 * Methods used by the generated CompilerTest class
 * <p>
 * @author peter
 */
public abstract class AbstractCompilerTest
{

    protected final void runTest( String n )
            throws Throwable
    {
        System.out.println( "Starting " + n );
        try {
            Jcl jcl = compileJcl(n);
            assertNotNull( "jcl node", jcl.getNode());
            assertFalse( "jcl node", jcl.getNode().isEmpty());
            assertEquals( "jcl node", "Test", jcl.getNode());
            
            assertNotNull( "jcl name", jcl.getName());
            assertFalse( "jcl name", jcl.getName().isEmpty());
            assertEquals( "jcl name", n, jcl.getName() );
            
            System.out.printf( "Jcl node \"%s\" job \"%s\"\n", jcl.getNode(), jcl.getName() );
            
            System.out.println( n + " Passed" );
        }
        catch( Throwable ex ) {
            System.out.println( n + " Failed: " + ex );
            throw ex;
        }
    }

    /**
     * Compile a job
     * <p>
     * @param n
     *          <p>
     * @return <p>
     * @throws IOException
     */
    protected final Jcl compileJcl( String n )
            throws IOException
    {
        try( InputStream is = getScript( n + ".job" ) ) {
            return JclFactory.compileJcl( new ANTLRInputStream( is ) );
        }
    }

    protected InputStream getScript( String t )
    {
        InputStream is = AbstractCompilerTest.class.getResourceAsStream( t );
        assertNotNull( "Failed to find " + t, is );
        return is;
    }

    protected final List<String> getLines( String n )
            throws IOException
    {
        try( BufferedReader r = new BufferedReader( new InputStreamReader( getClass().getResourceAsStream( n ) ) ) ) {
            return r.lines().collect( Collectors.toList() );
        }
        catch( NullPointerException ex ) {
            return Collections.emptyList();
        }
    }

    protected final void testLog( final List<String> log, final String n )
            throws IOException
    {
        List<String> expectedLog = getLines( n + ".log" );

        // No .log file or it's empty then don't test the log
        if( expectedLog.isEmpty() ) {
            return;
        }

        int es = expectedLog.size();
        int l = 0;
        for( String line: log ) {
            if( l < es ) {
                assertEquals( "Line " + l, expectedLog.get( l ), line );
            }
            else {
                fail( "Extra line " + l + ": " + line );
            }
            l++;
        }

        if( es > log.size() ) {
            fail( "Missing last " + (es - log.size()) + " lines" );
        }
    }

}
