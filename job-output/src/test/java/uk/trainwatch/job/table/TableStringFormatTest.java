/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.table;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author peter
 */
public class TableStringFormatTest
{

    private static final String STRING = "A String";
    private static final String STRING_SHORT = "A S";
    private static final int LENGTH = 11;
    private static final String STRING_L = "A String   ";
    private static final String STRING_R = "   A String";
    private static final String STRING_C = " A String  ";

    /**
     * Test of setMaxLength method, of class StringFormat.
     */
    @Test
    public void testLongerThanMaxLength()
    {
        TableStringFormat f = new TableStringFormat();
        f.setMaxLength( STRING_SHORT.length() );
        assertEquals( STRING_SHORT, f.format( STRING ) );
    }

    /**
     * Test of getMaxLength method, of class StringFormat.
     */
    @Test
    public void testDefault()
    {
        TableStringFormat f = new TableStringFormat();

        assertEquals( STRING, f.format( STRING ) );
    }

    /**
     * Test of getMaxLength method, of class StringFormat.
     */
    @Test
    public void testLeft()
    {
        TableStringFormat f = new TableStringFormat()
                .setAlignment( Alignment.LEFT )
                .setMaxLength( LENGTH );
        assertEquals( STRING_L, f.format( STRING ) );
    }

    /**
     * Test of getMaxLength method, of class StringFormat.
     */
    @Test
    public void testRight()
    {
        TableStringFormat f = new TableStringFormat()
                .setAlignment( Alignment.RIGHT )
                .setMaxLength( LENGTH );
        assertEquals( STRING_R, f.format( STRING ) );
    }

    /**
     * Test of getMaxLength method, of class StringFormat.
     */
    @Test
    public void testCenter()
    {
        TableStringFormat f = new TableStringFormat()
                .setAlignment( Alignment.CENTER )
                .setMaxLength( LENGTH );
        assertEquals( STRING_C, f.format( STRING ) );
    }

}
