/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.table.visitors;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import uk.trainwatch.job.table.Cells;
import uk.trainwatch.job.table.Row;
import uk.trainwatch.job.table.Table;
import uk.trainwatch.job.table.TableStringFormat;

/**
 *
 * @author peter
 */
public class FormatTableVisitor
        implements TableVisitor
{

    private List<Integer> widths;
    private List<TableStringFormat> formatters;
    private int col;
    private int maxCol;

    public static List<TableStringFormat> getFormatters( Table t )
    {
        FormatTableVisitor v = new FormatTableVisitor();
        t.accept( v );
        return v.getFormatters();
    }

    public List<TableStringFormat> getFormatters()
    {
        return formatters;
    }

    @Override
    public void visit( Table t )
    {
        widths = new ArrayList<>();
        formatters = t.getFormats();

        t.forEachHeader( this::visit );
        t.forEach( this::visit );

        // Any unset formats force fixed width
        for( int i = 0; i < maxCol; i++ ) {
            TableStringFormat f = formatters.get( i );
            int w = widths.get( i );
            if( f == null ) {
                f = new TableStringFormat()
                        .setMaxLength( w )
                        .left();
                formatters.set( i, f );
            }
            else {
                f.setMaxLength( w );
            }
        }

    }

    @Override
    public void visit( Row r )
    {
        col = 0;
        r.forEach( c -> c.accept( this ) );
        maxCol = Math.max( maxCol, col );
    }

    private void replace( Format f, Cells c )
    {
        while( formatters.size() <= col ) {
            formatters.add( null );
        }

        while( widths.size() <= col ) {
            widths.add( 0 );
        }

        TableStringFormat tsf = formatters.get( col );
        if( tsf == null ) {
            if( f != null ) {
                tsf = new TableStringFormat();
                formatters.set( col, tsf );
            }
        }

        final String v = Objects.toString( c.getValue() );
        if( tsf == null ) {
            // No format so a plain one
            widths.set( col, Math.max( widths.get( col ), v.length() ) );
        }
        else if( tsf.getMaxLength() < 1 ) {
            // A dynamic field
            widths.set( col, Math.max( widths.get( col ), tsf.format( v ).length() ) );
        }
        else {
            widths.set( col, Math.max( widths.get( col ), tsf.getMaxLength() ) );
        }

        final int colspan = c.getColspan();
        if( colspan > 1 ) {
            col += colspan;
        }
        else {
            col++;
        }
    }

    @Override
    public void visit( Cells.StringCell c )
    {
        replace( c.getFormat(), c );
    }

    @Override
    public void visit( Cells.NumberCell c )
    {
        replace( c.getFormat(), c );
    }

    @Override
    public void visit( Cells.ObjectCell c )
    {
        replace( c.getFormat(), c );
    }

}
