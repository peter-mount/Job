/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.ext;

import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.expr.ExpressionOperation;

/**
 *
 * @author peter
 */
public interface Extension
{

    /**
     * The name of this Extension
     *
     * @return
     */
    String getName();

    /**
     * The version of this Extension
     *
     * @return
     */
    String getVersion();

    /**
     * Initialise the extension. Note this could be called multiple times so implementations must be able to cope with that.
     *
     * @throws Exception
     */
    default void init()
            throws Exception
    {
    }

    /**
     * Construct an object
     * <p>
     * @param type Type/name of the object
     * @param exp  Arguments for this construction
     * <p>
     * @return ExpressionOperation or null if not supported
     */
    default ExpressionOperation construct( String type, ExpressionOperation... exp )
    {
        return null;
    }

    /**
     * Retrieve a statement given it's name
     *
     * @param name Name of statement
     * <p>
     * @param args
     *             <p>
     * @return Statement or null
     */
    default Statement getStatement( String name, ExpressionOperation... args )
    {
        return null;
    }

    /**
     *
     * @param name Function name
     * @param args Arguments
     * <p>
     * @return ExpressionOperaton or null if not supported
     */
    default ExpressionOperation getExpression( String name, ExpressionOperation... args )
    {
        return null;
    }

    /**
     *
     * @param src  ExpressionOperation to return the object to invoke this expression against
     * @param name Function name
     * @param args Arguments
     * <p>
     * @return ExpressionOperaton or null if not supported
     */
    default ExpressionOperation getExpression( ExpressionOperation src, String name, ExpressionOperation... args )
    {
        return null;
    }

    /**
     * Returns a Statement if this extension supports Job Output
     *
     * @param name
     * @param args
     *
     * @return
     */
    default Statement getOutputStatement( String name, ExpressionOperation... args )
    {
        return null;
    }
}
