/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.stream.Stream;

/**
 *
 * @author peter
 */
public class ReaderWrapper
        extends Reader
{

    private final BufferedReader r;

    public ReaderWrapper( Reader r )
    {
        this.r = r instanceof BufferedReader ? (BufferedReader) r : new BufferedReader( r );
    }

    public void close()
            throws IOException
    {
        r.close();
    }

    //<editor-fold defaultstate="collapsed" desc="Reader">
    public int read( CharBuffer target )
            throws IOException
    {
        return r.read( target );
    }

    public int read()
            throws IOException
    {
        return r.read();
    }

    public int read( char[] cbuf )
            throws IOException
    {
        return r.read( cbuf );
    }

    public int read( char[] cbuf, int off, int len )
            throws IOException
    {
        return r.read( cbuf, off, len );
    }

    public long skip( long n )
            throws IOException
    {
        return r.skip( n );
    }

    public boolean ready()
            throws IOException
    {
        return r.ready();
    }

    public boolean markSupported()
    {
        return r.markSupported();
    }

    public void mark( int readAheadLimit )
            throws IOException
    {
        r.mark( readAheadLimit );
    }

    public void reset()
            throws IOException
    {
        r.reset();
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Buffered Reader>
    public String readLine()
            throws IOException
    {
        return r.readLine();
    }

    public Stream<String> lines()
    {
        return r.lines();
    }

    //</editor-fold>
    @Override
    public String toString()
    {
        return r.toString();
    }

}
