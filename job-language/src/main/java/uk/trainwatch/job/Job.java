/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job;

import uk.trainwatch.job.lang.Operation;

/**
 *
 * @author peter
 */
public interface Job
        extends Operation<Void>
{

    String getId();

}
