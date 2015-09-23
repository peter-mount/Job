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
public class Constants
{

    public static ExpressionOperation constant( Object v )
    {
        return s -> v;
    }

    public static Number toNumber( Object v )
    {
        if( v instanceof Number ) {
            return (Number) v;
        }
        throw new NumberFormatException( "Unable to convert " + v );
    }
}
