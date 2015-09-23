/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.block;

import java.io.Serializable;
import java.util.Objects;
import java.util.logging.Level;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.lang.Operation;
import uk.trainwatch.job.lang.Statement;

/**
 *
 * @author peter
 */
public class Log
        implements Statement,
                   Serializable
{

    private static final long serialVersionUID = 1L;
    private final Level level;
    private final Operation<Object> string;

    public Log( Level level, Operation<Object> string )
    {
        this.level = level;
        this.string = string;
    }

    @Override
    public void invokeStatement( Scope scope )
            throws Exception
    {
        try {
            scope.getLogger().
                    log( level, () -> {
                        try {
                            return Objects.toString( string.invoke( scope ) );
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
    }

}
