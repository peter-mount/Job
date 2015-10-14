/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.shell;

import java.util.Objects;

/**
 *
 * @author peter
 */
public interface Container
        extends AutoCloseable
{

    void open();

    @Override
    default void close()
            throws Exception
    {
    }

    static final Container NOP = () -> {
    };

    default Container andThen( Container c )
    {
        return andThen( this, c );
    }

    static Container andThen( Container a, Container b )
    {
        if( a == null ) {
            return Objects.requireNonNull( b );
        }

        if( b == null ) {
            return Objects.requireNonNull( a );
        }

        Objects.requireNonNull( a );
        Objects.requireNonNull( b );
        
        return new Container()
        {

            @Override
            public void open()
            {
                a.open();
                b.open();
            }

            @Override
            public void close()
                    throws Exception
            {
                try {
                    b.close();
                }
                finally {
                    a.close();
                }
            }
        };
    }
}
