/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.lang.Operation;

/**
 *
 * @author peter
 */
@FunctionalInterface
public interface ExpressionOperation
        extends Operation<Object, Object>
{

    /**
     * Invoke this expression returning a String
     *
     * @param s
     *
     * @return
     *
     * @throws Exception
     */
    default String getString( Scope s )
            throws Exception
    {
        return Objects.toString( invoke( s ), null );
    }

    /**
     * Invoke this expression returning an int.
     *
     * @param s
     *
     * @return
     *
     * @throws Exception
     */
    default int getInt( Scope s )
            throws Exception
    {
        Number n = (Number) invoke( s );
        return n == null ? 0 : n.intValue();
    }

    /**
     * Invoke this expression returning a double
     *
     * @param s
     *
     * @return
     *
     * @throws Exception
     */
    default Double getDouble( Scope s )
            throws Exception
    {
        Number n = (Number) invoke( s );
        return n == null ? 0 : n.doubleValue();
    }

    /**
     * Invoke this expression casting to a specific class
     *
     * @param <T>
     * @param s
     *
     * @return
     *
     * @throws Exception
     */
    default <T> T get( Scope s )
            throws Exception
    {
        return (T) invoke( s );
    }

    /**
     * Invoke and return true if the result is equivalent to true.
     * <p>
     * This is the equivalent of {@code Logic.isTrue( s )}
     *
     * @param s
     *
     * @return
     *
     * @throws Exception
     */
    default boolean isTrue( Scope s )
            throws Exception
    {
        return Logic.isTrue( s );
    }

    /**
     * Invoke and return true if the result is equivalent to false.
     * <p>
     * This is the equivalent of {@code Logic.isFalse( s )}
     *
     * @param s
     *
     * @return
     *
     * @throws Exception
     */
    default boolean isFalse( Scope s )
            throws Exception
    {
        return Logic.isFalse( s );
    }

    static List<Object> invoke( ExpressionOperation ops[], Scope s )
            throws Exception
    {
        List<Object> l = new ArrayList<>( ops.length );
        for( ExpressionOperation op: ops ) {
            l.add( op.invoke( s ) );
        }
        return l;
    }
}
