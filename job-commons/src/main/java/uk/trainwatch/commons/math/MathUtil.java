/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.commons.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.function.Predicate;

/**
 *
 * @author peter
 */
public class MathUtil
{

    /**
     * Convert Number b to the same type as Number a
     * <p>
     * @param a
     * @param b
     *          <p>
     * @return
     */
    public static Number convert( Class<? extends Number> a, Number b )
    {
        if( a == null || b == null ) {
            return null;
        }

        // We treat shorts as ints
        if( a.isAssignableFrom( Integer.class ) || a.isAssignableFrom( Short.class ) ) {
            return b.intValue();
        }

        if( a.isAssignableFrom( Long.class ) ) {
            return b.longValue();
        }

        if( a.isAssignableFrom( Double.class ) ) {
            return b.doubleValue();
        }

        if( a.isAssignableFrom( Float.class ) ) {
            return b.floatValue();
        }

        if( a.isAssignableFrom( Byte.class ) ) {
            return b.byteValue();
        }

        if( a.isAssignableFrom( BigInteger.class ) ) {
            return b instanceof BigInteger ? (BigInteger) b : BigInteger.valueOf( b.longValue() );
        }

        if( a.isAssignableFrom( BigDecimal.class ) ) {
            if( b instanceof BigDecimal ) {
                return (BigDecimal) b;
            }
            else if( b instanceof BigInteger ) {
                return new BigDecimal( (BigInteger) b );
            }
            else if( b instanceof Integer || b instanceof Short ) {
                return new BigDecimal( b.intValue() );
            }
            else if( b instanceof Long ) {
                return new BigDecimal( b.longValue() );
            }
            else if( b instanceof Double || b instanceof Float ) {
                return new BigDecimal( b.doubleValue() );
            }
            else {
                return new BigDecimal( b.toString() );
            }
        }

        throw new NumberFormatException( "Unable to convert " + b.getClass() + " into " + a.getClass() );
    }

    /**
     * Convert Number b to the same type as Number a
     * <p>
     * @param a
     * @param b
     *          <p>
     * @return
     */
    public static Number convert( Number a, Number b )
    {
        if( a == null ) {
            return null;
        }

        // We treat shorts as ints
        if( a instanceof Integer || a instanceof Short ) {
            return b.intValue();
        }

        if( a instanceof Long ) {
            return b.longValue();
        }

        if( a instanceof Double ) {
            return b.doubleValue();
        }

        if( a instanceof Float ) {
            return b.floatValue();
        }

        if( a instanceof Byte ) {
            return b.byteValue();
        }

        if( a instanceof BigInteger ) {
            return b instanceof BigInteger ? (BigInteger) b : BigInteger.valueOf( b.longValue() );
        }

        if( a instanceof BigDecimal ) {
            if( b instanceof BigDecimal ) {
                return (BigDecimal) b;
            }
            else if( b instanceof BigInteger ) {
                return new BigDecimal( (BigInteger) b );
            }
            else if( b instanceof Integer || b instanceof Short || b instanceof Byte ) {
                return new BigDecimal( b.intValue() );
            }
            else if( b instanceof Long ) {
                return new BigDecimal( b.longValue() );
            }
            else if( b instanceof Double || b instanceof Float ) {
                return new BigDecimal( b.doubleValue() );
            }
            else {
                return new BigDecimal( b.toString() );
            }
        }

        throw new NumberFormatException( "Unable to convert " + b.getClass() + " into " + a.getClass() );
    }

    public static int compareTo( Number a, Number b )
    {
        if( a == b ) {
            return 0;
        }
        if( a instanceof Comparable ) {
            return ((Comparable) a).compareTo( convert( a, b ) );
        }

        throw new IllegalArgumentException( "Unable to compare " + a + " with " + b );
    }

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
