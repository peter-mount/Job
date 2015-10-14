/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang;

import java.io.IOException;
import java.io.UncheckedIOException;
import uk.trainwatch.job.lang.header.CompilationUnitCompiler;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import uk.trainwatch.job.Job;

/**
 *
 * @author peter
 */
public class Compiler
{

    public static JobParser parse( CharStream input )
    {
        ANTLRErrorListener errorListener = new BaseErrorListener()
        {

            @Override
            public void syntaxError( Recognizer<?, ?> recognizer, Object offendingSymbol,
                                     int line, int charPositionInLine,
                                     String msg, RecognitionException e )
            {
                throw new SyntaxError( recognizer, offendingSymbol, line, charPositionInLine, msg, e );
            }
        };

        JobLexer lexer = new JobLexer( input );
        lexer.removeErrorListeners();
        lexer.addErrorListener( errorListener );

        CommonTokenStream tokens = new CommonTokenStream( lexer );

        JobParser parser = new JobParser( tokens );
        parser.removeErrorListeners();
        parser.addErrorListener( errorListener );

        return parser;
    }

    public static Job compile( JobParser parser )
    {
        return new CompilationUnitCompiler().compile( parser );
    }

    public static Job compile( CharStream input )
    {
        return compile( parse( input ) );
    }

    public static ANTLRInputStream fromFile( String n )
    {
        try {
            return new ANTLRFileStream( n );
        }
        catch( IOException ex ) {
            throw new UncheckedIOException( ex );
        }
    }

    private Compiler()
    {
    }

}
