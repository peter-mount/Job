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
public class Assignment
{

    /**
     * Get a variable value
     *
     * @param name
     * @return
     */
    public static ExpressionOperation getVariable( String name )
    {
        return ( s, a ) -> s.getVar( name );
    }

    /**
     * Set a variable with the result of an expression
     *
     * @param name
     * @param assignment
     * @return
     */
    public static ExpressionOperation setVariable( String name, ExpressionOperation assignment )
    {
        Objects.requireNonNull( name, "No name provided to set variable" );
        Objects.requireNonNull( assignment, "No assignment provided to set variable" );
        return ( s, a ) ->
        {
            Object val = assignment.invoke( s );
            s.setVar( name, val );
            return val;
        };
    }

}
