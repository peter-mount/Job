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
import java.util.Objects;
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

    private static final Map<Key, PostgreSQLFunction> functions = new ConcurrentHashMap<>();

    public PostgreSQLManager( DataSource dataSource )
            throws SQLException
    {
        try( Connection con = dataSource.getConnection() ) {
            Statement s = con.createStatement();
            try( ResultSet rs = s.executeQuery( "SELECT * FROM config.sqlextension" ) ) {
                while( rs.next() ) {
                    PostgreSQLFunction f = new PostgreSQLFunction( rs );
                    functions.put( new Key( f.getType(), f.getName(), f.getArgc() ), f );
                    LOG.log( Level.FINE, () -> f.getName() + " " + f.getType() + " " + f.getDescription() );
                }
            }
        }
    }

    public PostgreSQLFunction get( PostgreSQLType type, String name, int argc )
    {
        return functions.get( new Key( type, name, argc ) );
    }

    private static class Key
    {

        private final PostgreSQLType type;
        private final String name;
        private final int argc;
        private final int hashCode;

        public Key( PostgreSQLType type, String name, int argc )
        {
            this.type = type;
            this.name = name;
            this.argc = argc;
            hashCode = 23 * (23 * Objects.hash( type )) * Objects.hash( name ) + argc;
        }

        public PostgreSQLType getType()
        {
            return type;
        }

        public int getArgc()
        {
            return argc;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public int hashCode()
        {
            return hashCode;
        }

        @Override
        public boolean equals( Object obj )
        {
            if( obj == null || getClass() != obj.getClass() ) {
                return false;
            }
            final Key other = (Key) obj;
            return Objects.equals( this.name, other.name )
                   && this.argc == other.argc
                   && this.type == other.type;
        }

    }
}
