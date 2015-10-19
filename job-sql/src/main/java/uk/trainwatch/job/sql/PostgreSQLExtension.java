/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.kohsuke.MetaInfServices;
import uk.trainwatch.job.ext.Extension;
import uk.trainwatch.job.lang.block.TypeOp;
import uk.trainwatch.job.lang.expr.ExpressionOperation;
import uk.trainwatch.util.sql.DataSourceProducer;
import uk.trainwatch.util.sql.SQL;
import uk.trainwatch.util.sql.SQLResultSetHandler;

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
        LOG.log( Level.INFO, () -> "getExpression(" + name + ") " + args.length + " with " + postgreSQLManager );
        if( postgreSQLManager == null ) {
            return null;
        }

        PostgreSQLFunction f = postgreSQLManager.get( name );
        if( f == null || args.length != f.getArgc() ) {
            return null;
        }

        // Returns a single value
        if( f.isSinglevalue() && args.length == f.getArgc() ) {
            return ( s, a ) -> {
                DataSource ds = DataSourceProducer.getInstance().getDataSource( f.getDataSource() );
                try( Connection con = ds.getConnection() ) {
                    Object argv[] = TypeOp.invokeArguments( s, args );
                    try( PreparedStatement ps = SQL.prepare( con, "SELECT " + f.getSqlCall(), argv ) ) {
                        try( ResultSet rs = ps.executeQuery() ) {
                            return rs.next() ? rs.getObject( 1 ) : null;
                        }
                    }
                }
            };
        }

        // Returns a result set in one go
        if( f.isResultset() ) {
            return ( s, a ) -> {
                DataSource ds = DataSourceProducer.getInstance().getDataSource( f.getDataSource() );
                try( Connection con = ds.getConnection() ) {
                    Object argv[] = TypeOp.invokeArguments( s, args );
                    try( PreparedStatement ps = SQL.prepare( con, "SELECT * FROM " + f.getSqlCall(), argv ) ) {
                        return SQL.executeQuery( ps, SQLResultSetHandler.toMap() );
                    }
                }
            };
        }

        return null;
    }

}
