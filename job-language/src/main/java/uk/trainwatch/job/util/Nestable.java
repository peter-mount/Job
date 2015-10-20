/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.util;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * A {@link List} implementation who's contents can be nested.
 * <p>
 * To begin this must be used in a try resources block for example {@code try(NestedList l = list.begin) { } }.
 * <p>
 * Then whilst inside that block the list can be used as a normal {@link List}. However from outside the try any use of the
 * list will throw {@link NullPointerException} as the underlying list will no longer exist.
 * <p>
 * This class can be nested. So if whilst inside a try block another one is used then it will create a new underlying list and any changes made will only affect
 * that one. When the inner try block completes then that instance is discarded but the previous one is restored. Nesting can occur as many times as needed
 * limited only by memory and the number of nested try blocks supported by Java.
 * <p>
 * @author peter
 * @param <C>
 * @param <T> Type of each element in the list
 */
public abstract class Nestable<C>
        implements AutoCloseable
{

    private final Deque<C> stack = new LinkedList<>();
    protected C instance = init();

    protected C init()
    {
        return null;
    }

    public final Nestable<C> begin()
    {
        if( instance != null ) {
            stack.push( instance );
        }
        instance = newInstance();
        return this;
    }

    @Override
    public void close()
    {
        instance = stack.poll();
    }

    /**
     * Execute the supplied {@link Runnable} within the scope of this list.
     * <p>
     * Whilst it's running a list will exist and any actions on the list will apply to that underlying list.
     * <p>
     * <p>
     * @param r Runnable
     * <p>
     * @return The generated list
     */
    public final C apply( Runnable r )
    {
        try( Nestable<C> l = begin() ) {
            r.run();
            return l.getInstance();
        }
    }

    public boolean isEmpty()
    {
        return instance == null;
    }

    protected abstract C newInstance();

    /**
     * Returns the current list.
     * <p>
     * This will be the underlying instance. This can be used if it's to be kept once the current scope expires.
     * <p>
     * @return the current List or null if none
     */
    public final C getInstance()
    {
        return instance;
    }

    /**
     * The underlying list as a string or "null" if none
     * <p>
     * @return
     */
    @Override
    public final String toString()
    {
        return Objects.toString( instance );
    }
}
