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

    File createFile( String name );

    File createTempFile( String prefix, String suffix );

    File getFile( String name );

    void addFile( File file );

    void addTempFile( File file );

    Collection<File> getFiles();

    File getLog();

    void setLogLevel( Level level );

    boolean isLoggable( Level level );

    void log( Level l, String s, Exception e );
}
