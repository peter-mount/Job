/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job;

import java.util.Collection;
import java.util.logging.Logger;
import javax.script.Bindings;
import onl.area51.job.jcl.Jcl;

/**
 *
 * @author peter
 */
public interface Scope
        extends AutoCloseable
{

    Job getJob();

    Jcl getJcl();

    /**
     * Create a new scope
     * <p>
     * @return
     */
    static Scope newInstance()
    {
        return newInstance( Logger.getAnonymousLogger() );
    }

    static Scope newInstance( Logger logger )
    {
        GlobalScope scope = new AbstractScope.GlobalScope();
        scope.setLogger( logger );
        return scope;
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
     */
    <T> void setVar( String name, T val );

    /**
     * Sets the value in this scope only
     * <p>
     * @param <T>
     * @param name
     * @param val
     */
    <T> void setLocalVar( String name, T val );

    /**
     * begin a new variable scope
     * <p>
     * @return new scope
     */
    Scope begin();

    Logger getLogger();

    void addImport( String type );

    Collection<String> getImports();

    String resolveType( String type );

    String resolveClass( String clazz );

    Scope.GlobalScope getGlobalScope();

    static interface GlobalScope
            extends Scope,
                    Bindings
    {

        void setJob( Job job );

        void setJcl( Jcl jcl );

        void setLogger( Logger logger );
    }
}
