/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.commons.math;

import uk.trainwatch.util.TriConsumer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.trainwatch.job.lang.expr.Arithmetic;

/**
 *
 * @author peter
 */
public class MathUtilTest
{
    /**
     * Test of min method, of class MathUtil.
     */
    @Test
    public void min()
    {
        testMinMax( 0, MathUtil::min );
        testMinMaxNull( MathUtil::min );
    }

    /**
     * Test of max method, of class MathUtil.
     */
    @Test
    public void max()
    {
        testMinMax( 1, MathUtil::max );
        testMinMaxNull( MathUtil::max );
    }

    private static final Number MIN_MAX_DATA[][] = {
        // Min values
        {0, 0L, 0F, 0D, BigInteger.ZERO, BigDecimal.ZERO},
        // Max values
        {10, 10L, 10.0F, 10.0D, BigInteger.TEN, BigDecimal.TEN}
    };

    /**
     *
     * @param test funtion of test.
     */
    private void testMinMaxNull( BiFunction<Number, Number, Number> test )
    {
        // If both are null then return null
        assertNull( test.apply( null, null ) );
        
        BiConsumer<Number, Number> c = ( a, b ) -> {
            Number v = test.apply( a, b );
            assertNotNull( v );
            if( a == null ) {
                assertEquals( b, v );
            }
            else if( b == null ) {
                assertEquals( a, v );
            }
        };

        for( Number[] row: MIN_MAX_DATA ) {
            for( Number v: row ) {
                c.accept( v, null );
                c.accept( null, v );
            }
        }
    }

    /**
     * 
     * @param expected row number in MIN_MAX_DATA of expected value
     * @param test function to test
     */
    private void testMinMax( int expected, BiFunction<Number, Number, Number> test )
    {
        TriConsumer<Number, Number, Number> c = ( e, a, b ) -> {
            Number v = test.apply( a, b );

            // Not null as none of our test data is null
            assertNotNull( v );

            // Test that returned type is the same as a as per contract
            assertTrue( a.getClass() == v.getClass() );

            // Test we have the correct value
            assertEquals( Arithmetic.convert( a, e ), v );
        };

        for( int i = 0; i < MIN_MAX_DATA[0].length; i++ ) {
            final Number a = MIN_MAX_DATA[0][i];
            final Number e = MIN_MAX_DATA[expected][i];
            for( Number b: MIN_MAX_DATA[1] ) {
                c.accept( e, a, b );
                c.accept( e, b, a );
            }
        }
    }

    private static final Number FLOOR_CEIL_DATA[][] = {
        {10, 10L, 10.0F, 10.0D, BigInteger.TEN, BigDecimal.TEN},
        {10, 10L, 10.4F, 10.4D, BigInteger.TEN, new BigDecimal( "10.4" )},
        {10, 10L, 9.6F, 9.6D, BigInteger.TEN, new BigDecimal( "9.6" )}
    };

    /**
     * FLOOR_CEIL_DATA[0] is the expected data
     * 
     * @param src row number in FLOOR_CEIL_DATA of source data
     * @param test function to test
     */
    private void floorCeiltest( int src, Function<Number, Number> test )
    {
        for( Number srcVal: FLOOR_CEIL_DATA[src] ) {
            for( Number expected: FLOOR_CEIL_DATA[0] ) {
                Number v = test.apply( srcVal );
                assertNotNull( v );
                assertEquals( Arithmetic.convert( v.getClass(), expected ), test.apply( srcVal ) );
            }
        }
    }

    @Test
    public void floor()
    {
        floorCeiltest( 1, MathUtil::floor );
    }

    @Test
    public void ceil()
    {
        floorCeiltest( 2, MathUtil::ceil );
    }
    
    @Test
    public void round()
    {
        floorCeiltest( 2, MathUtil::round );
    }
}
