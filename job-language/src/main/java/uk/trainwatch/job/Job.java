/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import uk.trainwatch.job.lang.Statement;

/**
 *
 * @author peter
 */
public interface Job
        extends Statement,
                Closeable
{

    UUID getJobUUID();

    JobOutput getJobOutput();

    @Override
    default void close()
            throws IOException
    {
        getJobOutput().close();
    }

    void addListener( JobListener l );

    void removeListener( JobListener l );

    void fire( Consumer<JobListener> c );

    default void log( Supplier<String> c )
    {
        log( Level.INFO, null, c );
    }

    default void log( Level l, Supplier<String> c )
    {
        log( l, null, c );
    }

    default void log( Level l, Exception e, Supplier<String> c )
    {
        if( isLoggable( l ) )
        {
            log( l, c.get(), e );
        }
    }

    default void log( String s )
    {
        log( Level.INFO, s, null );
    }

    default void log( Level l, String s )
    {
        log( l, s, null );
    }

    default void log( String s, Exception e )
    {
        log( e == null ? Level.INFO : Level.SEVERE, s, e );
    }

    default void log( Exception e )
    {
        log( Level.SEVERE, null, e );
    }

    boolean isLoggable( Level level );

    void log( Level l, String s, Exception e );
}
