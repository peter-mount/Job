/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.output;

import java.util.Arrays;
import java.util.Objects;
import org.kohsuke.MetaInfServices;
import uk.trainwatch.job.AbstractScope;
import uk.trainwatch.job.ext.Extension;
import uk.trainwatch.job.lang.Operation;
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.block.TypeOp;
import static uk.trainwatch.job.lang.expr.Arithmetic.decode;
import uk.trainwatch.job.lang.expr.ExpressionOperation;

/**
 * Implements print, println and printf functionality.
 *
 * TODO: Allow this to redirect to a specific PrintWriter
 *
 * @author peter
 */
@MetaInfServices(Extension.class)
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

    private String toString( Object o )
    {
        if( o instanceof Operation ) {
            try {
                return Objects.toString( ((Operation) o).invoke( AbstractScope.getCurrentScope() ) );
            }
            catch( Exception ex ) {
                throw new RuntimeException( ex );
            }
        }
        else {
            return Objects.toString( o );
        }
    }

    @Override
    public Statement getStatement( String name, ExpressionOperation... args )
    {
        switch( name ) {
            case "print":
            case "println":
                switch( args.length ) {
                    case 0:
                        return null;
                    case 1:
                        ExpressionOperation p1 = args[0];
                        return ( s, a ) -> s.getJob().getJobOutput().print( Objects.toString( decode( p1.invoke( s, a ) ) ) );

                    default:
                        return ( s, a ) -> {
                            for( Object arg: TypeOp.invokeArguments( s, args ) ) {
                                s.getJob().getJobOutput().print( Objects.toString( decode( arg ) ) );
                            }
                        };
                }

            case "printf":
                switch( args.length ) {
                    case 0:
                        return null;

                    default:
                        return ( s, a ) -> {
                            Object arg[] = TypeOp.invokeArguments( s, args );
                            if( arg.length > 1 ) {
                                s.getJob().getJobOutput().printf( Objects.toString( arg[0] ), Arrays.copyOfRange( arg, 1, arg.length ) );
                            }
                        };
                }

            default:
                return null;
        }
    }

}
