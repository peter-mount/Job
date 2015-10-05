/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.script;

import java.io.Reader;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

/**
 *
 * @author peter
 */
public class JobScriptEngine
        implements ScriptEngine
{

    private final ScriptEngineFactory factory;
    private JobScriptContext ctx = new JobScriptContext();

    JobScriptEngine( ScriptEngineFactory factory )
    {
        this.factory = factory;
    }

    @Override
    public Object eval( String script, ScriptContext context )
            throws ScriptException
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object eval( Reader reader, ScriptContext context )
            throws ScriptException
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object eval( String script )
            throws ScriptException
    {
        return eval( script, ctx );
    }

    @Override
    public Object eval( Reader reader )
            throws ScriptException
    {
        return eval( reader, ctx );
    }

    @Override
    public Object eval( String script, Bindings n )
            throws ScriptException
    {
        return eval( script, new JobScriptContext( ctx, n ) );
    }

    @Override
    public Object eval( Reader reader, Bindings n )
            throws ScriptException
    {
        return eval( reader, new JobScriptContext( ctx, n ) );
    }

    @Override
    public void put( String key, Object value )
    {
        getBindings( ScriptContext.ENGINE_SCOPE ).put( key, value );
    }

    @Override
    public Object get( String key )
    {
        return getBindings( ScriptContext.ENGINE_SCOPE ).get( key );
    }

    @Override
    public Bindings getBindings( int scope )
    {
        return ctx.getBindings( scope );
    }

    @Override
    public void setBindings( Bindings bindings, int scope )
    {
        ctx.setBindings( bindings, scope );
    }

    @Override
    public Bindings createBindings()
    {
        return new JobBindings();
    }

    @Override
    public ScriptContext getContext()
    {
        return ctx;
    }

    @Override
    public void setContext( ScriptContext context )
    {
        ctx = (JobScriptContext) context;
    }

    @Override
    public ScriptEngineFactory getFactory()
    {
        return factory;
    }

}
