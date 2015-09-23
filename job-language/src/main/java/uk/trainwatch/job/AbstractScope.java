/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author peter
 */
abstract class AbstractScope
        implements Scope
{

    protected final Map<String, Object> vars = new HashMap<>();

    protected AbstractScope()
    {
    }

    @Override
    public boolean exists( String name )
    {
        return vars.containsKey( name );
    }

    @Override
    public <T> T setVar( String name, T val )
    {
        return (T) vars.put( name, val );
    }

    static class GlobalScope
            extends AbstractScope
    {

        @Override
        public <T> T getVar( String name )
        {
            return (T) vars.get( name );
        }

        @Override
        public void close()
                throws Exception
        {
            // The global scope is never cleared
        }

        @Override
        public Scope begin()
        {
            return new DefaultScope( this );
        }

    }

    private static abstract class AbstractChildScope
            extends AbstractScope
    {

        protected final GlobalScope globalScope;

        protected AbstractChildScope( GlobalScope globalScope )
        {
            this.globalScope = globalScope;
        }

        @Override
        public <T> T setVar( String name, T val )
        {
            return globalScope.exists( name ) ? globalScope.setVar( name, val ) : (T) vars.put( name, val );
        }

        @Override
        public void close()
                throws Exception
        {
            vars.clear();
        }

        @Override
        public Scope begin()
        {
            return new SubScope( globalScope, this );
        }

    }

    private static class DefaultScope
            extends AbstractChildScope
    {

        protected DefaultScope( GlobalScope globalScope )
        {
            super( globalScope );
        }

        @Override
        public <T> T getVar( String name )
        {
            T v = (T) vars.get( name );

            if( v == null ) {
                v = globalScope.getVar( name );

                // Cache the result to improve performance
                if( v != null ) {
                    vars.put( name, v );
                }
            }

            return v;
        }

    }

    private static class SubScope
            extends AbstractChildScope
    {

        private Scope parentScope;

        protected SubScope( GlobalScope globalScope, Scope parentScope )
        {
            super( globalScope );
            this.parentScope = parentScope;
        }

        @Override
        public <T> T getVar( String name )
        {
            T v = (T) vars.get( name );

            if( v == null ) {
                v = globalScope.getVar( name );

                if( v == null ) {
                    v = parentScope.getVar( name );
                }

                // Cache the result to improve performance
                if( v != null ) {
                    vars.put( name, v );
                }
            }

            return v;
        }

    }
}
