package onl.area51.job.hub;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import onl.area51.httpd.HttpRequestHandlerBuilder;
import onl.area51.httpd.action.ActionRegistry;
import onl.area51.httpd.action.Actions;

/**
 *
 * @author peter
 */
@ApplicationScoped
public class EditAction
{

    void deploy( @Observes ActionRegistry registry )
    {
        registry.registerHandler( "/edit", HttpRequestHandlerBuilder.create()
                                  .log()
                                  .method( "GET" )
                                  .add( Actions.resourceAction( EditAction.class, "/jobcontrol/editor.html" ) )
                                  .end()
                                  .build() );
    }

}
