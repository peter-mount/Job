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
import java.io.PrintWriter;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import onl.area51.job.cluster.ClusterJobRetriever;
import onl.area51.job.jcl.Jcl;

/**
 * A simple servlet that retrieves the job content/
 * <p>
 * Note:
 *
 * @author peter
 */
@WebServlet(name = "JobRetrieveServer", urlPatterns = "/jcl/get", loadOnStartup = 0)
public class JobRetrieveServlet
        extends HttpServlet
{

    @Inject
    private ClusterJobRetriever clusterJobRetriever;

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
            throws ServletException,
                   IOException
    {
        String node = req.getParameter( "node" );
        String name = req.getParameter( "name" );
        if( node != null && !node.isEmpty() && name != null && !name.isEmpty() ) {
            Jcl jcl = Jcl.create( node, name );
            try {
                String script = clusterJobRetriever.retrieveJob( jcl );

                if( script != null ) {
                    byte b[] = script.getBytes( "UTF-8" );

                    resp.setContentType( "application/x-jcl" );
                    resp.setContentLength( b.length );
                    try( PrintWriter w = resp.getWriter() ) {
                        w.println( script );
                    }
                }
            }
            catch( IOException ex ) {
                log( "Failed to get " + node + ":" + name, ex );
                resp.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            }

        }

        resp.sendError( HttpServletResponse.SC_NOT_FOUND );
    }

}
