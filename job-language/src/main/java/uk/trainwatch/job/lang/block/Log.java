/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.block;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.lang.Statement;
import static uk.trainwatch.job.lang.expr.Arithmetic.decode;
import uk.trainwatch.job.lang.expr.ExpressionOperation;
import static uk.trainwatch.job.lang.expr.Arithmetic.decode;

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
        return ( s, a ) -> {
            Job job = s.getJob();
            Logger log = s.getLogger();

            boolean jobLog = job.isLoggable( level );
            boolean logLog = log != null && log.isLoggable( level );
            if( jobLog || logLog ) {
                String m = Objects.toString( decode( expr.invoke( s ) ) );

                // Log to the job log
                if( jobLog ) {
                    job.log( level, m );
                }

                // Log to the system log
                if( logLog ) {
                    log.log( level, m );
                }
            }
        };
    }

}
