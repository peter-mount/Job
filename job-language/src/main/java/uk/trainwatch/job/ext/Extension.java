/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.ext;

import uk.trainwatch.job.lang.Statement;

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
     * Retrieve a statement given it's name
     *
     * @param name Name of statement
     * @return Statement or null
     */
    default Statement getStatement( String name )
    {
        return null;
    }

}
