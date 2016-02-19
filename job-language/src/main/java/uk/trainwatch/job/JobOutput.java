/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * Maintains the output of a job
 * <p>
 * @author peter
 */
public interface JobOutput
        extends Closeable
{

    /**
     * Returns a path to a file local to the job's output
     *
     * @param name
     * @param more
     * @return
     */
    Path pathOf( String name, String... more );

    /**
     * Set the logging level.
     * <p>
     * @param level
     */
    void setLogLevel( Level level );

    /**
     * Is this Level above the current logging level. When true then logging will be recorded.
     * <p>
     * @param level <p>
     * @return
     */
    boolean isLoggable( Level level );

    /**
     * Write to the log
     * <p>
     * @param l Level of this entry
     * @param s String to log
     * @param e Exception to log
     */
    void log( Level l, String s, Exception e );

    void print( String s );

    void printf( String s, Object... args );

    void addJobOutputArchiver( JobOutputArchiver a );

    void removeJobOutputArchiver( JobOutputArchiver a );

    void addResource( AutoCloseable resource );

    void removeResource( AutoCloseable resource );
}
