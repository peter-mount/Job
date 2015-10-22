/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.header;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.JobOutput;

/**
 *
 * @author peter
 */
public final class JobOutputImpl
        implements JobOutput,
                   Closeable
{

    private volatile int levelValue = Level.INFO.intValue();
    private final File logFile;
    private final PrintWriter log;

    private final Map<String, File> permFiles = new HashMap<>();
    private final Map<String, File> tempFiles = new HashMap<>();

    public JobOutputImpl( Job job )
            throws IOException
    {
        logFile = createTempFile( job.getId(), ".log" );

        log = new PrintWriter( logFile );
    }

    @Override
    public void close()
            throws IOException
    {
        log.close();
    }

    /**
     * Cleanup the output. Client code must call this if the VM is long lived otherwise the temp files will remain on disk
     */
    @Override
    public void cleanup()
    {
        logFile.delete();
        tempFiles.values().forEach( File::delete );
        tempFiles.clear();
        permFiles.clear();
    }

    @Override
    public File getFile( String name )
    {
        return permFiles.get( name );
    }

    @Override
    public File createFile( String name )
    {
        return permFiles.computeIfAbsent( name, File::new );
    }

    @Override
    public void addFile( File file )
    {
        permFiles.putIfAbsent( file.getName(), file );
    }

    @Override
    public File createTempFile( String prefix, String suffix )
    {
        return tempFiles.computeIfAbsent( prefix + "." + suffix, n -> {
            try {
                File f = File.createTempFile( prefix, suffix );
                f.deleteOnExit();
                return f;
            }
            catch( IOException e ) {
                throw new UncheckedIOException( e );
            }
        } );
    }

    @Override
    public void addTempFile( File file )
    {
        tempFiles.putIfAbsent( file.getName(), file );
        file.deleteOnExit();
    }

    @Override
    public Collection<File> getFiles()
    {
        List<File> l = new ArrayList<>( permFiles.values() );
        l.addAll( tempFiles.values() );
        return l;
    }

    @Override
    public File getLog()
    {
        return logFile;
    }

    @Override
    public void log( Level l, String s, Exception e )
    {
        log.printf( "%1$tF %1$tT %2$-7s %3$s\n", System.currentTimeMillis(), l, s );
    }

    @Override
    public boolean isLoggable( Level level )
    {
        return level.intValue() >= levelValue;
    }

    @Override
    public void setLogLevel( Level level )
    {
        levelValue = (level == null ? Level.INFO : level).intValue();
    }

}
