/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.commons.math;

import uk.trainwatch.commons.*;
import java.util.concurrent.TimeUnit;
import org.kohsuke.MetaInfServices;
import uk.trainwatch.job.ext.Extension;
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.expr.ExpressionOperation;

/**
 * Provides Mathematics extensions
 * <p>
 * <h3>Statements</h3>
 * <ul>
 * <li>sleep(s); - Sleep for s seconds</li>
 * </ul>
 * <p>
 * @author peter
 */
@MetaInfServices(Extension.class)
public class MathExtension
        implements Extension
{

    @Override
    public String getName()
    {
        return "Math";
    }

    @Override
    public String getVersion()
    {
        return "1.0";
    }

    @Override
    public ExpressionOperation getExpression( String name, ExpressionOperation... args )
    {
        final int argc = args == null ? 0 : args.length;

        switch( argc ) {
            // ====================================================================
            // Mathematical constants
            case 0:
                switch( name ) {
                    // ====================================================================
                    case "e":
                        return ( s, a ) -> Math.E;

                    case "pi":
                        return ( s, a ) -> Math.PI;

                    default:
                }
                break;

            // ====================================================================
            case 1:
                switch( name ) {

                    // ====================================================================
                    // Math functions
                    case "abs":
                        return ( s, a ) -> MathUtil.abs( ((Number) args[0].invoke( s, a )) );

                    case "cbrt":
                        return ( s, a ) -> MathUtil.cbrt( ((Number) args[0].invoke( s, a )) );

                    case "ceil":
                        return ( s, a ) -> MathUtil.ceil( ((Number) args[0].invoke( s, a )) );

                    case "exp":
                        return ( s, a ) -> MathUtil.exp( ((Number) args[0].invoke( s, a )) );

                    case "expm1":
                        return ( s, a ) -> MathUtil.expm1( ((Number) args[0].invoke( s, a )) );

                    case "log":
                        return ( s, a ) -> MathUtil.log( ((Number) args[0].invoke( s, a )) );

                    case "log10":
                        return ( s, a ) -> MathUtil.log10( ((Number) args[0].invoke( s, a )) );

                    case "log1p":
                        return ( s, a ) -> MathUtil.log1p( ((Number) args[0].invoke( s, a )) );

                    case "floor":
                        return ( s, a ) -> MathUtil.floor( ((Number) args[0].invoke( s, a )) );

                    case "round":
                        return ( s, a ) -> MathUtil.round( ((Number) args[0].invoke( s, a )) );

                    case "sqrt":
                        return ( s, a ) -> MathUtil.sqrt( ((Number) args[0].invoke( s, a )) );

                    // ====================================================================
                    // Trigonometric functions
                    case "asin":
                        return ( s, a ) -> Trigonometry.asin( ((Number) args[0].invoke( s, a )) );

                    case "acos":
                        return ( s, a ) -> Trigonometry.acos( ((Number) args[0].invoke( s, a )) );

                    case "atan":
                        return ( s, a ) -> Trigonometry.atan( ((Number) args[0].invoke( s, a )) );

                    case "cos":
                        return ( s, a ) -> Trigonometry.cos( ((Number) args[0].invoke( s, a )) );

                    case "cosh":
                        return ( s, a ) -> Trigonometry.cosh( ((Number) args[0].invoke( s, a )) );

                    case "sin":
                        return ( s, a ) -> Trigonometry.sin( ((Number) args[0].invoke( s, a )) );

                    case "sinh":
                        return ( s, a ) -> Trigonometry.sinh( ((Number) args[0].invoke( s, a )) );

                    case "tan":
                        return ( s, a ) -> Trigonometry.tan( ((Number) args[0].invoke( s, a )) );

                    case "tanh":
                        return ( s, a ) -> Trigonometry.tanh( ((Number) args[0].invoke( s, a )) );

                    default:
                }
                break;

            // ====================================================================
            // binumeral functions
            case 2:
                switch( name ) {
                    // ====================================================================
                    // Math functions
                    case "max":
                        return ( s, a ) -> MathUtil.max( ((Number) args[0].invoke( s, a )), ((Number) args[1].invoke( s, a )) );

                    case "min":
                        return ( s, a ) -> MathUtil.min( ((Number) args[0].invoke( s, a )), ((Number) args[1].invoke( s, a )) );

                    // ====================================================================
                    // Trigonometric functions
                    case "atan":
                        return ( s, a ) -> Trigonometry.atan2( ((Number) args[0].invoke( s, a )), ((Number) args[1].invoke( s, a )) );

                    case "hypot":
                        return ( s, a ) -> Trigonometry.hypot( ((Number) args[0].invoke( s, a )), ((Number) args[1].invoke( s, a )) );

                    default:
                }
                break;
                
            default:
        }

        return null;
    }

}
