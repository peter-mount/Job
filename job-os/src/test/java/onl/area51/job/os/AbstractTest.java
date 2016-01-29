/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onl.area51.job.os;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;
import org.antlr.v4.runtime.tree.ParseTree;
import static org.junit.Assert.*;

/**
 *
 * @author peter
 */
public class AbstractTest
{

    protected static void debug( String t )
            throws IOException
    {
        try( BufferedReader r = new BufferedReader( new InputStreamReader( AbstractTest.class.getResourceAsStream( t ) ) ) ) {
            AtomicInteger c = new AtomicInteger();
            r.lines().forEach( l -> System.out.printf( "%03d %s\n", c.incrementAndGet(), l ) );
        }
    }

    protected InputStream getScript( String t )
    {
        InputStream is = AbstractTest.class.getResourceAsStream( t );
        assertNotNull( "Failed to find " + t, is );
        return is;
    }

    protected static void debug( ParseTree t )
    {
        System.out.println( "\nParse tree" );
        //System.out.println( t.getText() );
        debug( t, "" );
    }

    protected static void debug( ParseTree t, String prefix )
    {
        {//if( !(t instanceof TerminalNode) ) {
            int s = t.getChildCount();
            for( int i = 0; i < s; i++ ) {
                ParseTree c = t.getChild( i );
                String p = String.format( prefix.isEmpty() ? "%2$02d" : "%1$s.%2$02d", prefix, i );
                System.out.println( p + " " + c.getText() + " " + t.getClass() );
                if( c != null ) {
                    debug( c, p );
                }
            }
        }
    }

}
