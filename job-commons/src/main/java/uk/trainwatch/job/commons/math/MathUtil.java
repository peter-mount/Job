/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.commons.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import static uk.trainwatch.job.lang.expr.Arithmetic.compareTo;
import static uk.trainwatch.job.lang.expr.Arithmetic.convert;

/**
 *
 * @author peter
 */
public class MathUtil
{

    public static Number min( Number a, Number b )
    {
        if( a == null ) {
            return b;
        }
        if( b == null ) {
            return a;
        }

        return compareTo( a, b ) <= 0 ? a : convert( a, b );
    }

    public static Number max( Number a, Number b )
    {
        if( a == null ) {
            return b;
        }
        if( b == null ) {
            return a;
        }

        return compareTo( a, b ) >= 0 ? a : convert( a, b );
    }

    public static Number floor( Number a )
    {
        if( a == null || a instanceof Integer || a instanceof Long || a instanceof Short || a instanceof Byte || a instanceof BigInteger ) {
            return a;
        }
        if( a instanceof BigDecimal ) {
            return ((BigDecimal) a).setScale( 0, RoundingMode.FLOOR );
        }
        return Math.floor( a.doubleValue() );
    }

    public static Number ceil( Number a )
    {
        if( a == null || a instanceof Integer || a instanceof Long || a instanceof Short || a instanceof Byte || a instanceof BigInteger ) {
            return a;
        }
        if( a instanceof BigDecimal ) {
            return ((BigDecimal) a).setScale( 0, RoundingMode.CEILING );
        }
        return Math.ceil( a.doubleValue() );
    }

    public static Number round( Number a )
    {
        if( a == null || a instanceof Integer || a instanceof Long || a instanceof Short || a instanceof Byte || a instanceof BigInteger ) {
            return a;
        }
        if( a instanceof BigDecimal ) {
            return ((BigDecimal) a).setScale( 0, RoundingMode.HALF_UP );
        }
        return Math.round( a.doubleValue() );
    }

    public static Number abs( Number a )
    {
        if( a == null ) {
            return null;
        }

        if( a instanceof Integer || a instanceof Short || a instanceof Byte ) {
            return Math.abs( a.intValue() );
        }

        if( a instanceof Long ) {
            return Math.abs( a.intValue() );
        }

        if( a instanceof BigInteger ) {
            return ((BigInteger) a).abs();
        }

        if( a instanceof BigDecimal ) {
            return ((BigDecimal) a).abs();
        }

        return Math.abs( a.doubleValue() );
    }

    public static Number cbrt( Number a )
    {
        return a == null ? null : Math.cbrt( a.doubleValue() );
    }

    public static Number sqrt( Number a )
    {
        return a == null ? null : Math.sqrt( a.doubleValue() );
    }

    public static Number exp( Number a )
    {
        return a == null ? null : Math.exp( a.doubleValue() );
    }

    public static Number expm1( Number a )
    {
        return a == null ? null : Math.expm1( a.doubleValue() );
    }

    public static Number log( Number a )
    {
        return a == null ? null : Math.log( a.doubleValue() );
    }

    public static Number log10( Number a )
    {
        return a == null ? null : Math.log10( a.doubleValue() );
    }

    public static Number log1p( Number a )
    {
        return a == null ? null : Math.log1p( a.doubleValue() );
    }

    public static Number pow( Number a, Number b )
    {
        return a == null ? null : Math.pow( a.doubleValue(), b.doubleValue() );
    }

}
