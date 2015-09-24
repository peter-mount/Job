/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.expr;

/**
 *
 * @author peter
 */
public class Assignment
{

    public static ExpressionOperation setVariable( String name, ExpressionOperation assignment )
    {
        return scope ->
        {
            Object val = assignment.invoke( scope );
            scope.setVar( name, val );
            return val;
        };
    }

}
