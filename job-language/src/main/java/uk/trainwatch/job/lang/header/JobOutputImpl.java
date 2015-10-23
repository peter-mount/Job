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
import java.util.Objects;
import java.util.logging.Level;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.JobOutput;
import uk.trainwatch.job.JobOutputArchiver;

/**
 *
 * @author peter
 */
public final class JobOutputImpl
        implements JobOutput,
                   Closeable
{

    private final Job job;

    private volatile int levelValue = Level.INFO.intValue();
    private final File logFile;
    private final PrintWriter log;

    private final Map<String, File> permFiles = new HashMap<>();
    private final Map<String, File> tempFiles = new HashMap<>();
    private Collection<JobOutputArchiver> archivers;

    public JobOutputImpl( Job job )
            throws IOException
    {
        this.job = job;

        logFile = File.createTempFile( job.getId(), ".log" );
        logFile.deleteOnExit();
        log = new PrintWriter( logFile );
    }

    @Override
    public void close()
            throws IOException
    {
        try {
            log.close();
        }
        finally {
            if( archivers != null ) {
                try {
                    archive();
                }
                finally {
                    for( JobOutputArchiver a: archivers ) {
                        try {
                            a.close();
                        }
                        catch( IOException ex ) {

                        }
                    }
                }
            }
        }
    }

    private void archive()
            throws IOException
    {
        Collection<Map.Entry<String, File>> files = new ArrayList<>( tempFiles.entrySet() );
        files.addAll( permFiles.entrySet() );

        for( JobOutputArchiver a: archivers ) {

            a.archiveLog( job, logFile );

            for( Map.Entry<String, File> e: files ) {
                a.archive( e.getKey(), e.getValue() );
            }
        }
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
    public void delete( File file )
    {
        if( file != null ) {
            permFiles.entrySet()
                    .stream()
                    .filter( e -> file.equals( e.getValue() ) )
                    .findAny()
                    .ifPresent( e -> {
                        File f = e.getValue();
                        permFiles.remove( e.getKey() );
                        deleteImpl( f );
                    } );

            tempFiles.entrySet()
                    .stream()
                    .filter( e -> file.equals( e.getValue() ) )
                    .findAny()
                    .ifPresent( e -> {
                        File f = e.getValue();
                        tempFiles.remove( e.getKey() );
                        deleteImpl( f );
                    } );
        }
    }

    @Override
    public void delete( String name )
    {
        deleteImpl( permFiles.remove( name ) );
        deleteImpl( tempFiles.remove( name ) );
    }

    private void deleteImpl( File f )
    {
        if( f != null ) {
            f.delete();
        }
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
        Objects.requireNonNull( prefix, "Prefix is mandatory" );

        return tempFiles.computeIfAbsent( prefix + suffix, n -> {
            try {
                // File.createTempFile will throw an IllegalArgumentException if prefix is under 3 characters so pad out with _
                String p = prefix;
                if( p.length() < 3 ) {
                    p = (p + "___").substring( 0, 3 );
                }

                File f = File.createTempFile( p, suffix );
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

    @Override
    public void addJobOutputArchiver( JobOutputArchiver a )
    {
        if( archivers == null ) {
            archivers = new ArrayList<>();
        }
        archivers.add( a );
    }

    @Override
    public void removeJobOutputArchiver( JobOutputArchiver a )
    {
        if( archivers != null ) {
            archivers.remove( a );
            if( archivers.isEmpty() ) {
                archivers = null;
            }
        }
    }

}
