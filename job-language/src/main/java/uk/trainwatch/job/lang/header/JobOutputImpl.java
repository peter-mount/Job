/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.header;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import uk.trainwatch.io.IOConsumer;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.JobOutput;
import uk.trainwatch.job.JobOutputArchiver;

/**
 *
 * @author peter
 */
public final class JobOutputImpl
        implements JobOutput
{

    private final Job job;

    private URI fileSystemUri;
    private FileSystem fileSystem;

    private volatile int levelValue = Level.INFO.intValue();
    private final File logFile;
    private final PrintWriter log;

    private Collection<JobOutputArchiver> archivers;

    private Set<AutoCloseable> resources;

    public JobOutputImpl( Job job )
            throws IOException
    {
        this.job = job;

        logFile = File.createTempFile( "job", ".log" );
        logFile.deleteOnExit();
        log = new PrintWriter( logFile );
    }

    private FileSystem getFileSystem()
    {
        if( fileSystem != null )
        {
            try
            {
                fileSystemUri = URI.create( "local://" + job.getJobUUID().toString() + "?deleteOnExit=true" );
                fileSystem = FileSystems.newFileSystem( fileSystemUri, new HashMap<>() );
            } catch( IOException ex )
            {
                throw new UncheckedIOException( ex );
            }
        }
        return fileSystem;
    }

    @Override
    public Path pathOf( String name, String... more )
    {
        return getFileSystem().getPath( name, more );
    }

    @Override
    public void close()
            throws IOException
    {
        try
        {
            log.close();
            archive();
            logFile.delete();
        }
        finally
        {
            closeAll( archivers );
            closeAll( resources );
            if( fileSystem != null )
            {
                fileSystem.close();
            }
        }
    }

    private void closeAll( Collection<? extends AutoCloseable> c )
    {
        if( c != null && !c.isEmpty() )
        {
            c.forEach( a
                    -> 
                    {
                        try
                        {
                            a.close();
                        } catch( Exception ex )
                        {
                            // Ignore as we are closing down
                        }
            } );
        }
    }

    private void archive()
            throws IOException
    {
        if( archivers == null || archivers.isEmpty() )
        {
            return;
        }

        archive( logFile.toPath() );

        Files.walk( pathOf( "/" ), FileVisitOption.FOLLOW_LINKS )
                .filter( p -> Files.isRegularFile( p, LinkOption.NOFOLLOW_LINKS ) )
                .forEach( this::archive );
    }

    private void archive( Path p )
    {
        archivers.forEach( IOConsumer.guard( a -> a.archive( p ) ) );
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
    public void print( String s )
    {
        log.println( s );
    }

    @Override
    public void printf( String s, Object... args )
    {
        log.printf( s.endsWith( "\n" ) ? s : (s + "\n"), args );
    }

    @Override
    public void addJobOutputArchiver( JobOutputArchiver a )
    {
        if( archivers == null )
        {
            archivers = new ArrayList<>();
        }
        archivers.add( a );
    }

    @Override
    public void removeJobOutputArchiver( JobOutputArchiver a )
    {
        if( archivers != null )
        {
            archivers.remove( a );
            if( archivers.isEmpty() )
            {
                archivers = null;
            }
        }
    }

    @Override
    public void addResource( AutoCloseable resource )
    {
        if( resources == null )
        {
            resources = new HashSet<>();
        }
        resources.add( resource );
    }

    @Override
    public void removeResource( AutoCloseable resource )
    {
        if( resources != null )
        {
            resources.remove( resource );
        }
    }

}
