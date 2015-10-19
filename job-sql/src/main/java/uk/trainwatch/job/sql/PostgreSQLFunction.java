/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author peter
 */
public class PostgreSQLFunction
{

    private final long id;
    private final String name;
    private final String dataSource;
    private final int argc;
    private final boolean resultset;
    private final boolean singlevalue;
    private final String sqlCall;
    private final String description;

    public PostgreSQLFunction( ResultSet rs )
            throws SQLException
    {
        id = rs.getLong( "id" );
        name = rs.getString( "name" );
        dataSource = rs.getString( "dataSource" );
        argc = rs.getInt( "argc" );
        resultset = rs.getBoolean( "resultset" );
        singlevalue = rs.getBoolean( "singlevalue" );
        sqlCall = rs.getString( "sqlCall" );
        description = rs.getString( "description" );
    }

    public long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDataSource()
    {
        return dataSource;
    }

    public int getArgc()
    {
        return argc;
    }

    public boolean isResultset()
    {
        return resultset;
    }

    public boolean isSinglevalue()
    {
        return singlevalue;
    }

    public String getSqlCall()
    {
        return sqlCall;
    }

    public String getDescription()
    {
        return description;
    }

}
