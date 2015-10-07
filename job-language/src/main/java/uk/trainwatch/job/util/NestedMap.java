/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public abstract class NestedMap<K, V>
        extends Nestable<Map<K, V>>
        implements Map<K, V>
{

    @Override
    public int size()
    {
        return instance.size();
    }

    @Override
    public boolean isEmpty()
    {
        return instance.isEmpty();
    }

    @Override
    public boolean containsKey( Object key )
    {
        return instance.containsKey( key );
    }

    @Override
    public boolean containsValue( Object value )
    {
        return instance.containsValue( value );
    }

    @Override
    public V get( Object key )
    {
        return instance.get( key );
    }

    @Override
    public V put( K key, V value )
    {
        return instance.put( key, value );
    }

    @Override
    public V remove( Object key )
    {
        return instance.remove( key );
    }

    @Override
    public void putAll( Map<? extends K, ? extends V> m )
    {
        instance.putAll( m );
    }

    @Override
    public void clear()
    {
        instance.clear();
    }

    @Override
    public Set<K> keySet()
    {
        return instance.keySet();
    }

    @Override
    public Collection<V> values()
    {
        return instance.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return instance.entrySet();
    }

    public static class Hash<K, V>
            extends NestedMap<K, V>
    {

        @Override
        protected Map<K, V> newInstance()
        {
            return new HashMap<>();
        }

    }
    
    public static class Linked<K, V>
            extends NestedMap<K, V>
    {

        @Override
        protected Map<K, V> newInstance()
        {
            return new LinkedHashMap<>();
        }

    }
    
}
