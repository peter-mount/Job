/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.table.visitors;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import uk.trainwatch.job.table.Cells;
import uk.trainwatch.job.table.Header;
import uk.trainwatch.job.table.Row;
import uk.trainwatch.job.table.Table;
import uk.trainwatch.job.table.TableStringFormat;

/**
 * Table visitor to produce a plain text table
 * <p>
 * @author peter
 */
public class StringTableVisitor
        implements TableVisitor
{

    private final Appendable a;
    private final List<TableStringFormat> formatters;
    private String cellSeparator, headerSeparator, headerCorner;
    private int col;

    public StringTableVisitor( Appendable a, List<TableStringFormat> formatters )
    {
        this( a, formatters, "|", "=", "+" );
    }

    public StringTableVisitor( Appendable a, List<TableStringFormat> formatters, String cellSeparator, String headerSeparator, String headerCorner )
    {
        this.a = a;
        this.formatters = formatters;
        this.cellSeparator = cellSeparator;
        this.headerSeparator = headerSeparator;
        this.headerCorner = headerCorner;
    }

    public String getCellSeparator()
    {
        return cellSeparator;
    }

    public void setCellSeparator( String cellSeparator )
    {
        this.cellSeparator = Objects.toString( cellSeparator, "" );
    }

    public String getHeaderCorner()
    {
        return headerCorner;
    }

    public void setHeaderCorner( String headerCorner )
    {
        this.headerCorner = headerCorner;
    }

    public String getHeaderSeparator()
    {
        return headerSeparator;
    }

    public void setHeaderSeparator( String headerSeparator )
    {
        this.headerSeparator = headerSeparator;
    }
    private boolean sep;

    @Override
    public void visit( Table t )
    {
        if( !t.isHeadersEmpty() ) {
            sep = false;
            t.forEachHeader( h -> h.accept( this ) );

            sep = true;
            t.forEachHeader( h -> h.accept( this ) );

            sep = false;
        }

        t.forEach( r -> r.accept( this ) );
    }

    @Override
    public void visit( Row r )
    {
        try {
            col = 0;
            a.append( cellSeparator );
            r.forEach( c -> c.accept( this ) );
            a.append( '\n' );
        }
        catch( IOException ex ) {
            throw new UncheckedIOException( ex );
        }
    }

    @Override
    public void visit( Header h )
    {
        try {
            col = 0;
            a.append( sep ? headerCorner : cellSeparator );
            h.forEach( c -> c.accept( this ) );
            a.append( '\n' );
        }
        catch( IOException ex ) {
            throw new UncheckedIOException( ex );
        }
    }

    private void visitCell( Cells c )
    {
        try {
            TableStringFormat f = c.getFormat();
            if( f == null ) {
                f = formatters.get( col );
            }

            if( sep ) {
                int l = f.getMaxLength();
                for( int i = 0; i < l; i++ ) {
                    a.append( headerSeparator );
                }
            }
            else {
                a.append( f.format( c.getValue() ) );
            }

            a.append( sep ? headerCorner : cellSeparator );

            if( c.getColspan() > 1 ) {
                col += c.getColspan();
            }
            else {
                col++;
            }
        }
        catch( IOException ex ) {
            throw new UncheckedIOException( ex );
        }
    }

    @Override
    public void visit( Cells.StringCell c )
    {
        visitCell( c );
    }

    @Override
    public void visit( Cells.NumberCell c )
    {
        visitCell( c );
    }

    @Override
    public void visit( Cells.ObjectCell c )
    {
        visitCell( c );
    }

}
