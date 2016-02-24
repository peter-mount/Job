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

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;
import onl.area51.filesystem.io.FileSystemIORepository;
import onl.area51.job.jcl.Jcl;
import uk.trainwatch.util.MapBuilder;
import uk.trainwatch.util.sql.Database;

/**
 * An implementation that will retrieve a Job from some central location, a FileSystem or a Database
 *
 * @author peter
 */
@ApplicationScoped
public class ClusterJobRetriever
{

    @Database("job")
    @Inject
    private DataSource dataSource;

    private FileSystem fs;

    @PostConstruct
    void start()
            throws IOException
    {
        fs = FileSystems.newFileSystem( URI.create( "cache://job.control" ),
                                        MapBuilder.<String, Object>builder()
                                        .add( ClusterJobFileSystemIO.DATA_SOURCE, dataSource )
                                        .addBiFunction( FileSystemIORepository.WRAPPER, ClusterJobFileSystemIO::new )
                                        .build() );
    }

    @PreDestroy
    void stop()
    {
        try {
            fs.close();
        }
        catch( IOException ex ) {
        }
    }

    public Path getPath( Jcl jcl )
    {
        return fs.getPath( jcl.getNode(), jcl.getName() );
    }

    /**
     * Retrieve a stored Job
     *
     * @param jcl Jcl to retrieve
     *
     * @return
     *
     * @throws IOException
     */
    public String retrieveJob( Jcl jcl )
            throws IOException
    {
        try( Stream<String> s = Files.lines( getPath( jcl ), StandardCharsets.UTF_8 ) ) {
            return s.collect( Collectors.joining( "\n" ) );
        }
    }

    /**
     * Stores a job
     *
     * @param jcl Jcl to store
     * @param job Full text of the job
     *
     * @throws IOException
     */
    public void storeJob( Jcl jcl, String job )
            throws IOException
    {
        Files.write( getPath( jcl ),
                     Arrays.asList( job ),
                     StandardCharsets.UTF_8,
                     StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE );
    }
}
