/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.script;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptContext;

/**
 *
 * @author peter
 */
public class JobScriptContext
        implements ScriptContext
{

    private final Map<Integer, Bindings> bindings = new HashMap<>();

    public JobScriptContext()
    {
        bindings.put( ENGINE_SCOPE, new JobBindings() );
        bindings.put( GLOBAL_SCOPE, new JobBindings() );
    }

    JobScriptContext( JobScriptContext parent, Bindings b )
    {
        bindings.putAll( parent.bindings);
        bindings.put( ENGINE_SCOPE, b );
    }

    @Override
    public void setBindings( Bindings bindings, int scope )
    {
        this.bindings.put( scope, bindings );
    }

    @Override
    public Bindings getBindings( int scope )
    {
        return this.bindings.get( scope );
    }

    @Override
    public void setAttribute( String name, Object value, int scope )
    {
        bindings.computeIfAbsent( scope, s -> new JobBindings() )
                .put( name, value );
    }

    @Override
    public Object getAttribute( String name, int scope )
    {
        return bindings.computeIfAbsent( scope, s -> new JobBindings() )
                .get( name );
    }

    @Override
    public Object removeAttribute( String name, int scope )
    {
        return bindings.computeIfAbsent( scope, s -> new JobBindings() )
                .remove( name );
    }

    @Override
    public Object getAttribute( String name )
    {
        Object a = getAttribute( name, GLOBAL_SCOPE );
        if( a == null )
        {
            a = getAttribute( name, ENGINE_SCOPE );
        }
        return a;
    }

    @Override
    public int getAttributesScope( String name )
    {
        return bindings.entrySet().
                stream().
                sorted( ( a, b ) -> Integer.compare( a.getKey(), b.getKey() ) ).
                filter( b -> b.getValue().containsKey( name ) ).
                map( Map.Entry::getKey ).
                findAny().
                orElse( -1 );
    }

    @Override
    public Writer getWriter()
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Writer getErrorWriter()
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setWriter( Writer writer )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setErrorWriter( Writer writer )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Reader getReader()
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setReader( Reader reader )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Integer> getScopes()
    {
        return new ArrayList<>( bindings.keySet() );
    }

}
