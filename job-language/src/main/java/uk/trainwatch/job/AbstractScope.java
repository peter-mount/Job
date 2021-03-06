/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import onl.area51.job.jcl.Jcl;

/**
 *
 * @author peter
 */
public abstract class AbstractScope
        implements Scope
{

    private static final ThreadLocal<Scope> CURRENT_SCOPE = new ThreadLocal<>();

    private static final String STANDARD_IMPORTS[]
                                  = {
                // Collections
                "List", "java.util.ArrayList",
                "Set", "java.util.HashSet",
                "Queue", "java.util.LinkedList",
                "Deque", "java.util.LinkedList",
                "Map", "java.util.HashMap",
                // Exceptions
                "NullPointerException", "java.lang.NullPointerException",
                "RuntimeException", "java.lang.RuntimeException",
                "Exception", "java.lang.Exception"
            };
    private static final Map<String, Class<?>> IMPORT_CLASS = new ConcurrentHashMap<>();

    static {
        try {
            for( int i = 0; i < STANDARD_IMPORTS.length; i += 2 ) {
                IMPORT_CLASS.put( STANDARD_IMPORTS[i], Class.forName( STANDARD_IMPORTS[i + 1] ) );
            }
        }
        catch( ClassNotFoundException ex ) {
            throw new InstantiationError( ex.getMessage() );
        }
    }

    protected final Map<String, Object> vars = new HashMap<>();

    protected AbstractScope()
    {
    }

    public static Scope getCurrentScope()
    {
        return CURRENT_SCOPE.get();
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

    @Override
    public <T> void setLocalVar( String name, T val )
    {
        vars.put( name, val );
    }

    public static class GlobalScope
            extends AbstractScope
            implements Scope.GlobalScope
    {

        private final Map<String, String> imports = new LinkedHashMap<>();
        private Job job;
        private Jcl jcl;

        protected Logger logger;

        public GlobalScope()
        {
            this( null, null );
        }

        @SuppressWarnings("LeakingThisInConstructor")
        public GlobalScope( Logger logger )
        {
            this( null, logger );
        }

        @SuppressWarnings("LeakingThisInConstructor")
        public GlobalScope( Job job, Logger logger )
        {
            this.job = job;

            CURRENT_SCOPE.set( this );
            this.logger = logger;

            for( int i = 0; i < STANDARD_IMPORTS.length; i += 2 ) {
                imports.put( STANDARD_IMPORTS[i], STANDARD_IMPORTS[i + 1] );
            }
        }

        @Override
        public Job getJob()
        {
            return job;
        }

        @Override
        public void setJob( Job job )
        {
            this.job = job;
        }

        @Override
        public Jcl getJcl()
        {
            return jcl;
        }

        @Override
        public void setJcl( Jcl jcl )
        {
            this.jcl = jcl;
        }

        @Override
        public String resolveType( String type )
        {
            return imports.get( type );
        }

        @Override
        public String resolveClass( String clazz )
        {
            return imports.entrySet()
                    .stream()
                    .filter( c -> c.getValue().equals( clazz ) )
                    .map( Map.Entry::getKey )
                    .findAny()
                    .orElse( null );
        }

        @Override
        public Collection<String> getImports()
        {
            return imports.values();
        }

        @Override
        public void addImport( String type )
        {
            int i = type.lastIndexOf( '.' );
            String n = i > -1 ? type.substring( i + 1 ) : type;
            imports.putIfAbsent( n, type );
        }

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
            Scope scope = new DefaultScope( this );
            CURRENT_SCOPE.set( scope );
            return scope;
        }

        @Override
        public Scope.GlobalScope getGlobalScope()
        {
            return this;
        }

//<editor-fold defaultstate="collapsed" desc="Bindings">
        @Override
        public Object put( String name, Object value )
        {
            return vars.put( name, value );
        }

        @Override
        public void putAll( Map<? extends String, ? extends Object> toMerge )
        {
            vars.putAll( toMerge );
        }

        @Override
        public boolean containsKey( Object key )
        {
            return vars.containsKey( key );
        }

        @Override
        public Object get( Object key )
        {
            return vars.get( key );
        }

        @Override
        public Object remove( Object key )
        {
            return vars.remove( key );
        }

        @Override
        public int size()
        {
            return vars.size();
        }

        @Override
        public boolean isEmpty()
        {
            return vars.isEmpty();
        }

        @Override
        public boolean containsValue( Object value )
        {
            return vars.containsValue( value );
        }

        @Override
        public void clear()
        {
            vars.clear();
        }

        @Override
        public Set<String> keySet()
        {
            return vars.keySet();
        }

        @Override
        public Collection<Object> values()
        {
            return vars.values();
        }

        @Override
        public Set<Entry<String, Object>> entrySet()
        {
            return vars.entrySet();
        }
//</editor-fold>
    }

    public static abstract class ChildScope
            extends AbstractScope
    {

        protected final GlobalScope globalScope;

        protected ChildScope( GlobalScope globalScope )
        {
            this.globalScope = globalScope;
        }

        @Override
        public Job getJob()
        {
            return globalScope.getJob();
        }

        @Override
        public Jcl getJcl()
        {
            return globalScope.getJcl();
        }

        @Override
        public Scope.GlobalScope getGlobalScope()
        {
            return globalScope;
        }

        @Override
        public String resolveType( String type )
        {
            return globalScope.resolveType( type );
        }

        @Override
        public String resolveClass( String clazz )
        {
            return globalScope.resolveClass( clazz );
        }

        @Override
        public Collection<String> getImports()
        {
            return globalScope.getImports();
        }

        @Override
        public void addImport( String type )
        {
            globalScope.addImport( type );
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
            if( vars.containsKey( name ) ) {
                // If we have it then stop here
                vars.put( name, val );
            }
            else if( globalScope.exists( name ) ) {
                // If global then straight to that scope
                globalScope.setVar( name, val );
            }
            else if( !put( name, val ) ) {
                // Recurse down the scopes but if none claim it then put it into ours
                vars.put( name, val );
            }
        }

        @Override
        public void close()
        {
            vars.values().forEach( v -> {
                try {
                    if( v instanceof AutoCloseable ) {
                        ((AutoCloseable) v).close();
                    }
                }
                catch( Exception ex ) {
                    // Ignore
                }
            } );
            vars.clear();
        }

        @Override
        public Scope begin()
        {
            Scope scope = new SubScope( globalScope, this );
            CURRENT_SCOPE.set( scope );
            return scope;
        }

    }

    private static class DefaultScope
            extends ChildScope
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
            }

            return v;
        }

        @Override
        protected boolean put( String name, Object val )
        {
            if( vars.containsKey( name ) ) {
                vars.put( name, val );
                return true;
            }
            return false;
        }

        @Override
        public void close()
        {
            CURRENT_SCOPE.set( globalScope );
            super.close();
        }

    }

    private static class SubScope
            extends ChildScope
    {

        private final ChildScope parentScope;
        private boolean readOnly;

        protected SubScope( GlobalScope globalScope, ChildScope parentScope )
        {
            super( globalScope );
            this.parentScope = parentScope;
        }

        void setReadOnly( boolean readOnly )
        {
            this.readOnly = readOnly;
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
            }

            return v;
        }

        @Override
        protected boolean put( String name, Object val )
        {
            if( !readOnly && vars.containsKey( name ) ) {
                vars.put( name, val );
                return true;
            }
            return parentScope.put( name, val );
        }

        @Override
        public void close()
        {
            CURRENT_SCOPE.set( parentScope );
            super.close();
        }

    }

    public static Scope resourceScope( Scope scope )
    {
        return new ResourceScope( (GlobalScope) scope.getGlobalScope(), (ChildScope) scope );
    }

    public static class ResourceScope
            extends SubScope
    {

        protected ResourceScope( GlobalScope globalScope, ChildScope parentScope )
        {
            super( globalScope, parentScope );
        }

        @Override
        public Scope begin()
        {
            setReadOnly( true );
            return super.begin();
        }

        @Override
        public void close()
        {
            try {
                Iterator<Object> it = vars.values().iterator();
                while( it.hasNext() ) {
                    Object v = it.next();
                    if( v instanceof AutoCloseable ) {
                        try {
                            ((AutoCloseable) v).close();
                        }
                        catch( Exception ex ) {
                        }
                        finally {
                            it.remove();
                        }
                    }
                }
            }
            finally {
                super.close();
            }
        }

    }
}
