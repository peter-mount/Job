/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang;

import uk.trainwatch.job.Scope;

/**
 * A simple block of statements
 * <p>
 * @author peter
 */
public class Block
        implements Statement
{

    private final Statement body[];

    public Block( Statement[] body )
    {
        this.body = body;
    }

    @Override
    public void invokeStatement( Scope scope )
            throws Exception
    {
        try( Scope child = scope.begin() ) {
            for( Statement s: body ) {
                s.invoke( child );
            }
        }
    }

}
