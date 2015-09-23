/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang;

import java.io.IOException;
import java.io.InputStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import static org.junit.Assert.*;
import uk.trainwatch.job.Job;

/**
 * Methods used by the generated CompilerTest class
 * <p>
 * @author peter
 */
public abstract class AbstractCompilerTest
        extends AbstractTest
{

    protected Job compile( String t )
            throws IOException
    {
        try( InputStream is = getScript( t ) ) {
            return Compiler.compile( new ANTLRInputStream( is ) );
        }
    }

    protected Job test( String n )
            throws IOException
    {
        return test( n + ".job", n );
    }

    protected Job test( String t, String n )
            throws IOException
    {
        Job job = compile( t );
        assertNotNull( job );
        assertEquals( n, job.getId() );
        return job;
    }

}
