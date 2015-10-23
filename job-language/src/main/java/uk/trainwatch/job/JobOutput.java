/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job;

import java.io.File;
import java.util.Collection;
import java.util.logging.Level;

/**
 * Maintains the output of a job
 * <p>
 * @author peter
 */
public interface JobOutput
{

    /**
     * Cleanup the output. Client code must call this if the VM is long lived otherwise the temp files will remain on disk
     */
    void cleanup();

    /**
     * Create a permanent file for use by the job.
     * <p>
     * This file will not be deleted when the job completes.
     * <p>
     * @param name
     *             <p>
     * @return
     */
    File createFile( String name );

    /**
     * Create a temporary file.
     * <p>
     * This file will be deleted once the job completes.
     * <p>
     * @param name
     *             <p>
     * @return
     */
    default File createTempFile( String name )
    {
        int i = name.lastIndexOf( '.' );
        if( i > -1 ) {
            return createTempFile( name.substring( 0, i ), name.substring( i ) );
        }
        return createTempFile( name, null );
    }

    /**
     * Create a temporary file.
     * <p>
     * This file will be deleted once the job completes.
     * <p>
     * @param prefix
     * @param suffix
     *               <p>
     * @return
     */
    File createTempFile( String prefix, String suffix );

    /**
     * Retrieve the named file
     * <p>
     * @param name
     *             <p>
     * @return
     */
    File getFile( String name );

    /**
     * Add a file to the permanent list.
     * <p>
     * This file will not be deleted when the job completes.
     * <p>
     * @param file
     */
    void addFile( File file );

    /**
     * Add a file to the temporary list.
     * <p>
     * This file will be deleted once the job completes.
     * <p>
     * @param file
     */
    void addTempFile( File file );

    /**
     * Delete a file. Once deleted it's no longer part of the job output.
     * <p>
     * @param file File
     */
    void delete( File file );

    /**
     * Delete a file. Once deleted it's no longer part of the job output.
     * <p>
     * @param name file name
     */
    void delete( String name );

    /**
     * Return a collection of all files managed by this job.
     * <p>
     * @return
     */
    Collection<File> getFiles();

    /**
     * Retrieve the log file
     * <p>
     * @return
     */
    File getLog();

    /**
     * Set the logging level.
     * <p>
     * @param level
     */
    void setLogLevel( Level level );

    /**
     * Is this Level above the current logging level. When true then logging will be recorded.
     * <p>
     * @param level
     *              <p>
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
}
