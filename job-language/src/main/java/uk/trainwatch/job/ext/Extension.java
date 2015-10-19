/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.ext;

import uk.trainwatch.job.Job;
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

    default void init()
            throws Exception
    {
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
}
