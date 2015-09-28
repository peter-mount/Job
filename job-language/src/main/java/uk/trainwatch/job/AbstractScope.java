/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
    public <T> void setVar( String name, T val )
    {
        vars.put( name, val );
    }

    static class GlobalScope
            extends AbstractScope
            implements Scope.GlobalScope
    {

        protected Logger logger;

        @Override
        public void setLogger( Logger logger )
        {
            this.logger = logger;
        }

        @Override
        public Logger getLogger()
        {
            return logger;
        }

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
        public Logger getLogger()
        {
            return globalScope.getLogger();
        }

        protected abstract boolean put( String name, Object val );

        @Override
        public <T> void setVar( String name, T val )
        {
            if( vars.containsKey( name ) )
            {
                // If we have it then stop here
                vars.put( name, val );
            }
            else if( globalScope.exists( name ) )
            {
                // If global then straight to that scope
                globalScope.setVar( name, val );
            }
            else if( !put( name, val ) )
            {
                // Recurse down the scopes but if none claim it then put it into ours
                vars.put( name, val );
            }
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

            if( v == null )
            {
                v = globalScope.getVar( name );
            }

            return v;
        }

        @Override
        protected boolean put( String name, Object val )
        {
            if( vars.containsKey( name ) )
            {
                vars.put( name, val );
                return true;
            }
            return false;
        }

    }

    private static class SubScope
            extends AbstractChildScope
    {

        private final AbstractChildScope parentScope;

        protected SubScope( GlobalScope globalScope, AbstractChildScope parentScope )
        {
            super( globalScope );
            this.parentScope = parentScope;
        }

        @Override
        public <T> T getVar( String name )
        {
            T v = (T) vars.get( name );

            if( v == null )
            {
                v = globalScope.getVar( name );

                if( v == null )
                {
                    v = parentScope.getVar( name );
                }
            }

            return v;
        }

        @Override
        protected boolean put( String name, Object val )
        {
            if( vars.containsKey( name ) )
            {
                vars.put( name, val );
                return true;
            }
            return parentScope.put( name, val );
        }

    }
}
