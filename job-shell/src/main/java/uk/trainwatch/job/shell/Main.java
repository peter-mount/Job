/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.shell;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.lang.Compiler;
import uk.trainwatch.util.LoggingUtils;
import uk.trainwatch.util.sql.DataSourceProducer;

/**
 *
 * @author peter
 */
@ApplicationScoped
public class Main
{

    private static final Logger LOG = Logger.getLogger( "main" );

    private static final Semaphore semaphore = new Semaphore( 0 );
    private static CommandLine cmd;

    private static boolean cdi;
    private static boolean daemon;
    private static boolean cluster;
    private static String clusterName;

    public static void main( String... args )
            throws Exception
    {
        configure( args );

        // Compile any scripts. This will allow for any syntax errors to be shown immediately rather than after startup
        List<Job> jobs = ((List<String>) cmd.getArgList())
                .stream()
                .map( Compiler::fromFile )
                .map( Compiler::compile )
                .collect( Collectors.toList() );

        if( jobs.isEmpty() && !daemon ) {
            LOG.log( Level.SEVERE, "No scripts to run and not in daemon mode" );
            System.exit( 1 );
        }

        try( Container c = boot() ) {
            c.open();

            jobs.forEach( Main::run );

            if( daemon ) {
                semaphore.acquire();
            }
        }
        catch( RuntimeException ex ) {
            LOG.log( Level.SEVERE, ex, () -> "Fatal exception" );
        }
    }

    private static void run( Job job )
    {
        LOG.log( Level.INFO, () -> "Job: " + job.getId() );

        try( Scope scope = Scope.newInstance( Logger.getAnonymousLogger() ) ) {
            job.invoke( scope );
        }
        catch( Exception ex ) {
            throw new RuntimeException( ex );
        }
    }

    private static void configure( String[] args )
            throws ParseException,
                   IOException
    {
        Options options = new Options()
                .addOption( null, "cdi", false, "Enable CDI support" )
                .addOption( null, "cluster", true, "Enable cluster support (forces cdi & daemon)" )
                .addOption( null, "daemon", false, "Continue running once all scripts have run" )
                .addOption( null, "database", true, "Database config" );

        cmd = new GnuParser().parse( options, args );

        clusterName = cmd.hasOption( "cluster" ) ? cmd.getOptionValue( "cluster" ) : null;
        cluster = clusterName != null && !clusterName.isEmpty();

        cdi = cluster || cmd.hasOption( "cdi" );
        daemon = cluster || cmd.hasOption( "daemon" );

        if( cmd.hasOption( "database" ) ) {
            DataSourceProducer.setFactory( read( cmd.getOptionValue( "database" ) ) );
            DataSourceProducer.setUseJndi( false );
        }
    }

    private static Properties read( String name )
            throws IOException
    {
        try( Reader r = new FileReader( name ) ) {
            Properties p = new Properties();
            p.load( r );
            return p;
        }
    }

    private static Container boot()
            throws InterruptedException
    {
        Container c = null;

        if( cdi ) {
            c = Container.andThen( c, new WeldContainer() );
        }

        if( cluster ) {
            c = Container.andThen( c, new ClusterContainer( clusterName ) );
        }

        return c == null ? Container.NOP : c;
    }

    public static void exit()
    {
        semaphore.release( 10 );
    }
}
