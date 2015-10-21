/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.table.visitors;

import java.io.PrintWriter;
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

    private final PrintWriter w;

    private boolean firstHeader;
    private boolean inHeader;
    private boolean firstRow;
    private boolean inRow;

    private final List<TableStringFormat> formatters;
    private int col;
    private String cellName;

    public XHTMLTableVisitor( PrintWriter w, List<TableStringFormat> formatters )
    {
        this.w = w;
        this.formatters = formatters;
    }

    @Override
    public void visit( Table t )
    {
        w.print( "<table>" );

        t.forEachHeader( h -> h.accept( this ) );
        if( inHeader ) {
            w.print( "</thead>" );
        }

        t.forEach( r -> r.accept( this ) );
        if( inRow ) {
            w.print( "</tbody>" );
        }

        w.print( "</table>" );
    }

    @Override
    public void visit( Header h )
    {
        if( firstHeader ) {
            w.print( "<thead>" );
            firstHeader = false;
            inHeader = true;
        }

        w.print( "<tr>" );
        col = 0;
        cellName="th";
        h.forEach( c -> c.accept( this ) );
        w.print( "</tr>" );
    }

    @Override
    public void visit( Row r )
    {
        if( firstRow ) {
            w.print( "<tbody>" );
            firstRow = false;
            inRow = true;
        }

        w.print( "<tr>" );
        col = 0;
        cellName="td";
        r.forEach( c -> c.accept( this ) );
        w.print( "</tr>" );
    }

    private void visitCell( Cells c )
    {
        TableStringFormat f = c.getFormat();
        if( f == null ) {
            f = formatters.get( col );
        }

        w.print( '<' );
        w.print( cellName );
        if( c.getColspan() > 1 ) {
            w.print( " colspan=\"" );
            w.print( c.getColspan() );
            w.print( "\"" );
        }

        Alignment a = f.getAlignment();
        if( a == Alignment.CENTER || a == Alignment.CENTER ) {
            w.print( " align=\"" );
            w.print( a.toString().toLowerCase() );
            w.print( "\"" );
        }

        w.print( '>' );

        w.print( f.format( c.getValue() ).trim() );

        w.print( "</" );
        w.print( cellName );
        w.print( '>' );

        if( c.getColspan() > 1 ) {
            col += c.getColspan();
        }
        else {
            col++;
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
