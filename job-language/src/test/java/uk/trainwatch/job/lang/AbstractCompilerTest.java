/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.ANTLRInputStream;
import static org.junit.Assert.*;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.util.LogHandler;

/**
 * Methods used by the generated CompilerTest class
 * <p>
 * @author peter
 */
public abstract class AbstractCompilerTest
        extends AbstractTest
{

    /**
     * Compile a job
     * <p>
     * @param t
     *          <p>
     * @return
     *         <p>
     * @throws IOException
     */
    protected final Job compileJob( String n )
            throws IOException
    {
        try( InputStream is = getScript( n + ".job" ) ) {
            return Compiler.compile( new ANTLRInputStream( is ) );
        }
    }

    /**
     * Compile a job and ensure the top Job object is correct
     * <p>
     * @param n
     *          <p>
     * @return
     *         <p>
     * @throws IOException
     */
    protected final Job compile( String n )
            throws IOException
    {
        Job job = compileJob( n );
        assertNotNull( "No Job from compilation",job );
        assertEquals( "Job id", n, job.getId() );
        return job;
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

    /**
     * Execute a job
     * <p>
     * @param job
     *            <p>
     * @throws Exception
     */
    protected final void execute( Job job )
            throws Exception
    {
        String n = job.getId();

        List<String> log = new ArrayList<>();

        Logger logger = LogHandler.getLogger( "test." + n,
                                              r -> String.format( "%-6.6s%-6.6s %s", n, r.getLevel(), r.getMessage() ),
                                              s -> {
                                                  log.add( s );
                                                  System.out.println( s );
                                              } );
        logger.setLevel( Level.FINE );

        final Scope scope = Scope.newInstance( logger );

        // Now run the job
        job.invoke( scope );

        testLog( log, n );
    }
}