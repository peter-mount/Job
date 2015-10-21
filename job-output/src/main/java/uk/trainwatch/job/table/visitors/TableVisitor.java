/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.table.visitors;

import uk.trainwatch.job.table.Cells;
import uk.trainwatch.job.table.Header;
import uk.trainwatch.job.table.Row;
import uk.trainwatch.job.table.Table;

/**
 *
 * @author peter
 */
public interface TableVisitor
{

    default void visit( Table t )
    {
        t.forEachHeader( h -> h.accept( this ) );
        t.forEach( r -> r.accept( this ) );
    }

    default void visit( Header h )
    {
        h.forEach( c -> c.accept( this ) );
    }

    default void visit( Row r )
    {
        r.forEach( c -> c.accept( this ) );
    }

    default void visit( Cells.StringCell c )
    {
    }

    default void visit( Cells.NumberCell c )
    {
    }

    default void visit( Cells.ObjectCell c )
    {
    }
}
