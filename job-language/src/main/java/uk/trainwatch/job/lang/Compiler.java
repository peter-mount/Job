/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.lang.JobParser.*;

/**
 *
 * @author peter
 */
public class Compiler
        extends AbstractCompiler
{

    private Job job;

    public static Job compile( CharStream input )
    {
        ANTLRErrorListener errorListener = new BaseErrorListener()
        {

            @Override
            public void syntaxError( Recognizer<?, ?> recognizer, Object offendingSymbol,
                                     int line, int charPositionInLine,
                                     String msg, RecognitionException e )
            {
                //String sourceName = recognizer.getInputStream().getSourceName();
                throw new AssertionError(
                        String.format( "%d:%d %s",
                                       line, charPositionInLine,
                                       msg )
                );
            }
        };

        JobLexer lexer = new JobLexer( input );
        lexer.removeErrorListeners();
        lexer.addErrorListener( errorListener );

        CommonTokenStream tokens = new CommonTokenStream( lexer );

        JobParser parser = new JobParser( tokens );
        parser.removeErrorListeners();
        parser.addErrorListener( errorListener );

        Compiler compiler = new Compiler();
        parser.addParseListener( compiler );
        CompilationUnitContext compilationUnitContext = parser.compilationUnit();

        return compiler.getJob();
    }

    private Compiler()
    {
    }

    public Job getJob()
    {
        return job;
    }

    @Override
    public void exitJobDefinition( JobDefinitionContext ctx )
    {
        if( ctx.getChildCount() < 1 ) {
            throw new IndexOutOfBoundsException( "job Name" );
        }

        final String id = getString( ctx, 1 );
        job = new Job()
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

}
