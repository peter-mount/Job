/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.header;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.JobListener;
import uk.trainwatch.job.JobOutput;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.block.Block;

/**
 *
 * @author peter
 */
class JobImpl
        implements Job
{

    private static final long serialVersionUID = 1L;
    private final String id;
    private final String cluster;
    private final Statement declare;
    private final Statement output;
    private final Statement block;
    private JobOutputImpl jobOutput;
    private Set<JobListener> listeners;

    public JobImpl( String id, String cluster, Statement declare, Statement output, Statement block )
            throws IOException
    {
        this.id = id;
        this.cluster = cluster;
        this.declare = declare;
        this.output = output;
        this.block = block;
        jobOutput = new JobOutputImpl( this );
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getCluster()
    {
        return cluster;
    }

    @Override
    public JobOutput getJobOutput()
    {
        return jobOutput;
    }

    @Override
    public void invokeStatement( Scope scope, Object... args )
            throws Exception
    {
        if( scope instanceof Scope.GlobalScope ) {
            ((Scope.GlobalScope) scope).setJob( this );
        }

        try( JobOutputImpl outputImpl = jobOutput ) {
            jobOutput = outputImpl;

            scope.getLogger().log( Level.FINE, () -> "Starting " + id );
            fire( l -> l.jobStarted( this, scope ) );
            try {
                if( declare != null ) {
                    declare.invokeStatement( scope.getGlobalScope() );
                }

                if( output != null ) {
                    output.invokeStatement( scope );
                }

                block.invokeStatement( scope );
            }
            catch( Block.Throw ex ) {
                Exception ex1 = ex.getException();
                fire( l -> l.jobException( this, scope, ex1 ) );
                throw ex1;
            }
            finally {
                scope.getLogger().log( Level.FINE, () -> "Completed " + id );
                fire( l -> l.jobCompleted( this, scope ) );
            }
        }
    }

    @Override
    public void addListener( JobListener l )
    {
        if( listeners == null ) {
            listeners = new HashSet<>();
        }
        listeners.add( l );
    }

    @Override
    public void removeListener( JobListener l )
    {
        if( listeners != null && listeners.remove( l ) ) {
            if( listeners.isEmpty() ) {
                listeners = null;
            }
        }
    }

    @Override
    public void fire( Consumer<JobListener> c )
    {
        if( listeners != null ) {
            listeners.forEach( c );
        }
    }

    public void log( Level l, String s, Exception e )
    {
        jobOutput.log( l, s, e );
    }

    @Override
    public boolean isLoggable( Level level )
    {
        return jobOutput.isLoggable( level );
    }

}
