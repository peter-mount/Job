package onl.area51.job.hub;

import io.minio.messages.Owner;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import onl.area51.httpd.HttpRequestHandlerBuilder;
import onl.area51.httpd.action.ActionRegistry;
import onl.area51.httpd.rest.JsonEntity;
import org.apache.http.HttpStatus;
import uk.trainwatch.util.Functions;
import uk.trainwatch.util.JsonUtils;

/**
 * Lists the available nodes in a bucket
 *
 * @author peter
 */
@ApplicationScoped
public class ListAction
{

    void deploy( @Observes ActionRegistry registry, MinioService minioService )
    {
        registry.registerHandler( "/list", HttpRequestHandlerBuilder.create()
                                  .log()
                                  .method( "GET" )
                                  .setAttribute( "path", r -> r.getParam( "path" ) )
                                  .ifAttributePresentSetAttribute( "path", "json",
                                                                   ( r, prefix ) -> minioService.list( prefix.toString() )
                                                                   .map( item -> {
                                                                       Owner owner = item.owner();
                                                                       JsonObjectBuilder b = Json.createObjectBuilder()
                                                                               .add( "isDir", item.isDir() )
                                                                               .add( "name", item.objectName() )
                                                                               .add( "size", item.objectSize() )
                                                                               .add( "storageClass", item.storageClass() )
                                                                               .add( "owner", Json.createObjectBuilder()
                                                                                     .add( "id", owner.id() )
                                                                                     .add( "displayName", owner.displayName() ) );
                                                                       JsonUtils.add( b, "etag", item.etag() );
                                                                       return b;
                                                                   } )
                                                                   .reduce( Json.createArrayBuilder(),
                                                                            ( a, o ) -> a.add( o ),
                                                                            Functions.writeOnceBinaryOperator() )
                                  )
                                  .ifAttributePresentSendOk( "json", JsonEntity::createFromAttribute )
                                  .ifAttributeAbsentSendError( "json", HttpStatus.SC_BAD_REQUEST )
                                  .end()
                                  .build() )
                .registerHandler( "/listTree", HttpRequestHandlerBuilder.create()
                                  .log()
                                  .method( "GET" )
                                  .setAttribute( "path", r -> r.getParam( "path" ) )
                                  .ifAttributePresentSetAttribute( "path", "json",
                                                                   ( r, prefix ) -> {
                                                                       String p = prefix.toString();
                                                                       int l = p.length();
                                                                       return minioService.list( p )
                                                                       .map( item -> {
                                                                           String name = item.objectName();
                                                                           String text = item.objectName().substring( l );
                                                                           if( text.endsWith( "/" ) ) {
                                                                               text = text.substring( 0, text.length() - 1 );
                                                                           }
                                                                           JsonObjectBuilder b = Json.createObjectBuilder()
                                                                                   .add( "id", name )
                                                                                   .add( "text", text );
                                                                           if( item.isDir() ) {
                                                                               b.add( "children", item.isDir() );
                                                                           }else {
                                                                               b.add( "icon", "httpd/buttons/misc/Document24.gif" );
                                                                           }
                                                                           return b;
                                                                       } )
                                                                       .reduce( Json.createArrayBuilder(),
                                                                                ( a, o ) -> a.add( o ),
                                                                                Functions.writeOnceBinaryOperator() );
                                                                   } )
                                  .ifAttributePresentSendOk( "json", JsonEntity::createFromAttribute )
                                  .ifAttributeAbsentSendError( "json", HttpStatus.SC_BAD_REQUEST )
                                  .end()
                                  .build() );
    }

}
