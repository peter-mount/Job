/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.sql;

import uk.trainwatch.util.sql.ResultSetWrapper;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.kohsuke.MetaInfServices;
import uk.trainwatch.job.AbstractScope;
import uk.trainwatch.job.ext.Extension;
import uk.trainwatch.job.ext.ExtensionType;
import uk.trainwatch.job.lang.Operation;
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.block.TypeOp;
import uk.trainwatch.job.lang.expr.ExpressionOperation;
import uk.trainwatch.util.sql.DataSourceProducer;

/**
 * Implements print, println and printf functionality.
 * <p>
 * TODO: Allow this to redirect to a specific PrintWriter
 *
 * @author peter
 */
@MetaInfServices(Extension.class)
public class PostgreSQLExtension
        implements Extension
{

    private static final Logger LOG = Logger.getLogger( PostgreSQLExtension.class.getName() );
    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();
    private PostgreSQLManager postgreSQLManager;

    @Override
    public String getName()
    {
        return "PostgreSQL";
    }

    @Override
    public String getVersion()
    {
        return "1.0";
    }

    @Override
    public synchronized void init()
            throws Exception
    {
        if( postgreSQLManager == null ) {
            postgreSQLManager = new PostgreSQLManager( DataSourceProducer.getInstance().getDataSource( "rail" ) );
        }
    }

    /**
     * Handle statements
     * <p>
     * @param name
     * @param args
     *             <p>
     * @return
     */
    @Override
    public Statement getStatement( String name, ExpressionOperation... args )
    {
        LOG.log( Level.FINE, () -> "getStatement(" + name + ") " + args.length + " with " + postgreSQLManager );

        PostgreSQLFunction f = lookup( ExtensionType.STATEMENT, name, args );
        if( f == null ) {
            return null;
        }

        // Look up the data source
        DataSource dataSource = getDataSource( f );
        if( dataSource != null ) {
            return ( s, a ) -> getExpression( dataSource, args, f, PostgreSQLStatementOp::compile ).invoke( s, a );
        }

        return null;
    }

    /**
     * Handle standard functions that return a value
     * <p>
     * @param name
     * @param args
     *             <p>
     * @return
     */
    @Override
    public ExpressionOperation getExpression( String name, ExpressionOperation... args )
    {
        // Ignore if there's no database access available
        LOG.log( Level.FINE, () -> "getExpression(" + name + ") " + args.length + " with " + postgreSQLManager );

        PostgreSQLFunction f = lookup( ExtensionType.FUNCTION, name, args );
        if( f == null ) {
            return null;
        }

        // Look up the data source
        DataSource dataSource = getDataSource( f );
        if( dataSource != null ) {
            return ( s, a ) -> getExpression( dataSource, args, f, PostgreSQLFunctionOp::compile ).invoke( s, a );
        }

        return null;
    }

    /**
     * Lookup the function.
     * <p>
     * This will filter out all functions that don't match the number of arguments and those who's datasource is not available.
     * <p>
     * @param name
     * @param args
     *             <p>
     * @return
     */
    private PostgreSQLFunction lookup( ExtensionType type, String name, ExpressionOperation... args )
    {
        if( postgreSQLManager == null ) {
            return null;
        }

        // Lookup the function
        int argc = args == null ? 0 : args.length;

        return PostgreSQLType.getGetTypes( type )
                .stream()
                .map( t -> postgreSQLManager.get( t, name, argc ) )
                .filter( Objects::nonNull )
                .filter( pf -> pf.getArgc() == argc )
                .filter( pf -> getDataSource( pf ) != null )
                .findAny()
                .orElse( null );
    }

    /**
     * Create the expression for this operation
     * <p>
     * @param dataSource DataSource
     * @param args       arguments as expressions
     * @param f          function to invoke
     * @param getInvoker function to create the PostgreSQLInvoker
     * <p>
     * @return Expression
     */
    private Operation getExpression( DataSource dataSource,
                                     ExpressionOperation[] args,
                                     PostgreSQLFunction f,
                                     Function<PostgreSQLFunction, PostgreSQLInvoker> getInvoker )
    {
        PostgreSQLInvoker invoker = f.computeInvokerIfAbsent( getInvoker );

        if( invoker == null ) {
            return null;
        }

        if( invoker.isResourcable() ) {
            // Resourcable invocation
            return ( s, a ) -> {
                Object argv[] = TypeOp.invokeArguments( s, args );

                if( s instanceof AbstractScope.ResourceScope ) {
                    final ResultSetWrapper w = new ResultSetWrapper( dataSource );
                    try {
                        Object obj = invoker.invoke( s, w.getConnection(), argv );
                        if( obj instanceof ResultSet ) {
                            if( w.isValid( (ResultSet) obj ) ) {
                                return w;
                            }
                            throw new IllegalStateException( "Implementation does not support resources but declares it does" );
                        }
                        throw new IllegalStateException( "Implementation declares resource support but returns non-result set" );
                    }
                    catch( Exception ex ) {
                        w.close();
                        throw ex;
                    }
                }
                else {
                    try( Connection con = dataSource.getConnection() ) {
                        return invoker.invoke( s, con, argv );
                    }
                }
            };
        }

        // Non-resourcable invocation
        return ( s, a ) -> {
            Object argv[] = TypeOp.invokeArguments( s, args );
            try( Connection con = dataSource.getConnection() ) {
                return invoker.invoke( s, con, argv );
            }
        };
    }

    /**
     * Lookup the datasource. If it's not available then return null if not available in this instance
     * <p>
     * @param f function definition
     * <p>
     * @return DataSource or null
     */
    private DataSource getDataSource( PostgreSQLFunction f )
    {
        try {
            return dataSources.computeIfAbsent( f.getDataSource(), DataSourceProducer.getInstance()::getDataSource );
        }
        catch( IllegalArgumentException ex ) {
            // No datasource so return null
            return null;
        }
    }
}
