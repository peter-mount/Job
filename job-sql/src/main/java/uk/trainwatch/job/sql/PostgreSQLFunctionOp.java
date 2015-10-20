/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.sql;

import uk.trainwatch.util.sql.ConnectionWrapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.trainwatch.job.AbstractScope;
import uk.trainwatch.job.Scope;
import uk.trainwatch.util.sql.SQL;
import uk.trainwatch.util.sql.SQLResultSetHandler;

/**
 * A set of PostgreSQL functions that map to language functions
 * <p>
 * @author peter
 */
public abstract class PostgreSQLFunctionOp
        implements PostgreSQLInvoker
{

    private static final Logger LOG = Logger.getLogger( PostgreSQLStatementOp.class.getName() );
    private final String sqlCall;

    public static synchronized PostgreSQLInvoker compile( PostgreSQLFunction f )
    {
        switch( f.getType() ) {
            case SINGLE:
                return new Single( f );

            case TABLE:
                return new Result( f );

            default:
                return null;
        }
    }

    protected PostgreSQLFunctionOp( PostgreSQLFunction f, String sqlCall )
    {
        this.sqlCall = sqlCall;
    }

    protected final String getSqlCall()
    {
        return sqlCall;
    }

    /**
     * Invoker to return a single value as the result.
     */
    private static class Single
            extends PostgreSQLFunctionOp
    {

        public Single( PostgreSQLFunction f )
        {
            super( f, "SELECT " + f.getSqlCall() );
        }

        @Override
        public Object invoke( Scope s, Connection con, Object... args )
                throws Exception
        {
            LOG.log( Level.FINE, () -> "Invoking " + getSqlCall() );
            try( PreparedStatement ps = SQL.prepare( con, getSqlCall(), args ) ) {
                try( ResultSet rs = ps.executeQuery() ) {
                    return rs.next() ? rs.getObject( 1 ) : null;
                }
            }
        }

    }

    /**
     * Invoker to return a result as a collection of maps, one map per row
     */
    private static class Result
            extends PostgreSQLFunctionOp
    {

        public Result( PostgreSQLFunction f )
        {
            super( f, "SELECT * FROM " + f.getSqlCall() );
        }

        /**
         * We can run within a try-resources block
         * <p>
         * @return
         */
        @Override
        public boolean isResourcable()
        {
            return true;
        }

        @Override
        public Object invoke( Scope s, Connection con, Object... args )
                throws Exception
        {
            LOG.log( Level.FINE, () -> "Invoking " + getSqlCall() );
            if( s instanceof AbstractScope.ResourceScope ) {
                ConnectionWrapper c = (ConnectionWrapper) con;
                PreparedStatement ps = c.setPreparedStatement( SQL.prepare( con, getSqlCall(), args ) );
                return c.setResultSet( ps.executeQuery() );
            }
            else {
                try( PreparedStatement ps = SQL.prepare( con, getSqlCall(), args ) ) {
                    return SQL.executeQuery( ps, SQLResultSetHandler.toMap() );
                }
            }
        }

    }

}
