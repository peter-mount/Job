/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

/**
 * An archiver to run when a job completes
 * <p>
 * @author peter
 */
public interface JobOutputArchiver
        extends Closeable
{

    default void archiveLog( Job job, Path logFile )
            throws IOException
    {
        archive( logFile );
    }

    /**
     * Add a file to the archive
     * <p>
     * @param name Name of file
     * @param file File to archive
     * <p>
     * @throws IOException on error
     */
    default void archive( Path file )
            throws IOException
    {
        archive( file.toString(), file );
    }

    void archive( String name, Path file )
            throws IOException;

    /**
     * Normalise the archive name to remove directory paths
     * <p>
     * @param name <p>
     * @return
     */
    default String normalise( String name )
    {
        // Remove any path's from the name
        int i = name.lastIndexOf( '/' );
        String n = i > -1 ? name.substring( i + 1 ) : name;

        // Remvoe trailing _ which we add to temp files when prefix is < 3 characters (Limit imposed in File.createTempFile)
        while( n.endsWith( "_" ) )
        {
            n = n.substring( 0, n.length() - 1 );
        }
        if( n.isEmpty() )
        {
            n = "_";
        }

        return n;
    }
}
