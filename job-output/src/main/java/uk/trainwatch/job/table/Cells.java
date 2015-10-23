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
 * @param <T>
 * @param <F>
 */
public abstract class Cells<T, F extends TableStringFormat>
        implements Cell<T>
{

    protected F format;

    private int colspan = 1;

    private T value;

    @Override
    public T getValue()
    {
        return value;
    }

    @Override
    public Cell setValue( T value )
    {
        this.value = value;
        return this;
    }

    @Override
    public F getFormat()
    {
        return format;
    }

    public Cell<T> setFormat( F format )
    {
        this.format = format;
        return this;
    }

    @Override
    public int getColspan()
    {
        return colspan;
    }

    public void setColspan( int colspan )
    {
        this.colspan = colspan;
    }

    public static class StringCell
            extends Cells<String, TableStringFormat>
    {

        @Override
        public void accept( TableVisitor t )
        {
            t.visit( this );
        }

        @Override
        public TableStringFormat newFormat()
        {
            return new TableStringFormat();
        }

    }

    public static class NumberCell
            extends Cells<Number, TableDecimalFormat>
    {

        @Override
        public void accept( TableVisitor t )
        {
            t.visit( this );
        }

        @Override
        public TableDecimalFormat newFormat()
        {
            return new TableDecimalFormat();
        }

    }

    public static class ObjectCell
            extends Cells<Object, TableStringFormat>
    {

        @Override
        public void accept( TableVisitor t )
        {
            t.visit( this );
        }

        @Override
        public TableDecimalFormat newFormat()
        {
            return new TableDecimalFormat();
        }

    }

}
