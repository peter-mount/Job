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
public class NestedCounter
        extends Nestable<Integer>
{

    private Integer last;

    @Override
    protected Integer newInstance()
    {
        return 0;
    }

    @Override
    public void close()
    {
        last = instance;
        super.close();
    }

    public Integer getLast()
    {
        return last;
    }

    public void increment()
    {
        if( instance != null ) {
            instance++;
        }
    }

    public int get()
    {
        return instance == null ? -1 : instance;
    }

    public int getAndIncrement()
    {
        if( instance != null ) {
            int c = instance;
            instance++;
            return c;
        }
        return -1;
    }

    public int incrementAndGet()
    {
        if( instance != null ) {
            instance++;
            return instance;
        }
        return -1;
    }
}
