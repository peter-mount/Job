/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.block;

import java.io.Serializable;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.lang.Statement;

/**
 * A simple block of statements
 * <p>
 * @author peter
 */
abstract class Block
        implements Statement,
                   Serializable
{

    private static final long serialVersionUID = 1L;

    protected final Statement body[];

    public Block( Statement[] body )
    {
        this.body = body;
    }

    static class Global
            extends Block
    {

        private static final long serialVersionUID = 1L;

        public Global( Statement[] body )
        {
            super( body );
        }

        @Override
        public void invokeStatement( Scope scope )
                throws Exception
        {
            for( Statement s: body ) {
                s.invoke( scope );
            }
        }

    }

    static class Normal
            extends Block
    {

        private static final long serialVersionUID = 1L;

        public Normal( Statement[] body )
        {
            super( body );
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
}
