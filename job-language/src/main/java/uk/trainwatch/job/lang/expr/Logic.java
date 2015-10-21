/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.expr;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Objects;
import uk.trainwatch.job.Scope;
import static uk.trainwatch.job.lang.expr.Arithmetic.convert;
import static uk.trainwatch.job.lang.expr.Arithmetic.decode;

/**
 *
 * @author peter
 */
public class Logic
{

    private static final Comparator natural = Comparator.naturalOrder();

    public static boolean isTrue( Object v )
    {
        return v != null && !isFalse( v );
    }

    public static boolean isFalse( Object o )
    {
        if( o == null ) {
            return false;
        }
        Object v = decode( o );
        return v == Boolean.FALSE
               || v == (Integer) 0
               || v == (Double) 0.0
               || v == (Long) 0L
               || BigInteger.ZERO.equals( v )
               || BigDecimal.ZERO.equals( v );
    }

    public static ExpressionOperation trueOp()
    {
        return ( s, a ) -> Boolean.TRUE;
    }

    public static ExpressionOperation falseOp()
    {
        return ( s, a ) -> Boolean.FALSE;
    }

    /**
     * expr ? trueExpr : falseExpr
     * <p>
     * @param expr
     * @param trueExpr
     * @param falseExpr <p>
     * @return
     */
    public static ExpressionOperation conditional( ExpressionOperation expr, ExpressionOperation trueExpr,
                                                   ExpressionOperation falseExpr )
    {
        return ( s, a ) -> isTrue( expr.invoke( s ) ) ? trueExpr.invoke( s ) : falseExpr.invoke( s );
    }

    /**
     * lhs || rhs
     * <p>
     * @param lhs
     * @param rhs <p>
     * @return
     */
    public static ExpressionOperation conditionalOr( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> isTrue( lhs.invoke( s ) ) || isTrue( rhs.invoke( s ) );
    }

    /**
     * lhs && rhs
     * <p>
     * @param lhs
     * @param rhs <p>
     * @return
     */
    public static ExpressionOperation conditionalAnd( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> isTrue( lhs.invoke( s ) ) && isTrue( rhs.invoke( s ) );
    }

    /**
     * Inclusive OR lhs | rhs
     * <p>
     * @param lhs
     * @param rhs <p>
     * @return
     */
    public static ExpressionOperation inclusiveOr( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> {
            Number l = Constants.toNumber( lhs.invoke( s ) );
            Number r = convert( l, Constants.toNumber( rhs.invoke( s ) ) );
            if( l instanceof BigInteger ) {
                return ((BigInteger) l).or( (BigInteger) r );
            }
            if( l instanceof BigDecimal ) {
                throw new UnsupportedOperationException( "inclusive or not supported for BigDecimal" );
            }
            return l.longValue() | r.longValue();
        };
    }

    /**
     * Exclusive or lhs ^ rhs
     * <p>
     * @param lhs
     * @param rhs <p>
     * @return
     */
    public static ExpressionOperation exclusiveOr( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> {
            Number l = Constants.toNumber( lhs.invoke( s ) );
            Number r = convert( l, Constants.toNumber( rhs.invoke( s ) ) );
            if( l instanceof BigInteger ) {
                return ((BigInteger) l).xor( (BigInteger) r );
            }
            if( l instanceof BigDecimal ) {
                throw new UnsupportedOperationException( "xor not supported for BigDecimal" );
            }
            return l.longValue() ^ r.longValue();
        };
    }

    public static ExpressionOperation and( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> {
            Number l = Constants.toNumber( lhs.invoke( s ) );
            Number r = convert( l, Constants.toNumber( rhs.invoke( s ) ) );
            if( l instanceof BigInteger ) {
                return ((BigInteger) l).and( (BigInteger) r );
            }
            if( l instanceof BigDecimal ) {
                throw new UnsupportedOperationException( "and not supported for BigDecimal" );
            }
            return l.longValue() & r.longValue();
        };
    }

    public static ExpressionOperation equality( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> lhs.invoke( s ) == rhs.invoke( s );
    }

    public static ExpressionOperation inequality( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> lhs.invoke( s ) != rhs.invoke( s );
    }

    private static int compare( Scope scope, ExpressionOperation lhs, ExpressionOperation rhs )
            throws Exception
    {
        Object l = lhs.invoke( scope );
        Object r = rhs.invoke( scope );
        return l == r ? 0 : l == null ? -1 : r == null ? 1 : Objects.compare( l, r, natural );
    }

    public static ExpressionOperation lessThan( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> compare( s, lhs, rhs ) < 0;
    }

    public static ExpressionOperation lessThanEqual( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> compare( s, lhs, rhs ) <= 0;
    }

    public static ExpressionOperation greaterThanEqual( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> compare( s, lhs, rhs ) >= 0;
    }

    public static ExpressionOperation greaterThan( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> compare( s, lhs, rhs ) > 0;
    }

    public static ExpressionOperation shiftLeft( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> Constants.toNumber( lhs.invoke( s ) ).longValue() << Constants.toNumber( rhs.invoke( s ) ).longValue();
    }

    public static ExpressionOperation shiftRight( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> Constants.toNumber( lhs.invoke( s ) ).longValue() >> Constants.toNumber( rhs.invoke( s ) ).longValue();
    }

    public static ExpressionOperation shiftRightClear( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return ( s, a ) -> Constants.toNumber( lhs.invoke( s ) ).longValue() >> Constants.toNumber( rhs.invoke( s ) ).longValue();
    }

}
