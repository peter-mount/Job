/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Function;

/**
 * Bean representing a row in the config.sqlextension database table
 * <p>
 * @author peter
 */
public class PostgreSQLFunction
{

    private final long id;
    private final String name;
    private final String dataSource;
    private final int argc;
    private final PostgreSQLType type;
    private final String sqlCall;
    private final String description;
    private volatile PostgreSQLInvoker expression;

    public PostgreSQLFunction( ResultSet rs )
            throws SQLException
    {
        id = rs.getLong( "id" );
        name = rs.getString( "name" );
        dataSource = rs.getString( "dataSource" );
        argc = rs.getInt( "argc" );
        type = PostgreSQLType.lookup( rs.getString( "type" ) );
        sqlCall = rs.getString( "sqlCall" );
        description = rs.getString( "description" );
    }

    /**
     * The ID in the database
     * <p>
     * @return
     */
    public long getId()
    {
        return id;
    }

    /**
     * The unique name which will map into the language
     * <p>
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * The datasource to use to access this function
     * <p>
     * @return
     */
    public String getDataSource()
    {
        return dataSource;
    }

    /**
     * The number of arguments required
     * <p>
     * @return
     */
    public int getArgc()
    {
        return argc;
    }

    /**
     * The type of this function
     * <p>
     * @return
     */
    public PostgreSQLType getType()
    {
        return type;
    }

    /**
     * SQL Fragment for JDBC. For example {@code darwin.departureboard(?)} where ? is the first argument for that function.
     * <p>
     * @return
     */
    public String getSqlCall()
    {
        return sqlCall;
    }

    /**
     * Description of what this function performs.
     * <p>
     * @return
     */
    public String getDescription()
    {
        return description;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode( this.name );
        hash = 83 * hash + this.argc;
        hash = 83 * hash + Objects.hashCode( this.type );
        return hash;
    }

    @Override
    public boolean equals( Object obj )
    {
        if( obj == null || getClass() != obj.getClass() ) {
            return false;
        }
        final PostgreSQLFunction other = (PostgreSQLFunction) obj;
        return Objects.equals( this.name, other.name )
               && this.argc == other.argc
               && this.type == other.type;
    }

    /**
     * Return's the cached expression for this function
     * <p>
     * @return
     */
    public PostgreSQLInvoker getInvoker()
    {
        return expression;
    }

    public PostgreSQLInvoker computeInvokerIfAbsent( Function<PostgreSQLFunction, PostgreSQLInvoker> mapper )
    {
        if( expression == null ) {
            synchronized( this ) {
                if( expression == null ) {
                    expression = mapper.apply( this );
                }
            }
        }
        return expression;
    }

}
