/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.output;

import java.util.Arrays;
import java.util.Objects;
import org.kohsuke.MetaInfServices;
import uk.trainwatch.job.ext.Extension;
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.block.TypeOp;
import uk.trainwatch.job.lang.expr.ExpressionOperation;

/**
 * Implements print, println and printf functionality.
 *
 * TODO: Allow this to redirect to a specific PrintWriter
 *
 * @author peter
 */
@MetaInfServices( Extension.class )
public class PrintExtension
        implements Extension
{

    @Override
    public String getName()
    {
        return "Printing";
    }

    @Override
    public String getVersion()
    {
        return "1.0";
    }

    @Override
    public Statement getStatement( String name )
    {
        switch( name )
        {
            case "print":
                return ( s, a ) ->
                {
                    Object args[] = TypeOp.invokeArguments( s, (ExpressionOperation[]) a );
                    for( Object arg : args )
                    {
                        System.out.print( arg );
                    }
                };
            case "println":
                return ( s, a ) ->
                {
                    Object args[] = TypeOp.invokeArguments( s, (ExpressionOperation[]) a );
                    for( Object arg : args )
                    {
                        System.out.println( arg );
                    }
                };
            case "printf":
                return ( s, a ) ->
                {
                    Object args[] = TypeOp.invokeArguments( s, (ExpressionOperation[]) a );
                    if( args.length > 1 )
                    {
                        System.out.printf( Objects.toString( args[0] ), Arrays.copyOfRange( args, 1, args.length ) );
                    }
                };
            default:
                return null;
        }
    }

}
