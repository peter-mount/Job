/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.table;

import java.util.Objects;

/**
 *
 * @author peter
 */
public class Header
        extends Molecule<Cell>
{

    /**
     * Append a value into the next cell
     * <p>
     * @param v Value to append
     * <p>
     * @return this row
     */
    public Header append( Object v )
    {
        newCell( v );
        return this;
    }

    private Cell add( Cell c )
    {
        getElements().add( c );
        return c;
    }

    /**
     * Append a value into the next cell.
     * <p>
     * Unlike {@link #append(java.lang.Object) } this returns the cell for further customisation
     * <p>
     * @param v value to append
     * <p>
     * @return the cell created.
     */
    public Cell newCell( Object v )
    {
        return add( new Cells.StringCell().setValue( Objects.toString( v ) ) );
    }

    @Override
    public void accept( TableVisitor t )
    {
        t.visit( this );
    }

}
