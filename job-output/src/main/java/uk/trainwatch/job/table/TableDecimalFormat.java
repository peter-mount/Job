/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.table;

import java.text.DecimalFormat;
import java.text.FieldPosition;

/**
 *
 * @author peter
 */
public class TableDecimalFormat
        extends TableStringFormat<TableDecimalFormat>
{

    private DecimalFormat f;

    public TableDecimalFormat setFormat( String fmt )
    {
        f = new DecimalFormat( fmt );
        return this;
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public TableDecimalFormat()
    {
        this.f = null;
        setAlignment( Alignment.RIGHT );
    }

    public TableDecimalFormat setAlignment( Alignment alignment )
    {
        return super.setAlignment( alignment == null ? Alignment.RIGHT : alignment );
    }

    @Override
    @SuppressWarnings("StringBufferMayBeStringBuilder")
    public StringBuffer format( Object obj, StringBuffer toAppendTo, FieldPosition pos )
    {
        if( f == null || !(obj instanceof Number) ) {
            return super.format( obj, toAppendTo, pos );
        }

        StringBuffer b = new StringBuffer();
        f.format( obj, b, pos );
        return super.format( b.toString(), toAppendTo, pos );
    }

}
