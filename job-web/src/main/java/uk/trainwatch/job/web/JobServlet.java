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
package uk.trainwatch.job.web;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import onl.area51.job.cluster.JobCluster;
import uk.trainwatch.util.JsonUtils;

/**
 * Handles the request to execute jobs
 * <p>
 * TODO this needs security adding - currently thats handled outside of the application
 *
 * @author peter
 */
@WebServlet(name = "JobServlet", urlPatterns = "/job", loadOnStartup = 0)
public class JobServlet
        extends HttpServlet
{

    private static final Logger LOG = Logger.getLogger( JobServlet.class.getName() );

    @Inject
    private JobCluster jobCluster;

    @Override
    public void init( ServletConfig config )
            throws ServletException
    {
        super.init( config );

        // Just call it to initialise it
        jobCluster.isValidName( "" );
    }

    private int getInt( String s )
    {
        if( s != null && !s.isEmpty() ) {
            try {
                return Integer.parseInt( s );
            }
            catch( Exception ex ) {
            }
        }
        return 1;
    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
            throws ServletException,
                   IOException
    {
        String clusterName = req.getParameter( "cluster" );
        String jobName = req.getParameter( "job" );
        boolean wait = Boolean.valueOf( req.getParameter( "wait" ) );
        int time = getInt( req.getParameter( "time" ) );
        try {

            if( wait ) {
                LOG.log( Level.INFO, () -> "Executing cluster=" + clusterName + " job=" + jobName + " wait=" + wait + " time=" + time );

                Map<String, Object> ret = jobCluster.call( clusterName, jobName, null, time, TimeUnit.MINUTES );
                resp.getWriter()
                        .println( ret );
            }
            else {
                LOG.log( Level.INFO, () -> "Executing cluster=" + clusterName + " job=" + jobName );

                jobCluster.execute( clusterName, jobName, null );
            }
        }
        catch( TimeoutException ex ) {
            LOG.log( Level.SEVERE, ex, () -> "Timeout for time " + time );
            resp.sendError( HttpServletResponse.SC_GATEWAY_TIMEOUT, ex.getMessage() );
        }
        catch( IOException ex ) {
            LOG.log( Level.SEVERE, null, ex );
            resp.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage() );
        }
    }

}
