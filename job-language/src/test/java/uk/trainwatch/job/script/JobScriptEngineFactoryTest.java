/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author peter
 */
public class JobScriptEngineFactoryTest
{

    private static ScriptEngineManager scriptEngineManager;

    @BeforeClass
    public static void beforeClass()
    {
        scriptEngineManager = new ScriptEngineManager();
    }

    @Test
    public void getEngineByName()
            throws Exception
    {
        ScriptEngine engine = scriptEngineManager.getEngineByName( "job" );
        Assert.assertNotNull( engine );

        Assert.assertEquals( "job", engine.getFactory().getEngineName() );
    }

    @Test
    public void getEngineByExtension()
            throws Exception
    {
        ScriptEngine engine = scriptEngineManager.getEngineByExtension( "job" );
        Assert.assertNotNull( engine );

        Assert.assertEquals( "job", engine.getFactory().getEngineName() );
    }

    @Test
    public void getEngineByMimeType()
            throws Exception
    {
        ScriptEngine engine = scriptEngineManager.getEngineByMimeType( "application/x-job" );
        Assert.assertNotNull( engine );

        Assert.assertEquals( "job", engine.getFactory().getEngineName() );
    }

}
