/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.block;

import java.util.Objects;
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.expr.ExpressionOperation;
import uk.trainwatch.job.lang.expr.Logic;

/**
 *
 * @author peter
 */
public class Control
{

    public static Statement ifThen( ExpressionOperation exp, Statement trueBlock )
    {
        return s ->
        {
            Objects.requireNonNull( exp, "No if exp");
            Objects.requireNonNull( trueBlock, "No if trueBlock");
            if( Logic.isTrue( exp.invoke( s ) ) )
            {
                trueBlock.invokeStatement( s );
            }
        };
    }

    public static Statement ifThenElse( ExpressionOperation exp, Statement trueBlock, Statement falseBlock )
    {
        return s ->
        {
            Objects.requireNonNull( exp, "No if exp");
            Objects.requireNonNull( trueBlock, "No if trueBlock");
            Objects.requireNonNull( falseBlock, "No if falseBlock");
            if( Logic.isTrue( exp.invoke( s ) ) )
            {
                trueBlock.invokeStatement( s );
            }
            else
            {
                falseBlock.invokeStatement( s );
            }
        };
    }
}
