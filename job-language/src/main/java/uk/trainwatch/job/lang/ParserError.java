/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 *
 * @author peter
 */
public class ParserError
        extends AssertionError
{

    private final int line;
    private final int charPositionInLine;
    private final String msg;
    private final String sourceName;

    public ParserError( Recognizer<?, ?> recognizer, Object offendingSymbol,
                        int line, int charPositionInLine,
                        String msg, RecognitionException e )
    {
        super( String.format( "%d:%d %s", line, charPositionInLine, msg ), e );
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        this.msg = msg;
        sourceName = recognizer.getInputStream().getSourceName();
    }

    public final int getLine()
    {
        return line;
    }

    public final int getCharPositionInLine()
    {
        return charPositionInLine;
    }

    public final String getMsg()
    {
        return msg;
    }

    public final String getSourceName()
    {
        return sourceName;
    }

}
