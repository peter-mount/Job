/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang;

import java.util.logging.Logger;
import uk.trainwatch.job.AbstractScope;

/**
 * Dummy class implementing a closable resource
 * <p>
 * @author peter
 */
public class TempResource
        implements AutoCloseable
{

    private final Logger log;

    public TempResource()
    {
        log = AbstractScope.getCurrentScope().getLogger();
        log.info( "Open" );
    }

    public void run()
    {
        log.info( "Run" );
    }

    @Override
    public void close()
            throws Exception
    {
        log.info( "Close" );
    }

}
