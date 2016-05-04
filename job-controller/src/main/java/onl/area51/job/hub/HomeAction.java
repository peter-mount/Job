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
public class HomeAction
{

    void deploy( @Observes ActionRegistry registry )
    {
        registry.registerHandler("/", HttpRequestHandlerBuilder.create()
                                  .log()
                                  .method( "GET" )
                                  .add(Actions.resourceAction(HomeAction.class, "/home.html" ) )
                                  .end()
                                  .build() );
    }

}
