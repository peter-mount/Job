/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.expr;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 *
 * @author peter
 */
public class Arithmetic
{

    /**
     * Decodes a value.
     * <p>
     * For example, if o is a {@link JsonValue} then we return the correct value it represents
     * <p>
     * @param o
     *          <p>
     * @return
     */
    public static Object decode( Object o )
    {
        if( o instanceof JsonValue ) {
            JsonValue v = (JsonValue) o;
            switch( v.getValueType() ) {
                case FALSE:
                    return false;

                case NULL:
                    return null;

                case NUMBER:
                    JsonNumber n = (JsonNumber) v;
                    return n.isIntegral() ? n.bigIntegerValue() : n.bigDecimalValue();

                case TRUE:
                    return true;

                case STRING:
                    return ((JsonString) v).getString();

                // All others are subclasses
                default:
                    return v;
            }
        }

        return o;
    }

    public static Object[] decode( Object[] a )
    {
        if( a != null ) {
            for( int i = 0; i < a.length; i++ ) {
                a[i] = decode( a );
            }
        }
        return a;
    }

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

    public static ExpressionOperation add( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> {
            Object lv = lhs.invoke( s );
            Object rv = rhs.invoke( s );

            // Handle appending to a string
            if( lv instanceof String || rv instanceof String ) {
                return Objects.toString( lv ) + Objects.toString( rv );
            }

            Number l = Constants.toNumber( lv );
            Number r = convert( l, Constants.toNumber( rv ) );
            if( l instanceof Integer ) {
                return l.intValue() + r.intValue();
            }
            if( l instanceof Long ) {
                return l.longValue() + r.longValue();
            }
            if( l instanceof Double ) {
                return l.doubleValue() + r.doubleValue();
            }
            if( l instanceof Float ) {
                return l.floatValue() + r.floatValue();
            }
            if( l instanceof Short ) {
                return l.shortValue() + r.shortValue();
            }
            if( l instanceof Byte ) {
                return l.byteValue() + r.byteValue();
            }
            if( l instanceof BigInteger ) {
                return ((BigInteger) l).add( (BigInteger) r );
            }
            if( l instanceof BigDecimal ) {
                return ((BigDecimal) l).add( (BigDecimal) r );
            }

            throw new IllegalStateException( "Cannot add " + l.getClass() + " with " + r.getClass() );
        };
    }

    public static ExpressionOperation sub( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> {
            Number l = Constants.toNumber( lhs.invoke( s ) );
            Number r = convert( l, Constants.toNumber( rhs.invoke( s ) ) );
            if( l instanceof Integer ) {
                return l.intValue() - r.intValue();
            }
            if( l instanceof Long ) {
                return l.longValue() - r.longValue();
            }
            if( l instanceof Double ) {
                return l.doubleValue() - r.doubleValue();
            }
            if( l instanceof Float ) {
                return l.floatValue() - r.floatValue();
            }
            if( l instanceof Short ) {
                return l.shortValue() - r.shortValue();
            }
            if( l instanceof Byte ) {
                return l.byteValue() - r.byteValue();
            }
            if( l instanceof BigInteger ) {
                return ((BigInteger) l).subtract( (BigInteger) r );
            }
            if( l instanceof BigDecimal ) {
                return ((BigDecimal) l).subtract( (BigDecimal) r );
            }
            throw new IllegalStateException( "Cannot subtract " + l.getClass() + " with " + r.getClass() );
        };
    }

    public static ExpressionOperation mult( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> {
            Number l = Constants.toNumber( lhs.invoke( s ) );
            Number r = convert( l, Constants.toNumber( rhs.invoke( s ) ) );
            if( l instanceof Integer ) {
                return l.intValue() * r.intValue();
            }
            if( l instanceof Long ) {
                return l.longValue() * r.longValue();
            }
            if( l instanceof Double ) {
                return l.doubleValue() * r.doubleValue();
            }
            if( l instanceof Float ) {
                return l.floatValue() * r.floatValue();
            }
            if( l instanceof Short ) {
                return l.shortValue() * r.shortValue();
            }
            if( l instanceof Byte ) {
                return l.byteValue() * r.byteValue();
            }
            if( l instanceof BigInteger ) {
                return ((BigInteger) l).multiply( (BigInteger) r );
            }
            if( l instanceof BigDecimal ) {
                return ((BigDecimal) l).multiply( (BigDecimal) r );
            }
            throw new IllegalStateException( "Cannot multiply " + l.getClass() + " with " + r.getClass() );
        };
    }

    public static ExpressionOperation div( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> {
            Number l = Constants.toNumber( lhs.invoke( s ) );
            Number r = convert( l, Constants.toNumber( rhs.invoke( s ) ) );
            if( l instanceof Integer ) {
                return l.intValue() / r.intValue();
            }
            if( l instanceof Long ) {
                return l.longValue() / r.longValue();
            }
            if( l instanceof Double ) {
                return l.doubleValue() / r.doubleValue();
            }
            if( l instanceof Float ) {
                return l.floatValue() / r.floatValue();
            }
            if( l instanceof Short ) {
                return l.shortValue() / r.shortValue();
            }
            if( l instanceof Byte ) {
                return l.byteValue() / r.byteValue();
            }
            if( l instanceof BigInteger ) {
                return ((BigInteger) l).divide( (BigInteger) r );
            }
            if( l instanceof BigDecimal ) {
                return ((BigDecimal) l).divide( (BigDecimal) r );
            }
            throw new IllegalStateException( "Cannot div " + l.getClass() + " with " + r.getClass() );
        };
    }

    public static ExpressionOperation mod( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> {
            Number l = Constants.toNumber( lhs.invoke( s ) );
            Number r = convert( l, Constants.toNumber( rhs.invoke( s ) ) );
            if( l instanceof Integer ) {
                return l.intValue() % r.intValue();
            }
            if( l instanceof Long ) {
                return l.longValue() % r.longValue();
            }
            if( l instanceof Double ) {
                return l.doubleValue() % r.doubleValue();
            }
            if( l instanceof Float ) {
                return l.floatValue() % r.floatValue();
            }
            if( l instanceof Short ) {
                return l.shortValue() % r.shortValue();
            }
            if( l instanceof Byte ) {
                return l.byteValue() % r.byteValue();
            }

            if( l instanceof BigDecimal ) {
                l = convert( BigInteger.class, l );
                r = convert( BigInteger.class, r );
            }
            if( l instanceof BigInteger ) {
                return ((BigInteger) l).mod( (BigInteger) r );
            }

            throw new IllegalStateException( "Cannot mod " + l.getClass() + " with " + r.getClass() );
        };
    }

    public static ExpressionOperation negate( ExpressionOperation expr )
    {
        return ( s, a ) -> {
            Number l = Constants.toNumber( expr.invoke( s ) );
            if( l instanceof Integer ) {
                return -l.intValue();
            }
            if( l instanceof Long ) {
                return -l.longValue();
            }
            if( l instanceof Double ) {
                return -l.doubleValue();
            }
            if( l instanceof Float ) {
                return -l.floatValue();
            }
            if( l instanceof Short ) {
                return -l.shortValue();
            }
            if( l instanceof Byte ) {
                return -l.byteValue();
            }
            if( l instanceof BigInteger ) {
                return ((BigInteger) l).negate();
            }
            if( l instanceof BigDecimal ) {
                return ((BigDecimal) l).negate();
            }
            throw new IllegalStateException( "Cannot negate " + l.getClass() );
        };
    }

    public static ExpressionOperation tilde( ExpressionOperation expr )
    {
        return ( s, a ) -> {
            Number l = Constants.toNumber( expr.invoke( s ) );
            if( l instanceof Integer ) {
                return ~l.intValue();
            }
            if( l instanceof Long ) {
                return ~l.longValue();
            }
            if( l instanceof Short ) {
                return ~l.shortValue();
            }
            if( l instanceof Byte ) {
                return ~l.byteValue();
            }
            if( l instanceof BigInteger ) {
                return ((BigInteger) l).not();
            }
            throw new IllegalStateException( "Cannot ~ " + l.getClass() );
        };
    }

    public static ExpressionOperation not( ExpressionOperation expr )
    {
        return ( s, a ) -> !Logic.isTrue( expr.invoke( s ) );
    }

}
