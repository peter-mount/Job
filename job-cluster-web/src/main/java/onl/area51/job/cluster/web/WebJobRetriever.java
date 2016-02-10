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
package onl.area51.job.cluster.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Typed;
import onl.area51.job.cluster.ClusterJobRetriever;
import onl.area51.job.jcl.Jcl;

/**
 * {@link ClusterJobRetriever} implementation that utilises a database for storage
 *
 * @author peter
 */
@ApplicationScoped
@Typed(ClusterJobRetriever.class)
public class WebJobRetriever
        implements ClusterJobRetriever
{

    private static final Logger LOG = Logger.getLogger( WebJobRetriever.class.getName() );

    private final Map<String, Map<String, Entry>> cache = new ConcurrentHashMap<>();

    @Override
    public String retrieveJob( Jcl jcl )
            throws IOException
    {
        return cache.computeIfAbsent( jcl.getNode(), node -> new ConcurrentHashMap<>() )
                .compute( jcl.getName(), ( name, existing ) -> {
                      try {
                          if( existing == null || (existing.getTime() + 300000L) < System.currentTimeMillis() ) {
                              LOG.log( Level.INFO, () -> "Retrieving " + jcl.getNode() + "." + jcl.getName() );
                              URL url = new URL( WebJobListener.getJobRepositoryUrl()
                                                 + "?node=" + jcl.getNode().toLowerCase()
                                                 + "&name=" + jcl.getName().toLowerCase() );
                              URLConnection con = url.openConnection();
                              try( BufferedReader r = new BufferedReader( new InputStreamReader( con.getInputStream() ) ) ) {
                                  return new Entry( r.lines().collect( Collectors.joining( "\n" ) ) );
                              }
                          };
                          return existing;
                      }
                      catch( IOException ex ) {
                          throw new UncheckedIOException( ex );
                      }
                  } )
                .getScript();
    }

    @Override
    public void storeJob( Jcl jcl, String job )
            throws IOException
    {
        throw new UnsupportedOperationException();
    }

    private static class Entry
    {

        private final long time;
        private final String script;

        public Entry( String script )
        {
            this.time = System.currentTimeMillis();
            this.script = script;
        }

        public String getScript()
        {
            return script;
        }

        public long getTime()
        {
            return time;
        }

    }
}
