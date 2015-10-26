/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.table;

import uk.trainwatch.job.table.visitors.TableVisitor;

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

    void removeColumn( int col )
    {
        if( col < size() ) {
            getElements().remove( col );
        }
    }

    public Cell set( int index, Object v )
    {
        while( size() <= index ) {
            newCell( "" );
        }
        Cell cell = createCell( v );
        getElements().set( index, cell );
        return cell;
    }

    private Cell createCell( Object v )
    {
        if( v instanceof String ) {
            return new Cells.StringCell().setValue( (String) v );
        }

        if( v instanceof Number ) {
            return new Cells.NumberCell().setValue( (Number) v );
        }

        return new Cells.ObjectCell().setValue( v );
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
        return add( createCell( v ) );
    }

    @Override
    public void accept( TableVisitor t )
    {
        t.visit( this );
    }

}
