/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job;

/**
 * A listener to allow the monitoring of a job
 * <p>
 * @author peter
 */
public interface JobListener
{

    default void jobStarted( Job job, Scope scope )
    {
    }

    default void jobCompleted( Job job, Scope scope )
    {
    }

    default void jobException( Job job, Scope scope, Exception ex )
    {
    }
}
