/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.util;

/**
 *
 * @author peter
 */
public class NestedObject<T>
        extends Nestable<T>
{

    @Override
    protected T newInstance()
    {
        return null;
    }

    public T set( T instance )
    {
        this.instance = instance;
        return instance;
    }

}
