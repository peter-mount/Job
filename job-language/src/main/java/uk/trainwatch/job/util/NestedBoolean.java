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
public class NestedBoolean
        extends Nestable<Boolean>
{

    @Override
    protected Boolean init()
    {
        return Boolean.FALSE;
    }

    @Override
    protected Boolean newInstance()
    {
        return Boolean.FALSE;
    }

    public void set( boolean instance )
    {
        this.instance = instance;
    }

    public boolean get()
    {
        return instance;
    }
}
