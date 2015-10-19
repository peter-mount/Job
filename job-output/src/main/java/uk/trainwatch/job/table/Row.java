/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.table;

/**
 *
 * @author peter
 */
public class Row
        extends Molecule<Cell>
{

    /**
     * Append a value into the next cell
     * <p>
     * @param v Value to append
     * <p>
     * @return this row
     */
    public Row append( Object v )
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
        if( v instanceof String ) {
            return add( new Cells.StringCell().setValue( (String) v ) );
        }

        if( v instanceof Number ) {
            return add( new Cells.NumberCell().setValue( (Number) v ) );
        }

        return add( new Cells.ObjectCell().setValue( v ) );
    }

    @Override
    public void accept( TableVisitor t )
    {
        t.visit( this );
    }

}
