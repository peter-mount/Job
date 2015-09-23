/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job;

/**
 *
 * @author peter
 */
public interface Scope
        extends AutoCloseable
{

    /**
     * Create a new scope
     * <p>
     * @return
     */
    static Scope newInstance()
    {
        return new AbstractScope.GlobalScope();
    }

    /**
     * Does a variable exist in this scope
     * <p>
     * @param name variable name
     * <p>
     * @return
     */
    boolean exists( String name );

    /**
     * Return the value of a variable
     * <p>
     * @param <T>  Type expected
     * @param name variable name
     * <p>
     * @return value or null
     */
    <T> T getVar( String name );

    /**
     * Set the value of a variable. The new value will only hold within this specific scope
     * <p>
     * @param <T>  Type of variable
     * @param name variable name
     * @param val  value to set
     * <p>
     * @return original value or null if not present
     */
    <T> T setVar( String name, T val );

    /**
     * begin a new variable scope
     * <p>
     * @return new scope
     */
    Scope begin();
}
