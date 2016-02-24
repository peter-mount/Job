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
package onl.area51.job.cluster;

import onl.area51.filesystem.io.OverlayingFileSystemIO;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.FileSystemUtils;
import uk.trainwatch.util.sql.SQL;

/**
 * Call the database for a job definition if it's not present in the file system
 *
 * @author peter
 */
public class ClusterJobFileSystemIO
        extends OverlayingFileSystemIO.Synchronous
{

    private static final Logger LOG = Logger.getLogger( ClusterJobFileSystemIO.class.getName() );

    public static final String DATA_SOURCE = "dataSource";

    private final DataSource dataSource;

    public ClusterJobFileSystemIO( FileSystemIO delegate, Map<String, ?> env )
    {
        super( delegate, Executors.newSingleThreadExecutor() );

        this.dataSource = Objects.requireNonNull( FileSystemUtils.get( env, DATA_SOURCE ), DATA_SOURCE + " not defined" );
    }

    @Override
    protected void retrievePath( String path )
            throws IOException
    {
        if( path == null || path.isEmpty() ) {
            throw new FileNotFoundException( "/" );
        }

        String jobName[] = path.split( "/" );
        if( jobName.length != 2 ) {
            throw new FileNotFoundException( path );
        }

        LOG.log( Level.INFO, () -> "Retrieving " + path );
        try( Connection con = dataSource.getConnection() ) {
            try( PreparedStatement ps = SQL.prepare( con, "SELECT * FROM getJob(?,?)", jobName[0], jobName[1] ) ) {

                String script = SQL.stream( ps, SQL.STRING_LOOKUP )
                        .limit( 1 )
                        .findAny()
                        .orElseThrow( () -> new FileNotFoundException( path ) );

                try( Writer w = new OutputStreamWriter( getDelegate().newOutputStream( path.toCharArray(),
                                                                                       StandardOpenOption.CREATE,
                                                                                       StandardOpenOption.TRUNCATE_EXISTING,
                                                                                       StandardOpenOption.WRITE ),
                                                        StandardCharsets.UTF_8 ) ) {
                    w.write( script );
                }
            }
        }
        catch( SQLException ex ) {
            throw new IOException( "Failed to retrieve " + path, ex );
        }
    }

}