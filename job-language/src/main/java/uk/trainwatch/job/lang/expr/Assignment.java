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

    public static ExpressionOperation setVariable( String name, ExpressionOperation assignment )
    {
        Objects.requireNonNull( name, "No name provided to set variable");
        Objects.requireNonNull( assignment, "No assignment provided to set variable");
        return scope ->
        {
            Object val = assignment.invoke( scope );
            scope.setVar( name, val );
            return val;
        };
    }

}
