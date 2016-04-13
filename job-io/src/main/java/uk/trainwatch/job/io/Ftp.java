/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.io;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPFile;
import uk.trainwatch.io.ftp.FTPClient;
import uk.trainwatch.io.ftp.FTPClientBuilder;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.JobListener;
import uk.trainwatch.job.Scope;
import static uk.trainwatch.job.lang.expr.Arithmetic.decode;
import uk.trainwatch.job.lang.expr.ExpressionOperation;
import uk.trainwatch.util.config.Configuration;
import uk.trainwatch.util.config.ConfigurationService;

/**
 * Handles FTP File Transfers
 * <p>
 * @author peter
 */
public class Ftp
        implements AutoCloseable
{

    private static final Logger LOG = Logger.getLogger( Ftp.class.getName() );

    private final String name;
    private final FTPClient client;
    private final Configuration config;
    private final Job job;
    private final JobListener listener;

    public static ExpressionOperation create( ExpressionOperation exp )
    {
        return ( s, a ) -> new Ftp( Objects.toString( decode( exp.invoke( s, a ) ), null ), s );
    }

    private Ftp( String name, Scope scope )
    {
        this.name = name;
        this.job = scope.getJob();

        listener = new JobListener()
        {

            @Override
            public void jobCompleted( Job job, Scope scope )
            {
                try {
                    close();
                }
                catch( IOException ex ) {
                    LOG.log( Level.SEVERE, null, ex );
                }
            }

        };

        config = ConfigurationService.getInstance().getConfiguration( name );

        FTPClientBuilder b = new FTPClientBuilder()
                .logger( s -> LOG.log( Level.INFO, s ) );

        if( config.getBoolean( "ftp.printCommands", false ) ) {
            b.printCommands();
        }

        int controlKeepAliveReplyTimeout = config.getInt( "ftp.controlKeepAliveReplyTimeout", -1 );
        if( controlKeepAliveReplyTimeout > -1 ) {
            b.controlKeepAliveReplyTimeout( controlKeepAliveReplyTimeout );
        }

        int keepAliveTimeout = config.getInt( "ftp.keepAliveTimeout", -1 );
        if( keepAliveTimeout > -1 ) {
            b.keepAliveTimeout( keepAliveTimeout );
        }

        if( config.getBoolean( "ftp.debug", false ) ) {
            b.enableDebugging();
        }
        if( config.getBoolean( "ftp.passive", false ) ) {
            b.passive();
        }
        if( config.getBoolean( "ftp.useEpsvWithIPv4", false ) ) {
            b.useEpsvWithIPv4();
        }

        // Default to binary as thats needed for most
        if( config.getBoolean( "ftp.binary", true ) ) {
            b.binary();
        }
        else {
            b.ascii();
        }
        client = b.build();

        job.addListener( listener );
    }

    @Override
    public void close()
            throws IOException
    {
        try {
            if( client.isConnected() ) {
                client.close();
            }
        }
        finally {
            job.removeListener( listener );
        }
    }

    public void connect()
            throws IOException
    {
        if( !client.isConnected() ) {
//            client.connect( config.getString( "ftp.server" ) );
        }
    }

    public void login()
            throws IOException
    {
        connect();
        if( !client.isLoggedIn() ) {
//            client.login( config.getString( "ftp.username" ), config.getString( "ftp.password" ) );
        }
    }

    public void retrieve( Object v )
            throws IOException
    {
        Object file = decode( v );
        if( file instanceof File ) {
            client.retrieve( (File) file, StandardCopyOption.REPLACE_EXISTING );
        }
        else if( file instanceof Path ) {
            client.retrieve( (Path) file, StandardCopyOption.REPLACE_EXISTING );
        }
        else if( file instanceof FTPFile ) {
            client.retrieve( (FTPFile) file, new File( ((FTPFile) file).getName() ), StandardCopyOption.REPLACE_EXISTING );
        }
        else {
            throw new IllegalArgumentException( file.toString() );
        }
    }

    public void retrieve( FTPFile f, Object v )
            throws IOException
    {
        Object file = decode( v );
        if( file instanceof File ) {
            client.retrieve( f, (File) file, StandardCopyOption.REPLACE_EXISTING );
        }
        else if( file instanceof Path ) {
            client.retrieve( f, (Path) file, StandardCopyOption.REPLACE_EXISTING );
        }
        else {
            throw new IllegalArgumentException( file.toString() );
        }
    }

    public Reader retrieveReader( String remote )
            throws IOException
    {
        return client.retrieveReader( remote );
    }

    public void store( File file )
            throws IOException
    {
        client.store( file );
    }

    public Writer storeWriter( String remote )
            throws IOException
    {
        return client.storeWriter( remote );
    }

    public boolean deleteFile( String pathname )
            throws IOException
    {
        return client.deleteFile( pathname );
    }

    public Collection<String> listNames()
            throws IOException
    {
        return client.listNames();
    }

    public Collection<FTPFile> listDirectories()
            throws IOException
    {
        return client.listDirectories();
    }

    public Collection<FTPFile> listFiles()
            throws IOException
    {
        return client.listFiles();
    }

    public String currentDirectory()
            throws IOException
    {
        return client.printWorkingDirectory();
    }

    public void changeToParentDirectory()
            throws IOException
    {
        client.changeToParentDirectory();
    }

    public void changeDirectory( String pathName )
            throws IOException
    {
        client.changeWorkingDirectory( pathName );
    }

    public boolean makeDirectory( String pathname )
            throws IOException
    {
        return client.makeDirectory( pathname );
    }

}
