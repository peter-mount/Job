/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.commons.math;

/**
 *
 * @author peter
 */
public class Trigonometry
{

    public static Number sin( Number a )
    {
        return Math.sin( a.doubleValue() );
    }

    public static Number asin( Number a )
    {
        return Math.asin( a.doubleValue() );
    }

    public static Number sinh( Number a )
    {
        return Math.sinh( a.doubleValue() );
    }

    public static Number cos( Number a )
    {
        return Math.cos( a.doubleValue() );
    }

    public static Number acos( Number a )
    {
        return Math.acos( a.doubleValue() );
    }

    public static Number cosh( Number a )
    {
        return Math.cosh( a.doubleValue() );
    }

    public static Number tan( Number a )
    {
        return Math.tan( a.doubleValue() );
    }

    public static Number atan( Number a )
    {
        return Math.atan( a.doubleValue() );
    }

    public static Number atan2( Number a, Number b )
    {
        return Math.atan2( a.doubleValue(), b.doubleValue() );
    }

    public static Number tanh( Number a )
    {
        return Math.tanh( a.doubleValue() );
    }

    public static Number hypot( Number a, Number b )
    {
        return a == null ? null : Math.hypot( a.doubleValue(), b.doubleValue() );
    }

    public static Number toDegrees( Number a )
    {
        return Math.toDegrees( a.doubleValue() );
    }

    public static Number toRadians( Number a )
    {
        return Math.toRadians( a.doubleValue() );
    }

}
