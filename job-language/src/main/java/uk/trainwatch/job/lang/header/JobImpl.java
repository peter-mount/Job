/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.header;

import java.io.Serializable;
import java.util.logging.Level;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.lang.Statement;

/**
 *
 * @author peter
 */
class JobImpl
        implements Job,
                   Serializable
{

    private static final long serialVersionUID = 1L;
    private final String id;
    private final String runAs;
    private final Statement declare;
    private final Statement output;
    private final Statement block;

    public JobImpl( String id, String runAs, Statement declare, Statement output, Statement block )
    {
        this.id = id;
        this.runAs = runAs;
        this.declare = declare;
        this.output = output;
        this.block = block;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getRunAs()
    {
        return runAs;
    }

    @Override
    public void invokeStatement( Scope scope )
            throws Exception
    {
        scope.getLogger().log( Level.FINE, () -> "Starting " + id );
        try {
            if( declare != null ) {
                declare.invokeStatement( scope );
            }

            if( output != null ) {
                output.invokeStatement( scope );
            }

            block.invokeStatement( scope );
        }
        finally {
            scope.getLogger().log( Level.FINE, () -> "Completed " + id );
        }
    }

}
