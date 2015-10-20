/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.sql;

import java.sql.Connection;
import uk.trainwatch.job.Scope;

/**
 * An invoker that invokes a PostgreSQL function
 * <p>
 * @author peter
 */
@FunctionalInterface
public interface PostgreSQLInvoker
{

    /**
     * Invoke an expression
     * <p>
     * @param s    Language Scope
     * @param con  JDBC Connection
     * @param args Function arguments
     * <p>
     * @return result type
     * <p>
     * @throws Exception on failure
     */
    Object invoke( Scope s, Connection con, Object... args )
            throws Exception;

    /**
     * Return true if this invoker can be used in a try-resources block
     * <p>
     * @return
     */
    default boolean isResourcable()
    {
        return false;
    }
}
