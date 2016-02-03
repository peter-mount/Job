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
import onl.area51.job.jcl.Jcl;
import uk.trainwatch.util.sql.Database;
import uk.trainwatch.util.sql.SQL;

/**
 * {@link ClusterJobRetriever} implementation that utilises a database for storage
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
    public String retrieveJob( Jcl jcl )
            throws IOException
    {
        try( Connection con = dataSource.getConnection() ) {
            try( PreparedStatement ps = SQL.prepare( con, "SELECT * FROM getJob(?,?)", jcl.getNode(), jcl.getName() ) ) {
                return SQL.stream( ps, SQL.STRING_LOOKUP )
                        .limit( 1 )
                        .findAny()
                        .orElse( null );
            }
        }
        catch( SQLException |
               UncheckedIOException ex ) {
            throw new IOException( ex );
        }
    }

    @Override
    public void storeJob( Jcl jcl, String job )
            throws IOException
    {
        try( Connection con = dataSource.getConnection() ) {
            SQL.executeFunction( con, "storeJob(?,?,?)", jcl.getNode(), jcl.getName(), job );
        }
        catch( SQLException |
               UncheckedIOException ex ) {
            throw new IOException( ex );
        }
    }

}
