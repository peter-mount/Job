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
public class TableDecimalFormatTest
{

    @Test
    public void testInt()
    {
        assertEquals( "        42",
                      new TableDecimalFormat()
                      .right()
                      .setMaxLength( 10 )
                      .setFormat( "#0" )
                      .format( 42 )
        );
    }

    @Test
    public void testLong()
    {
        assertEquals( "        42",
                      new TableDecimalFormat()
                      .right()
                      .setMaxLength( 10 )
                      .setFormat( "#0" )
                      .format( 42L )
        );
    }

    @Test
    public void testDouble()
    {
        assertEquals( "  3.141593",
                      new TableDecimalFormat()
                      .right()
                      .setMaxLength( 10 )
                      .setFormat( "#0.000000" )
                      .format( 3.1415926 )
        );
    }

}
