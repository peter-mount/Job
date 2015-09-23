/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang;

import uk.trainwatch.job.*;
import uk.trainwatch.job.lang.JobParser;

/**
 *
 * @author peter
 */
public class JobImpl
{
    private final String id;
    public JobImpl( JobParser.JobContext ctx ) {
        id = ctx.ID().getText();
        ctx.declare();
        ctx.output();
        ctx.block();
    }
}
