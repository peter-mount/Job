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
import uk.trainwatch.job.Scope;
import uk.trainwatch.util.sql.SQL;

/**
 *
 * @author peter
 */
public abstract class PostgreSQLStatementOp
        implements PostgreSQLInvoker
{

    private static final Logger LOG = Logger.getLogger( PostgreSQLStatementOp.class.getName() );
    private final String sqlCall;

    public static synchronized PostgreSQLInvoker compile( PostgreSQLFunction f )
    {
        switch( f.getType() ) {
            case STATEMENT:
                return new Simple( f );

            default:
                return null;
        }
    }

    protected PostgreSQLStatementOp( PostgreSQLFunction f, String sqlCall )
    {
        this.sqlCall = sqlCall;
    }

    protected final String getSqlCall()
    {
        return sqlCall;
    }

    /**
     * A Simple statement. Use for functions that return nothing
     */
    private static class Simple
            extends PostgreSQLStatementOp
    {

        public Simple( PostgreSQLFunction f )
        {
            super( f, "SELECT " + f.getSqlCall() );
        }

        @Override
        public Object invoke( Scope s, Connection con, Object... args )
                throws Exception
        {
            LOG.log( Level.FINE, () -> "Invoking " + getSqlCall() );
            try( PreparedStatement ps = SQL.prepare( con, getSqlCall(), args ) ) {
                // Note: Execute and ditch any results or update counts
                boolean r = ps.execute();
                while( r || ps.getUpdateCount() != -1 ) {
                    if( r ) {
                        ResultSet rs = ps.getResultSet();
                        if( rs != null ) {
                            rs.close();
                        }
                    }
                    r = ps.getMoreResults();
                }
                return null;
            }
        }

    }
}
