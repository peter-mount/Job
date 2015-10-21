/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.table;

import uk.trainwatch.job.table.visitors.TableVisitor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author peter
 */
public class Header
        extends Molecule<Cell>
{

    private final Map<String, Integer> index = new HashMap<>();

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
        String s = Objects.toString( v );
        Cell cell = add( new Cells.StringCell().setValue( s ) );
        index.computeIfAbsent( s, k -> size()-1 );
        return cell;
    }

    @Override
    public void accept( TableVisitor t )
    {
        t.visit( this );
    }

    public int indexOf( String s )
    {
        return index.getOrDefault( s, -1 );
    }

    public boolean contains( String s )
    {
        return index.containsKey( s );
    }
}
