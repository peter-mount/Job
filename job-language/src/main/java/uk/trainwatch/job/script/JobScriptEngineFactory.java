/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.script;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 *
 * @author peter
 */
public class JobScriptEngineFactory
        implements ScriptEngineFactory
{

    private static final String ENGINE_NAME = "job";
    private static final String ENGINE_VERSION = "1.0";
    private static final String LANG_NAME = "Job Control";
    private static final String LANG_VERSION = "1.0";
    private static final List<String> EXTENSIONS = Arrays.asList( "job" );
    private static final List<String> MIMETYPES = Arrays.asList( "application/x-job" );
    private static final List<String> NAMES = Arrays.asList( "job", "jobcontrol" );
    private final Map<String, Object> parameters = new ConcurrentHashMap<>();

    public JobScriptEngineFactory()
    {
        parameters.put( ScriptEngine.ENGINE, ENGINE_NAME );
        parameters.put( ScriptEngine.ENGINE_VERSION, ENGINE_VERSION );
        parameters.put( ScriptEngine.NAME, ENGINE_NAME );
        parameters.put( ScriptEngine.LANGUAGE, LANG_NAME );
        parameters.put( ScriptEngine.LANGUAGE_VERSION, LANG_VERSION );
        // THREADING?
    }

    @Override
    public String getEngineName()
    {
        return ENGINE_NAME;
    }

    @Override
    public String getEngineVersion()
    {
        return ENGINE_VERSION;
    }

    @Override
    public List<String> getExtensions()
    {
        return EXTENSIONS;
    }

    @Override
    public List<String> getMimeTypes()
    {
        return MIMETYPES;
    }

    @Override
    public List<String> getNames()
    {
        return NAMES;
    }

    @Override
    public String getLanguageName()
    {
        return LANG_NAME;
    }

    @Override
    public String getLanguageVersion()
    {
        return LANG_VERSION;
    }

    @Override
    public Object getParameter( String key )
    {
        return parameters.get( key );
    }

    @Override
    public String getMethodCallSyntax( String obj, String m, String... args )
    {
        return null;
    }

    @Override
    public String getOutputStatement( String toDisplay )
    {
        return "log \"" + toDisplay.replace( "\"", "\\\"" ) + "\";";
    }

    @Override
    public String getProgram( String... statements )
    {
        StringBuilder sb = new StringBuilder()
                .append( "job \"\";\n{\"" );
        for( String s : statements )
        {
            sb.append( s ).append( ";\n" );
        }
        return sb.append( '}' ).toString();
    }

    @Override
    public ScriptEngine getScriptEngine()
    {
        return new JobScriptEngine( this );
    }

}
