/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.block;

import java.util.Objects;
import java.util.logging.Level;
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.expr.ExpressionOperation;

/**
 *
 * @author peter
 */
public class Log
{

    public static Statement log( Level level, ExpressionOperation expr )
    {
        Objects.requireNonNull( level, "No level" );
        Objects.requireNonNull( expr, "No expression" );
        return (s,a) -> {
            try {
                s.getLogger().
                        log( level, () -> {
                            try {
                                return Objects.toString( expr.invoke( s ) );
                            }
                            catch( Exception ex ) {
                                throw new RuntimeException( ex );
                            }
                        } );
            }
            catch( RuntimeException ex ) {
                Throwable t = ex.getCause();
                throw t instanceof Exception ? (Exception) t : ex;
            }
        };
    }

}
