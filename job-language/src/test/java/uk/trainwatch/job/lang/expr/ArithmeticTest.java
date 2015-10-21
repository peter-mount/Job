/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.expr;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiFunction;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author peter
 */
public class ArithmeticTest
{

    @FunctionalInterface
    public static interface TriConsumer<A, B, C>
    {

        void accept( A e, B c, C v );

    }

    /**
     * Test of convert method, of class MathUtil.
     */
    @Test
    public void convert_Class_Number()
    {
        testConvert( ( e, v ) -> Arithmetic.convert( e.getClass(), v ) );
    }

    /**
     * Test of convert method, of class MathUtil.
     */
    @Test
    public void convert_Number_Number()
    {
        testConvert( Arithmetic::convert );
    }

    private static final Number CONVERT_DATA[] = {1, 1L, 1F, 1D, BigInteger.ONE, BigDecimal.ONE};

    private void testConvert( BiFunction<Number, Number, Number> test )
    {
        for( Number e: CONVERT_DATA ) {
            for( Number v: CONVERT_DATA ) {
                Number a = test.apply( e, v );
                assertNotNull( a );
                assertEquals( e, a );
            }
        }
    }

    /**
     * Test of compareTo method, of class MathUtil.
     */
    @Test
    public void compareTo()
    {
        testCompareTo( ( lessThan, a, b ) -> {
            if( a.equals( b ) ) {
                // a == b
                assertEquals( 0, Arithmetic.compareTo( a, b ) );
            }
            else if( lessThan ) {
                // a < b
                assertEquals( -1, Arithmetic.compareTo( a, b ) );
            }
            else {
                // a > b
                assertEquals( 1, Arithmetic.compareTo( a, b ) );
            }
        } );
    }

    /**
     *
     * @param test test function, boolean true if expected to be less than if not equal. a,b is a.compareTo(b)
     */
    private void testCompareTo( TriConsumer<Boolean, Number, Number> test )
    {
        for( Number a: MIN_MAX_DATA[0] ) {
            for( Number b: MIN_MAX_DATA[1] ) {
                test.accept( true, a, b );
                test.accept( false, b, a );
            }
        }
    }

    private static final Number MIN_MAX_DATA[][] = {
        // Min values
        {0, 0L, 0F, 0D, BigInteger.ZERO, BigDecimal.ZERO},
        // Max values
        {10, 10L, 10.0F, 10.0D, BigInteger.TEN, BigDecimal.TEN}
    };

}
