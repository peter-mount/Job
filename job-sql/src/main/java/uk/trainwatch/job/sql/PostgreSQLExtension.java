/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.sql;

import uk.trainwatch.util.sql.ResultSetWrapper;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.kohsuke.MetaInfServices;
import uk.trainwatch.job.AbstractScope;
import uk.trainwatch.job.ext.Extension;
import uk.trainwatch.job.lang.block.TypeOp;
import uk.trainwatch.job.lang.expr.ExpressionOperation;
import uk.trainwatch.util.sql.DataSourceProducer;

/**
 * Implements print, println and printf functionality.
 *
 * TODO: Allow this to redirect to a specific PrintWriter
 *
 * @author peter
 */
@MetaInfServices(Extension.class)
public class PostgreSQLExtension
        implements Extension
{

    private static final Logger LOG = Logger.getLogger( PostgreSQLExtension.class.getName() );

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
    public void init()
            throws Exception
    {
        postgreSQLManager = new PostgreSQLManager( DataSourceProducer.getInstance().getDataSource( "rail" ) );
    }

//    @Override
//    public Statement getStatement( String name, ExpressionOperation... args )
//    {
//        LOG.log( Level.INFO, () -> "getStatement(" + name + ") " + args.length + " with " + postgreSQLManager );
//        if( postgreSQLManager == null ) {
//            return null;
//        }
//
//        PostgreSQLFunction f = postgreSQLManager.get( name );
////        if( f != null && args.length == f.getArgc() ) {
////            if( f.isSinglevalue() ) {
////                return ( s, a ) -> {
////                    DataSource ds = DataSourceProducer.getInstance().getDataSource( f.getDataSource() );
////                    try( Connection con = ds.getConnection() ) {
////                        Object argv[] = TypeOp.invokeArguments( s, args );
////                        try( PreparedStatement ps = SQL.prepare( con, "SELECT " + f.getSqlCall(), argv ) ) {
////                            try( ResultSet rs = ps.executeQuery() ) {
////                                if( rs.next() ) {
////                                    return rs.getObject( 1 );
////                                }
////                                else {
////                                    return null;
////                                }
////                            }
////
////                        }
////                    }
////                };
////            }
////        }
//
//        return null;
//    }
    @Override
    public ExpressionOperation getExpression( String name, ExpressionOperation... args )
    {
        // Ignore if there's no database access available
        LOG.log( Level.INFO, () -> "getExpression(" + name + ") " + args.length + " with " + postgreSQLManager );
        if( postgreSQLManager == null ) {
            return null;
        }

        // Lookup the function
        PostgreSQLFunction f = postgreSQLManager.get( name );
        if( f == null || args.length != f.getArgc() || f.isVoid() ) {
            return null;
        }

        // Look up the data source
        DataSource dataSource = getDataSource( f );
        if( dataSource != null && args.length == f.getArgc() ) {
            return getExpression( dataSource, args, f, PostgreSQLFunctionOp::compile );
        }

        return null;
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
    private ExpressionOperation getExpression( DataSource dataSource,
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
            return DataSourceProducer.getInstance().getDataSource( f.getDataSource() );
        }
        catch( IllegalArgumentException ex ) {
            return null;
        }
    }
}
