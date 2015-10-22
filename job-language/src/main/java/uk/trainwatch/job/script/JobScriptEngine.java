/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.script;

import uk.trainwatch.job.util.HashBindings;
import java.io.Reader;
import java.io.StringReader;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import org.antlr.v4.runtime.ANTLRInputStream;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.lang.Compiler;

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
        return eval( new StringReader( script ), context );
    }

    @Override
    public Object eval( Reader reader, ScriptContext context )
            throws ScriptException
    {
        Job job = null;
        try {
            job = Compiler.compile( new ANTLRInputStream( reader ) );

            Scope scope;

            Bindings b = context.getBindings( ScriptContext.ENGINE_SCOPE );
            if( b instanceof Scope ) {
                scope = (Scope) b;
            }
            else {
                scope = Scope.newInstance();
                ctx.setBindings( (Bindings) scope, ScriptContext.ENGINE_SCOPE );
            }

            job.invoke( scope );

            return null;
        }
        catch( Exception ex ) {
            throw new ScriptException( ex );
        }
        finally {
            if( job != null ) {
                job.getJobOutput().cleanup();
            }
        }
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
        return eval( script, new JobScriptContext( null, n ) );
    }

    @Override
    public Object eval( Reader reader, Bindings n )
            throws ScriptException
    {
        return eval( reader, new JobScriptContext( null, n ) );
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
        return new HashBindings();
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
