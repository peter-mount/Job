/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.shell;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.weld.environment.se.Weld;
import uk.trainwatch.util.LoggingUtils;

/**
 *
 * @author peter
 */
public class WeldContainer
        implements Container
{

    private static final Logger LOG = Logger.getLogger( WeldContainer.class.getName() );
    
    private Weld weld;

    @Override
    public void open()
    {
        System.setProperty( "org.jboss.logging.provider", "jdk" );
        System.setProperty( "hazelcast.logging.type", "jdk" );
        LoggingUtils.setLevel( Level.INFO );
        LOG.info( "Initializing environment..." );

        LoggingUtils.setLevel( Level.SEVERE );
        weld = new Weld();

        weld.initialize();

        LoggingUtils.setLevel( Level.INFO );
        LOG.info( "Environment running" );
    }

    @Override
    public void close()
    {
        LOG.info( "Shutting down environment" );

        LoggingUtils.setLevel( Level.SEVERE );
        weld.shutdown();

        LoggingUtils.setLevel( Level.INFO );
    }
}
