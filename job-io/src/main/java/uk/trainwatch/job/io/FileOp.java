/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import static uk.trainwatch.job.lang.expr.Arithmetic.decode;
import uk.trainwatch.job.lang.expr.ExpressionOperation;

/**
 *
 * @author peter
 */
public class FileOp
{

    public static ExpressionOperation newFile( ExpressionOperation exp0 )
    {
        return ( s, a ) -> {
            Object arg0 = Objects.requireNonNull( decode( exp0.invoke( s ) ) );
            return s.getJob().getJobOutput().createFile( Objects.toString( arg0 ) );
        };
    }

    public static ExpressionOperation newFile( ExpressionOperation exp0, ExpressionOperation exp1 )
    {
        return ( s, a ) -> {
            Object arg0 = Objects.requireNonNull( decode( exp0.invoke( s ) ) );
            Object arg1 = Objects.requireNonNull( decode( exp1.invoke( s ) ) );

            File f;
            if( arg0 instanceof File ) {
                f = new File( (File) arg0, Objects.toString( arg1 ) );
            }
            else {
                f = new File( Objects.toString( arg0 ), Objects.toString( arg1 ) );
            }
            s.getJob().getJobOutput().addFile( f );
            return f;
        };
    }

    public static ExpressionOperation newTempFile( ExpressionOperation exp0 )
    {
        return ( s, a ) -> {
            String name = Objects.requireNonNull( decode( exp0.invoke( s ) ) ).toString();
            int i = name.lastIndexOf( "." );

            return s.getJob().getJobOutput().createTempFile( i > -1 ? name.substring( 0, i ) : name,
                                                             i > -1 ? name.substring( i ) : "" );
        };
    }

    public static ExpressionOperation newTempFile( ExpressionOperation exp0, ExpressionOperation exp1 )
    {
        return ( s, a ) -> {
            Object arg0 = Objects.requireNonNull( decode( exp0.invoke( s ) ) );
            Object arg1 = Objects.requireNonNull( decode( exp1.invoke( s ) ) );

            return s.getJob().getJobOutput().createTempFile( Objects.toString( arg0 ), Objects.toString( arg1 ) );
        };
    }

    public static ExpressionOperation newReader( ExpressionOperation exp )
    {
        return ( s, a ) -> new ReaderWrapper( createReader( exp.invoke( s, a ) ) );
    }

    public static Reader createReader( Object v )
            throws FileNotFoundException
    {
        Objects.requireNonNull( v );
        Object o = decode( v );

        if( o instanceof ReaderWrapper ) {
            return (Reader) o;
        }

        if( o instanceof File ) {
            return new FileReader( (File) o );
        }

        if( o instanceof Path ) {
            return new FileReader( ((Path) o).toFile() );
        }

        if( o instanceof Reader ) {
            return (Reader) o;
        }

        if( o instanceof InputStream ) {
            return new InputStreamReader( (InputStream) o );
        }

        return new StringReader( o.toString() );
    }

    public static ExpressionOperation newWriter( ExpressionOperation exp )
    {
        return ( s, a ) -> new WriterWrapper( createWriter( exp.invoke( s, a ) ) );
    }

    public static Writer createWriter( Object v )
            throws IOException
    {
        Objects.requireNonNull( v );
        Object o = decode( v );

        if( o instanceof WriterWrapper ) {
            return (Writer) o;
        }

        if( o instanceof File ) {
            return new FileWriter( (File) o );
        }

        if( o instanceof Path ) {
            return new FileWriter( ((Path) o).toFile() );
        }

        if( o instanceof Writer ) {
            return (Writer) o;
        }

        if( o instanceof OutputStream ) {
            return new OutputStreamWriter( (OutputStream) o );
        }

        return new StringWriter();
    }

    private static InputStream newInputStream( Object v )
            throws FileNotFoundException
    {
        Objects.requireNonNull( v );
        Object o = decode( v );

        if( o instanceof File ) {
            return new FileInputStream( (File) o );
        }

        if( o instanceof Path ) {
            return new FileInputStream( ((Path) o).toFile() );
        }

        if( o instanceof InputStream ) {
            return (InputStream) o;
        }

        throw new UnsupportedOperationException();
    }

    private static OutputStream newOutputStream( Object v )
            throws FileNotFoundException
    {
        Objects.requireNonNull( v );
        Object o = decode( v );

        if( o instanceof File ) {
            return new FileOutputStream( (File) o );
        }

        if( o instanceof Path ) {
            return new FileOutputStream( ((Path) o).toFile() );
        }

        if( o instanceof OutputStream ) {
            return (OutputStream) o;
        }

        throw new UnsupportedOperationException();
    }

    public static ExpressionOperation gunzip( ExpressionOperation exp )
    {
        return ( s, a ) -> new GZIPInputStream( newInputStream( exp.invoke( s, a ) ) );
    }

    public static ExpressionOperation gzip( ExpressionOperation exp )
    {
        return ( s, a ) -> new GZIPOutputStream( newOutputStream( exp.invoke( s, a ) ) );
    }

}
