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
        extends Operation<Object,Object>
{

    void invokeStatement( Scope scope, Object... args)
            throws Exception;

    @Override
    default Object invoke( Scope scope, Object... args )
            throws Exception
    {
        invokeStatement(scope, args );
        return null;
    }
}
