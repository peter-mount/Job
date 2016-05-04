/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onl.area51.job.cluster;

import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InputSizeMismatchException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.NoResponseException;
import io.minio.messages.Item;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author peter
 */
@ApplicationScoped
public class MinioService
{

    private String bucketName;
    private MinioClient client;

    @PostConstruct
    void start()
            throws Exception
    {

        client = new MinioClient( System.getenv( "MINIO_ENDPOINT" ), System.getenv( "MINIO_ACCESS_KEY" ), System.getenv( "MINIO_SECRET_KEY" ) );

        bucketName = System.getenv( "JOB_BUCKET" );

        if( !client.bucketExists( bucketName ) ) {
            client.makeBucket( bucketName );
        }
    }

    public String getBucketName()
    {
        return bucketName;
    }

    public MinioClient getClient()
    {
        return client;
    }

    public Stream<Item> list( String prefix )
            throws IOException
    {
        Iterable<Result<Item>> iterable = client.listObjects( bucketName, prefix, false );
        Iterator<Result<Item>> iterator = iterable.iterator();

        // Wrap with one that doesn't throw NPE if there are no results - bug in Minio client?
        Iterator<Result<Item>> it = new Iterator<Result<Item>>()
        {
            @Override
            public boolean hasNext()
            {
                try {
                    return iterator.hasNext();
                }
                catch( NullPointerException ex ) {
                    return false;
                }
            }

            @Override
            public Result<Item> next()
            {
                return iterator.next();
            }
        };

        return StreamSupport.stream( Spliterators.spliteratorUnknownSize( it, 0 ), false )
                .map( r -> {
                    try {
                        return r.get();
                    }
                    catch( InvalidBucketNameException |
                           NoSuchAlgorithmException |
                           InsufficientDataException |
                           IOException |
                           InvalidKeyException |
                           NoResponseException |
                           XmlPullParserException |
                           ErrorResponseException |
                           InternalException ex ) {
                        throw new RuntimeException( ex );
                    }
                } );
    }

    public String getJobPath( String node, String job )
    {
        return String.join( "/", "jobs", node, job );
    }

    public String getContent( String node, String job )
            throws IOException
    {
        return getContent( getJobPath( node, job ) );
    }

    public String getContent( String objectName )
            throws IOException
    {
        try( BufferedReader r = new BufferedReader( new InputStreamReader( getObject( objectName ) ) ) ) {
            return r.lines().collect( Collectors.joining( "\n" ) );
        }
    }

    public void setContent( String node, String job, String content )
            throws IOException
    {
        setContent( getJobPath( node, job ), content );
    }

    public void setContent( String objectName, String content )
            throws IOException
    {
        setContent( objectName, content.getBytes( StandardCharsets.UTF_8 ) );
    }

    public void setContent( String objectName, byte[] content )
            throws IOException
    {
        try( InputStream is = new ByteArrayInputStream( content ) ) {
            putObject( bucketName, objectName, is, content.length, "plain/text" );
        }
    }

    public InputStream getObject( String objectName )
            throws IOException
    {
        try {
            return client.getObject( bucketName, objectName );
        }
        catch( InvalidBucketNameException |
               NoSuchAlgorithmException |
               InsufficientDataException |
               InvalidKeyException |
               NoResponseException |
               XmlPullParserException |
               ErrorResponseException |
               InternalException |
               InvalidArgumentException ex ) {
            throw new IOException( ex );
        }
    }

    public InputStream getObject( String bucketName, String objectName, long offset )
            throws IOException
    {
        try {
            return client.getObject( bucketName, objectName, offset );
        }
        catch( InvalidBucketNameException |
               NoSuchAlgorithmException |
               InsufficientDataException |
               InvalidKeyException |
               NoResponseException |
               XmlPullParserException |
               ErrorResponseException |
               InternalException |
               InvalidArgumentException ex ) {
            throw new IOException( ex );
        }
    }

    public void removeObject( String bucketName, String objectName )
            throws IOException
    {
        try {
            client.removeObject( bucketName, objectName );
        }
        catch( InvalidBucketNameException |
               NoSuchAlgorithmException |
               InsufficientDataException |
               InvalidKeyException |
               NoResponseException |
               XmlPullParserException |
               ErrorResponseException |
               InternalException ex ) {
            throw new IOException( ex );
        }
    }

    public void putObject( String bucketName, String objectName, InputStream stream, long size, String contentType )
            throws IOException
    {
        try {
            client.putObject( bucketName, objectName, stream, size, contentType );
        }
        catch( InvalidBucketNameException |
               NoSuchAlgorithmException |
               InsufficientDataException |
               InvalidKeyException |
               NoResponseException |
               XmlPullParserException |
               ErrorResponseException |
               InternalException |
               InvalidArgumentException |
               InputSizeMismatchException ex ) {
            throw new IOException( ex );
        }
    }

}
