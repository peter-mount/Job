/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.lang.JobParser.*;

/**
 *
 * @author peter
 */
public class Compiler
{

    private Compiler()
    {
    }

    public static JobParser parse( CharStream input )
    {
        JobLexer lexer = new JobLexer( input );
        CommonTokenStream tokens = new CommonTokenStream( lexer );
        return new JobParser( tokens );
    }

    public static Job compile( JobContext ctx )
    {
        String id = ctx.ID().getText();
        return new Job()
        {

            @Override
            public String getId()
            {
                return id;
            }

            @Override
            public Void invoke( Scope scope )
                    throws Exception
            {
                return null;
            }
        };
    }

    public static Block compile( BlockContext ctx )
    {
        ctx.children.forEach( c -> {
        } );
        return new Block( new Statement[0] );
    }
}
