/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.table;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Objects;

/**
 * A simple Formatter for strings
 * <p>
 * @author peter
 * @param <F>
 */
public class TableStringFormat<F extends TableStringFormat>
        extends Format
{

    private int maxLength;
    private Alignment alignment;

    public int getMaxLength()
    {
        return maxLength;
    }

    public F setMaxLength( int maxLength )
    {
        this.maxLength = maxLength;
        return (F) this;
    }

    public Alignment getAlignment()
    {
        return alignment;
    }

    public F setAlignment( Alignment alignment )
    {
        this.alignment = alignment==null?Alignment.LEFT:alignment;
        return (F) this;
    }

    public F left()
    {
        alignment = Alignment.LEFT;
        return (F) this;
    }

    public F right()
    {
        alignment = Alignment.RIGHT;
        return (F) this;
    }

    public F center()
    {
        alignment = Alignment.CENTER;
        return (F) this;
    }

    @Override
    public StringBuffer format( Object obj, StringBuffer toAppendTo, FieldPosition pos )
    {
        String s = Objects.toString( obj );
        if( maxLength == 0 ) {
            return toAppendTo.append( s );
        }

        int l = s.length();
        if( l < maxLength ) {
            int f = maxLength - l;
            if( alignment == Alignment.RIGHT ) {
                l += append( toAppendTo, f );
            }
            else if( alignment == Alignment.CENTER ) {
                l += append( toAppendTo, f >>> 1 );
            }

            toAppendTo.append( s );

            if( alignment == Alignment.LEFT ) {
                l += append( toAppendTo, f );
            }
            else if( alignment == Alignment.CENTER ) {
                l += append( toAppendTo, f >>> 1 );
            }

            if( l < maxLength ) {
                append( toAppendTo, maxLength - l );
            }
            return toAppendTo;
        }

        return toAppendTo.append( s, 0, maxLength );
    }

    private int append( StringBuffer toAppendTo, int c )
    {
        for( int i = 0; i < c; i++ ) {
            toAppendTo.append( ' ' );
        }
        return c;
    }

    @Override
    public Object parseObject( String source, ParsePosition pos )
    {
        throw new UnsupportedOperationException();
    }

}
