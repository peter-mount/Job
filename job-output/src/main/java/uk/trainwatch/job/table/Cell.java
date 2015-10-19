/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.table;

import java.text.Format;

/**
 *
 * @author peter
 */
public interface Cell<T>
        extends Atom
{

    T getValue();

    default Cell<T> setValue( T v )
    {
        return this;
    }

    default <F extends Format> F getFormat()
    {
        return null;
    }

    default <F extends Format> F newFormat()
    {
        return null;
    }
}
