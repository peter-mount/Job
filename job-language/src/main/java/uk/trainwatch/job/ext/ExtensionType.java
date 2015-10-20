/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.ext;

/**
 *
 * @author peter
 */
public enum ExtensionType
{

    /**
     * Dummy entry for when we need to record an unsupported function
     */
    UNSUPPORTED,
    /**
     * A Statement
     */
    STATEMENT,
    /**
     * A function that returns some value
     */
    FUNCTION;

}
