/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.UnaryOperator;

/**
 * A {@link List} implementation who's contents can be nested.
 * <p>
 * To begin this must be used in a try resources block for example {@code try(NestedList l = getInstance().begin) { } }.
 * <p>
 * Then whilst inside that block the list can be used as a normal {@link List}. However from outside the try any use of the
 * list will throw {@link NullPointerException} as the underlying list will no longer exist.
 * <p>
 * This class can be nested. So if whilst inside a try block another one is used then it will create a new underlying list and any changes made will only affect
 * that one. When the inner try block completes then that instance is discarded but the previous one is restored. Nesting can occur as many times as needed
 * limited only by memory and the number of nested try blocks supported by Java.
 * <p>
 * @author peter
 * @param <T> Type of each element in the list
 */
public abstract class NestedList<T>
        extends Nestable<List<T>>
        implements List<T>
{

    //<editor-fold defaultstate="collapsed" desc="Delegate List to current instance">
    @Override
    public int size()
    {
        return getInstance().size();
    }

    @Override
    public boolean isEmpty()
    {
        return getInstance().isEmpty();
    }

    @Override
    public boolean contains( Object o )
    {
        return getInstance().contains( o );
    }

    @Override
    public Iterator<T> iterator()
    {
        return getInstance().iterator();
    }

    @Override
    public Object[] toArray()
    {
        return getInstance().toArray();
    }

    @Override
    public <T> T[] toArray( T[] a )
    {
        return getInstance().toArray( a );
    }

    @Override
    public boolean add( T e )
    {
        return getInstance().add( e );
    }

    @Override
    public boolean remove( Object o )
    {
        return getInstance().remove( o );
    }

    @Override
    public boolean containsAll(
            Collection<?> c )
    {
        return getInstance().containsAll( c );
    }

    @Override
    public boolean addAll(
            Collection<? extends T> c )
    {
        return getInstance().addAll( c );
    }

    @Override
    public boolean addAll( int index,
                           Collection<? extends T> c )
    {
        return getInstance().addAll( index, c );
    }

    @Override
    public boolean removeAll(
            Collection<?> c )
    {
        return getInstance().removeAll( c );
    }

    @Override
    public boolean retainAll(
            Collection<?> c )
    {
        return getInstance().retainAll( c );
    }

    @Override
    public void replaceAll( UnaryOperator<T> operator )
    {
        getInstance().replaceAll( operator );
    }

    @Override
    public void sort(
            Comparator<? super T> c )
    {
        getInstance().sort( c );
    }

    @Override
    public void clear()
    {
        getInstance().clear();
    }

    @Override
    public boolean equals( Object o )
    {
        return getInstance().equals( o );
    }

    @Override
    public int hashCode()
    {
        return getInstance().hashCode();
    }

    @Override
    public T get( int index )
    {
        return getInstance().get( index );
    }

    @Override
    public T set( int index, T element )
    {
        return getInstance().set( index, element );
    }

    @Override
    public void add( int index, T element )
    {
        getInstance().add( index, element );
    }

    @Override
    public T remove( int index )
    {
        return getInstance().remove( index );
    }

    @Override
    public int indexOf( Object o )
    {
        return getInstance().indexOf( o );
    }

    @Override
    public int lastIndexOf( Object o )
    {
        return getInstance().lastIndexOf( o );
    }

    @Override
    public ListIterator<T> listIterator()
    {
        return getInstance().listIterator();
    }

    @Override
    public ListIterator<T> listIterator( int index )
    {
        return getInstance().listIterator( index );
    }

    @Override
    public List<T> subList( int fromIndex, int toIndex )
    {
        return getInstance().subList( fromIndex, toIndex );
    }

    @Override
    public Spliterator<T> spliterator()
    {
        return getInstance().spliterator();
    }
//</editor-fold>

    /**
     * A {@link NestedList} which uses an {@link ArrayList} as the underlying getInstance().
     * <p>
     * @param <T> type of each element
     */
    public static class Array<T>
            extends NestedList<T>
    {

        @Override
        protected List<T> newInstance()
        {
            return new ArrayList<>();
        }

    }

    /**
     * A {@link NestedList} which uses a {@link LinkedList} as the underlying getInstance().
     * <p>
     * @param <T> type of each element
     */
    public static class Linked<T>
            extends NestedList<T>
    {

        @Override
        protected List<T> newInstance()
        {
            return new LinkedList<>();
        }

    }
}
