/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang;

import org.antlr.v4.runtime.ANTLRInputStream;
import uk.trainwatch.job.Job;

/**
 *
 * @author peter
 */
public class ScriptReader
{

    public static void main( String... args )
            throws Exception
    {
        ScriptReader sr = new ScriptReader();
        JobParser parser = Compiler.parse( new ANTLRInputStream( "job \"Test\" run as \"Local\" {"
                + "}" ) );

        Job job = Compiler.compile( parser.job() );
        System.out.printf( "Job %s\n", job.getId() );

        throw new AssertionError();
    }
}
