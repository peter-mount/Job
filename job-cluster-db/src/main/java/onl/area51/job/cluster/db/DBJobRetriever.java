/*
 * Copyright 2016 peter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package onl.area51.job.cluster.db;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.sql.DataSource;
import onl.area51.job.cluster.ClusterJobRetriever;
import org.antlr.v4.runtime.ANTLRInputStream;
import uk.trainwatch.io.IOFunction;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.lang.Compiler;
import uk.trainwatch.util.sql.Database;
import uk.trainwatch.util.sql.SQL;

/**
 *
 * @author peter
 */
@ApplicationScoped
@Typed(ClusterJobRetriever.class)
public class DBJobRetriever
        implements ClusterJobRetriever
{

    @Database("job")
    @Inject
    private DataSource dataSource;

    @Override
    public Job retrieveJob( String cluster, String name )
            throws IOException
    {
        try( Connection con = dataSource.getConnection() ) {
            try( PreparedStatement ps = SQL.prepare( con,
                                                     "SELECT d.outpt, d.decl,d.text,d.version"
                                                     + " FROM jobdef d"
                                                     + " INNER JOIN job j ON d.id=j.id"
                                                     + " INNER JOIN cluster c ON j.clusterid=c.id"
                                                     + " WHERE c.name=? AND j.name=?"
                                                     + " ORDER BY d.version DESC"
                                                     + " LIMIT 1",
                                                     cluster, name ) ) {
                return SQL.stream( ps, rs -> mapper( cluster, name, rs ) )
                        .limit( 1 )
                        .map( StringReader::new )
                        .map( IOFunction.guard( ANTLRInputStream::new ) )
                        .map( Compiler::compile )
                        .findAny()
                        .orElse( null );
            }
        }
        catch( SQLException |
               UncheckedIOException ex ) {
            throw new IOException( ex );
        }
    }

    private String mapper( String cluster, String name, ResultSet rs )
            throws SQLException
    {
        StringBuilder sb = new StringBuilder()
                .append( "job \"" ).append( name ).append( "\";\n" )
                .append( "run as \"" ).append( cluster ).append( "\";\n" );

        append( sb, "output", rs.getString( "outpt" ) );
        append( sb, "declare", rs.getString( "decl" ) );
        append( sb, null, rs.getString( "text" ) );
        return sb.toString();
    }

    private void append( StringBuilder sb, String prefix, String s )
    {
        if( s != null ) {
            if( prefix != null ) {
                sb.append( prefix ).append( ' ' );
            }
            sb.append( "{\n" ).append( s ).append( "\n}\n" );
        }
    }
}
