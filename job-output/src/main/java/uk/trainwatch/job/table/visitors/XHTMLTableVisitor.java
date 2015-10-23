/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.table.visitors;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import uk.trainwatch.job.table.Alignment;
import uk.trainwatch.job.table.Cells;
import uk.trainwatch.job.table.Header;
import uk.trainwatch.job.table.Row;
import uk.trainwatch.job.table.Table;
import uk.trainwatch.job.table.TableStringFormat;

/**
 *
 * @author peter
 */
public class XHTMLTableVisitor
        implements TableVisitor
{

    private final Appendable w;

    private boolean firstHeader;
    private boolean inHeader;
    private boolean firstRow;
    private boolean inRow;

    private final List<TableStringFormat> formatters;
    private int col;
    private String cellName;

    public XHTMLTableVisitor( Appendable w, List<TableStringFormat> formatters )
    {
        this.w = w;
        this.formatters = formatters;
    }

    @Override
    public void visit( Table t )
    {
        try {
            w.append( "<table>" );

            t.forEachHeader( h -> h.accept( this ) );
            if( inHeader ) {
                w.append( "</thead>" );
            }

            t.forEach( r -> r.accept( this ) );
            if( inRow ) {
                w.append( "</tbody>" );
            }

            w.append( "</table>" );
        }
        catch( IOException ex ) {
            throw new UncheckedIOException( ex );
        }
    }

    @Override
    public void visit( Header h )
    {
        try {
            if( firstHeader ) {
                w.append( "<thead>" );
                firstHeader = false;
                inHeader = true;
            }

            w.append( "<tr>" );
            col = 0;
            cellName = "th";
            h.forEach( c -> c.accept( this ) );
            w.append( "</tr>" );
        }
        catch( IOException ex ) {
            throw new UncheckedIOException( ex );
        }
    }

    @Override
    public void visit( Row r )
    {
        try {
            if( firstRow ) {
                w.append( "<tbody>" );
                firstRow = false;
                inRow = true;
            }

            w.append( "<tr>" );
            col = 0;
            cellName = "td";
            r.forEach( c -> c.accept( this ) );
            w.append( "</tr>" );
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

            w.append( '<' );
            w.append( cellName );
            if( c.getColspan() > 1 ) {
                w.append( " colspan=\"" );
                w.append( String.valueOf( c.getColspan() ) );
                w.append( "\"" );
            }

            Alignment a = f.getAlignment();
            if( a == Alignment.CENTER || a == Alignment.CENTER ) {
                w.append( " align=\"" );
                w.append( a.toString().toLowerCase() );
                w.append( "\"" );
            }

            w.append( '>' );

            w.append( f.format( c.getValue() ).trim() );

            w.append( "</" );
            w.append( cellName );
            w.append( '>' );

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
