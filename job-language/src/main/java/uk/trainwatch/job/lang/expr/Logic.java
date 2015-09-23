/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.expr;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

/**
 *
 * @author peter
 */
public class Logic
{

    private static final Comparator natural = Comparator.naturalOrder();

    public static boolean isTrue( Object v )
    {
        return v == Boolean.TRUE || v == (Integer) 1 || v == (Double) 1.0 || v == (Long) 1L;
    }

    public static boolean isFalse( Object v )
    {
        return v == null || !isTrue( v );
    }

    public static ExpressionOperation trueOp()
    {
        return s -> Boolean.TRUE;
    }

    public static ExpressionOperation falseOp()
    {
        return s -> Boolean.FALSE;
    }

    /**
     * expr ? trueExpr : falseExpr
     * <p>
     * @param expr
     * @param trueExpr
     * @param falseExpr
     *                  <p>
     * @return
     */
    public static ExpressionOperation conditional( ExpressionOperation expr, ExpressionOperation trueExpr, ExpressionOperation falseExpr )
    {
        return scope -> isTrue( expr.invoke( scope ) ) ? trueExpr.invoke( scope ) : falseExpr.invoke( scope );
    }

    /**
     * lhs || rhs
     * <p>
     * @param lhs
     * @param rhs
     *            <p>
     * @return
     */
    public static ExpressionOperation conditionalOr( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> isTrue( lhs.invoke( scope ) ) || isTrue( rhs.invoke( scope ) );
    }

    /**
     * lhs && rhs
     * <p>
     * @param lhs
     * @param rhs
     *            <p>
     * @return
     */
    public static ExpressionOperation conditionalAnd( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> isTrue( lhs.invoke( scope ) ) && isTrue( rhs.invoke( scope ) );
    }

    /**
     * Inclusive OR lhs | rhs
     * <p>
     * @param lhs
     * @param rhs
     *            <p>
     * @return
     */
    public static ExpressionOperation inclusiveOr( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> Constants.toNumber( lhs.invoke( scope ) ).longValue() | Constants.toNumber( rhs.invoke( scope ) ).longValue();
    }

    /**
     * Exclusive or lhs ^ rhs
     * <p>
     * @param lhs
     * @param rhs
     *            <p>
     * @return
     */
    public static ExpressionOperation exclusiveOr( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> Constants.toNumber( lhs.invoke( scope ) ).longValue() ^ Constants.toNumber( rhs.invoke( scope ) ).longValue();
    }

    public static ExpressionOperation and( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> Constants.toNumber( lhs.invoke( scope ) ).longValue() & Constants.toNumber( rhs.invoke( scope ) ).longValue();
    }

    public static ExpressionOperation equality( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> lhs.invoke( scope ) == rhs.invoke( scope );
    }

    public static ExpressionOperation inequality( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> lhs.invoke( scope ) != rhs.invoke( scope );
    }

    public static ExpressionOperation lessThan( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> natural.compare( lhs.invoke( scope ), rhs.invoke( scope ) ) < 0;
    }

    public static ExpressionOperation lessThanEqual( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> natural.compare( lhs.invoke( scope ), rhs.invoke( scope ) ) <= 0;
    }

    public static ExpressionOperation greaterThanEqual( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> natural.compare( lhs.invoke( scope ), rhs.invoke( scope ) ) >= 0;
    }

    public static ExpressionOperation greaterThan( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> natural.compare( lhs.invoke( scope ), rhs.invoke( scope ) ) > 0;
    }

    public static ExpressionOperation shiftLeft( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> Constants.toNumber( lhs.invoke( scope ) ).longValue() << Constants.toNumber( rhs.invoke( scope ) ).longValue();
    }

    public static ExpressionOperation shiftRight( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> Constants.toNumber( lhs.invoke( scope ) ).longValue() >> Constants.toNumber( rhs.invoke( scope ) ).longValue();
    }

    public static ExpressionOperation shiftRightClear( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> Constants.toNumber( lhs.invoke( scope ) ).longValue() >> Constants.toNumber( rhs.invoke( scope ) ).longValue();
    }

}
