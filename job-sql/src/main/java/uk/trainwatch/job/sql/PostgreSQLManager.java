/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Manages the available functions stored in the database
 * <p>
 * @author peter
 */
public class PostgreSQLManager
{

    private static final Logger LOG = Logger.getLogger( PostgreSQLManager.class.getName() );

    private static final Map<String, PostgreSQLFunction> functions = new ConcurrentHashMap<>();

    public PostgreSQLManager( DataSource dataSource )
            throws SQLException
    {
        try( Connection con = dataSource.getConnection() ) {
            Statement s = con.createStatement();
            try( ResultSet rs = s.executeQuery( "SELECT * FROM config.sqlextension" ) ) {
                while( rs.next() ) {
                    PostgreSQLFunction f = new PostgreSQLFunction( rs );
                    functions.put( f.getName(), f );
                    LOG.log( Level.FINE, () -> f.getName() + " " + f.isResultset() + " " + f.isSinglevalue() + " " + f.getDescription() );
                }
            }
        }
    }

    public PostgreSQLFunction get( String name )
    {
        return functions.get( name );
    }
}
