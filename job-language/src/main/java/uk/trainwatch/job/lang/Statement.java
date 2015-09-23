/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang;

import uk.trainwatch.job.Scope;

/**
 *
 * @author peter
 */
@FunctionalInterface
public interface Statement
        extends Operation<Void>
{

    void invokeStatement( Scope scope )
            throws Exception;

    @Override
    default Void invoke( Scope scope )
            throws Exception
    {
        invokeStatement( scope );
        return null;
    }
}
