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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import static onl.area51.job.cluster.Constants.*;

/**
 * Handles initialisation from inside a servlet container
 *
 * @author peter
 */
@WebListener
public class ClusterContextListener
        implements ServletContextListener
{

    private static final Logger LOG = Logger.getLogger( ClusterContextListener.class.getName() );

    private static String clusterName = System.getProperty( CLUSTER_NAME );
    private static int threadCount = 1;

    @Override
    public void contextInitialized( ServletContextEvent sce )
    {
        ServletContext cxt = sce.getServletContext();
        String name = cxt.getInitParameter( CLUSTER_NAME );
        LOG.log( Level.WARNING, () -> "CTX " + name );

        if( name != null && !name.isEmpty() ) {
            clusterName = name;
        }

        try {
            threadCount = Math.max( 1, Integer.parseInt( cxt.getInitParameter( THREAD_COUNT ) ) );
        }
        catch( NumberFormatException |
               NullPointerException ex ) {
            threadCount = 1;
        }

        LOG.log( Level.WARNING, () -> "Cluster Name " + clusterName + " threadCount " + threadCount );
    }

    @Override
    public void contextDestroyed( ServletContextEvent sce )
    {
    }

    public static String getClusterName()
    {
        return clusterName;
    }

    public static int getThreadCount()
    {
        return threadCount;
    }

}
