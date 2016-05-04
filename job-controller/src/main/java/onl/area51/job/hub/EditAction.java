package onl.area51.job.hub;

import onl.area51.job.cluster.MinioService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.json.Json;
import onl.area51.httpd.HttpRequestHandlerBuilder;
import onl.area51.httpd.action.ActionRegistry;
import onl.area51.httpd.rest.JsonEntity;
import org.apache.http.HttpStatus;

/**
 *
 * @author peter
 */
@ApplicationScoped
public class EditAction
{

    void deploy( @Observes ActionRegistry registry, MinioService minioService )
    {
        registry.registerHandler( "/get", HttpRequestHandlerBuilder.create()
                                  .log()
                                  .method( "GET" )
                                  .setAttribute( "path", r -> r.getParam( "path" ) )
                                  .ifAttributePresentSetAttribute( "path", "json",
                                                                   ( r, path ) -> Json.createObjectBuilder()
                                                                   .add( "path", path.toString() )
                                                                   .add( "content", minioService.getContent( path.toString() ) )
                                  )
                                  .ifAttributePresentSendOk( "json", JsonEntity::createFromAttribute )
                                  .ifAttributeAbsentSendError( "json", HttpStatus.SC_BAD_REQUEST )
                                  .end()
                                  .build() )
                .registerHandler( "/update", HttpRequestHandlerBuilder.create()
                                  .log()
                                  .method( "POST" )
                                  .setAttribute( "path", r -> r.getParam( "path" ) )
                                  .ifAttributePresentSetAttribute( "path", "content", r -> r.getParam( "content" ) )
                                  .ifAttributePresentSetAttribute( "content", "json",
                                                                   r -> {
                                                                       String path = r.getAttribute( "path" );
                                                                       String content = r.getAttribute( "content" );
                                                                       minioService.setContent( path, content );
                                                                       return Json.createObjectBuilder()
                                                                       .add( "path", path )
                                                                       .add( "content", content );
                                                                   } )
                                  .ifAttributePresentSendOk( "json", JsonEntity::createFromAttribute )
                                  .ifAttributeAbsentSendError( "json", HttpStatus.SC_BAD_REQUEST )
                                  .end()
                                  .build() )
                .registerHandler( "/new", HttpRequestHandlerBuilder.create()
                                  .log()
                                  .method( "GET" )
                                  .setAttribute( "path", r -> r.getParam( "path" ) )
                                  .ifAttributePresentSetAttribute( "path", "content",
                                                                   ( r, path ) -> Json.createObjectBuilder()
                                                                   .add( "path", path.toString() )
                                                                   .add( "content", "" )
                                  )
                                  .ifAttributePresentSendOk( "json", JsonEntity::createFromAttribute )
                                  .ifAttributeAbsentSendError( "json", HttpStatus.SC_BAD_REQUEST )
                                  .end()
                                  .build() );
    }

}
