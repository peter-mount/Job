/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.expr;

import java.util.Map;
import java.util.Objects;

/**
 *
 * @author peter
 */
public class Assignment
{

    /**
     * Get a variable value
     *
     * @param name
     *             <p>
     * @return
     */
    public static ExpressionOperation getVariable( String name )
    {
     //   System.out.println( "getVariable " + name );
//        if( "lamb".equals(name))throw new RuntimeException();
        return ( s, a ) -> s.getVar( name );
    }

    /**
     * Set a variable with the result of an expression
     *
     * @param name
     * @param assignment
     *                   <p>
     * @return
     */
    public static ExpressionOperation setVariable( String name, ExpressionOperation assignment )
    {
        Objects.requireNonNull( name, "No name provided to set variable" );
        Objects.requireNonNull( assignment, "No assignment provided to set variable" );
        return ( s, a ) -> {
            Object val = assignment.invoke( s );
      //      System.out.println( "setVariable " + name + " = " + val );
            s.setVar( name, val );
            return val;
        };
    }

    /**
     * Get the named field from some object
     * <p>
     * @param src
     * @param name
     *             <p>
     * @return
     */
    public static ExpressionOperation getField( ExpressionOperation src, String name )
    {
        return ( s, a ) -> {
            Object o = Objects.requireNonNull( src.invoke( s ), "Cannot dereference null for " + name );

            if( o instanceof Map ) {
                return ((Map) o).get( name );
            }

            throw new UnsupportedOperationException( "Unable to locate " + name + " in " + o );
        };
    }

    /**
     * Get the named field from some object
     * <p>
     * @param src
     * @param name
     *             <p>
     * @return
     */
    public static ExpressionOperation setField( ExpressionOperation src, String name, ExpressionOperation expr )
    {
        return ( s, a ) -> {
            Object o = Objects.requireNonNull( src.invoke( s ), "Cannot dereference null for " + name );

            Object v = expr.invoke( s );

            if( o instanceof Map ) {
                return ((Map) o).put( name, v );
            }

            throw new UnsupportedOperationException( "Unable to locate " + name + " in " + o );
        };
    }

}
