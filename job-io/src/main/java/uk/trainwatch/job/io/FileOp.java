/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.lang.Statement;
import static uk.trainwatch.job.lang.expr.Arithmetic.decode;
import uk.trainwatch.job.lang.expr.ExpressionOperation;

/**
 *
 * @author peter
 */
public class FileOp
{

    public static Path getPath( ExpressionOperation exp, Scope s )
            throws Exception
    {
        Object o = decode( exp.invoke( s ) );
        if( o instanceof Path )
        {
            return (Path) o;
        }
        if( o instanceof File )
        {
            return ((File) o).toPath();
        }
        return s.getJob().getJobOutput().pathOf( Objects.toString( o ) );
    }

    public static ExpressionOperation newFile( ExpressionOperation exp )
    {
        return ( s, a ) -> getPath( exp, s );
    }

    public static ExpressionOperation newFile( ExpressionOperation exp0, ExpressionOperation exp1 )
    {
        return ( s, a ) -> getPath( exp0, s ).resolve( getPath( exp1, s ) );
    }

    public static ExpressionOperation newReader( ExpressionOperation exp )
    {
        return ( s, a ) -> new ReaderWrapper( Files.newBufferedReader( getPath( exp, s ) ) );
    }

    public static Reader createReader( Object v )
            throws IOException
    {
        Object o = Objects.requireNonNull( decode( v ) );

        if( o instanceof ReaderWrapper )
        {
            return (Reader) o;
        }

        if( o instanceof Path )
        {
            return new ReaderWrapper( Files.newBufferedReader( (Path) o ) );
        }

        if( o instanceof Reader )
        {
            return (Reader) o;
        }

        if( o instanceof InputStream )
        {
            return new InputStreamReader( (InputStream) o );
        }

        throw new UnsupportedOperationException( "Unable to create Reader from " + o );
    }

    public static ExpressionOperation newWriter( ExpressionOperation exp )
    {
        return ( s, a ) -> new WriterWrapper( Files.newBufferedWriter( getPath( exp, s ), StandardOpenOption.CREATE, StandardOpenOption.WRITE ) );
    }

    public static Writer createWriter( Object v )
            throws IOException
    {
        Objects.requireNonNull( v );
        Object o = decode( v );

        if( o instanceof WriterWrapper )
        {
            return (Writer) o;
        }

        if( o instanceof Path )
        {
            return new WriterWrapper( Files.newBufferedWriter( (Path) o, StandardOpenOption.CREATE, StandardOpenOption.WRITE ) );
        }

        if( o instanceof Writer )
        {
            return (Writer) o;
        }

        if( o instanceof OutputStream )
        {
            return new OutputStreamWriter( (OutputStream) o );
        }

        throw new UnsupportedOperationException( "Unable to create Writer from " + o );
    }

    private static InputStream newInputStream( Object v )
            throws IOException
    {
        Objects.requireNonNull( v );
        Object o = decode( v );

        if( o instanceof File )
        {
            return new FileInputStream( (File) o );
        }

        if( o instanceof Path )
        {
            return Files.newInputStream( (Path) o );
        }

        if( o instanceof InputStream )
        {
            return (InputStream) o;
        }

        throw new UnsupportedOperationException();
    }

    private static OutputStream newOutputStream( Object v )
            throws IOException
    {
        Objects.requireNonNull( v );
        Object o = decode( v );

        if( o instanceof File )
        {
            return new FileOutputStream( (File) o );
        }

        if( o instanceof Path )
        {
            return Files.newOutputStream( (Path) o );
        }

        if( o instanceof OutputStream )
        {
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

    public static Statement delete( ExpressionOperation... args )
    {
        return ( s, a )
                -> 
                {
                    for( ExpressionOperation arg : args )
                    {
                        Object o = decode( arg.invoke( s, a ) );
                        if( o != null )
                        {
                            Path p;
                            if( o instanceof Path )
                            {
                                p = (Path) o;
                            }
                            else if( o instanceof File )
                            {
                                p = ((File) o).toPath();
                            }
                            else
                            {
                                p = s.getJob().getJobOutput().pathOf( o.toString() );
                            }
                            Files.delete( p );
                        }
                    }
        };
    }
}
