/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.expr;

import java.util.Objects;

/**
 *
 * @author peter
 */
public class Arithmetic
{

    public static ExpressionOperation add( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> {
            Object lv = lhs.invoke( scope );
            Object rv = rhs.invoke( scope );
            
            // Handle appending to a string
            if( lv instanceof String || rv instanceof String ) {
                return Objects.toString( lv ) + Objects.toString( rv );
            }

            Number l = Constants.toNumber( lv );
            Number r = Constants.toNumber( rv );
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
            throw new IllegalStateException( "Cannot add " + l.getClass() + " with " + r.getClass() );
        };
    }

    public static ExpressionOperation sub( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> {
            Number l = Constants.toNumber( lhs.invoke( scope ) );
            Number r = Constants.toNumber( rhs.invoke( scope ) );
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
            throw new IllegalStateException( "Cannot subtract " + l.getClass() + " with " + r.getClass() );
        };
    }

    public static ExpressionOperation mult( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> {
            Number l = Constants.toNumber( lhs.invoke( scope ) );
            Number r = Constants.toNumber( rhs.invoke( scope ) );
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
            throw new IllegalStateException( "Cannot multiply " + l.getClass() + " with " + r.getClass() );
        };
    }

    public static ExpressionOperation div( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> {
            Number l = Constants.toNumber( lhs.invoke( scope ) );
            Number r = Constants.toNumber( rhs.invoke( scope ) );
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
            throw new IllegalStateException( "Cannot div " + l.getClass() + " with " + r.getClass() );
        };
    }

    public static ExpressionOperation mod( ExpressionOperation lhs, ExpressionOperation rhs )
    {
        return scope -> {
            Number l = Constants.toNumber( lhs.invoke( scope ) );
            Number r = Constants.toNumber( rhs.invoke( scope ) );
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
            throw new IllegalStateException( "Cannot mod " + l.getClass() + " with " + r.getClass() );
        };
    }

    public static ExpressionOperation negate( ExpressionOperation expr )
    {
        return scope -> {
            Number l = Constants.toNumber( expr.invoke( scope ) );
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
            throw new IllegalStateException( "Cannot negate " + l.getClass() );
        };
    }

    public static ExpressionOperation tilde( ExpressionOperation expr )
    {
        return scope -> {
            Number l = Constants.toNumber( expr.invoke( scope ) );
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
            throw new IllegalStateException( "Cannot ~ " + l.getClass() );
        };
    }

    public static ExpressionOperation not( ExpressionOperation expr )
    {
        return scope -> !Logic.isTrue( expr.invoke( scope ) );
    }

}
