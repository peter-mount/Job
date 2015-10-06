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
 * @param <T>
 */
@FunctionalInterface
public interface Operation<R, T>
{

    R invoke( Scope scope, T... args )
            throws Exception;

    /**
     * A nop operation
     * 
     * @param <O>
     * @param <R>
     * @param <T>
     * @return 
     */
    static <O extends Operation<R, T>, R, T> O nop()
    {
        Operation<R, T> o = ( s, a )
                ->
                {
                    return null;
                };
        return (O) o;
    }

}
