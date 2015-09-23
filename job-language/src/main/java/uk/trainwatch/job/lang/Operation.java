/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang;

import uk.trainwatch.job.Scope;

/**
 * An operation to perform in a script
 * <p>
 * @author peter
 * @param <R> type of return value
 */
@FunctionalInterface
public interface Operation<R>
{

    R invoke( Scope scope )
            throws Exception;
    
    /**
     * A no operation
     */
    static final Statement NOP = s -> {
    };

}
