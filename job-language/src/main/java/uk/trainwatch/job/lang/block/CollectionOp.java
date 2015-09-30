/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import uk.trainwatch.job.lang.expr.Arithmetic;
import uk.trainwatch.job.lang.expr.Constants;
import uk.trainwatch.job.lang.expr.ExpressionOperation;

/**
 *
 * @author peter
 */
public class CollectionOp
{

    public static ExpressionOperation clear( ExpressionOperation assign )
    {
        return s -> {
            Collection c = (Collection) Objects.requireNonNull( assign.invoke( s ), "Null collection" );
            c.clear();
            return c;
        };
    }

    public static ExpressionOperation append( ExpressionOperation assign, ExpressionOperation exp )
    {
        return s -> {
            Collection c = (Collection) Objects.requireNonNull( assign.invoke( s ), "Null collection" );
            c.add( exp.invoke( s ) );
            return c;
        };
    }

    public static ExpressionOperation prepend( ExpressionOperation assign, ExpressionOperation exp )
    {
        return s -> {
            Collection c = Objects.requireNonNull( (Collection) assign.invoke( s ), "Null collection" );
            Object val = exp.invoke( s );
            if( c instanceof List ) {
                ((List) c).add( 0, val );
            }
            else if( c instanceof Deque ) {
                ((Deque) c).addFirst( val );
            }
            else {
                c.add( val );
            }
            return c;
        };
    }

    public static ExpressionOperation newList()
    {
        return s -> new ArrayList<>();
    }

    public static ExpressionOperation newList( ExpressionOperation exp )
    {
        return s -> {
            Object val = exp.invoke( s );
            if( val instanceof Collection ) {
                return new ArrayList<>( (Collection) val );
            }
            else {
                return new ArrayList<>( Constants.toNumber( val ).intValue() );
            }
        };
    }

}
