/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import static uk.trainwatch.job.lang.expr.Arithmetic.decode;

/**
 *
 * @author peter
 */
public class WriterWrapper
        extends Writer
{

    private final BufferedWriter w;

    public WriterWrapper( Writer w )
    {
        this.w = w instanceof BufferedWriter ? (BufferedWriter) w : new BufferedWriter( w );
    }

    @Override
    public void close()
            throws IOException
    {
        w.close();
    }

    //<editor-fold defaultstate="collapsed" desc="BufferedWriter">
    @Override
    public void write( int c )
            throws IOException
    {
        w.write( c );
    }

    @Override
    public void write( char[] cbuf, int off, int len )
            throws IOException
    {
        w.write( cbuf, off, len );
    }

    @Override
    public void write( String s, int off, int len )
            throws IOException
    {
        w.write( s, off, len );
    }

    public void newLine()
            throws IOException
    {
        w.newLine();
    }

    @Override
    public void flush()
            throws IOException
    {
        w.flush();
    }

    @Override
    public void write( char[] cbuf )
            throws IOException
    {
        w.write( cbuf );
    }

    @Override
    public void write( String str )
            throws IOException
    {
        w.write( str );
    }

    //</editor-fold>
    public void write( Object o )
            throws IOException
    {
        append( o );
    }

    public Writer append( Object o )
            throws IOException
    {

        if( o != null ) {
            w.append( decode( o ).toString() );
        }
        return this;
    }

    @Override
    public String toString()
    {
        return w.toString();
    }
    
    
}
