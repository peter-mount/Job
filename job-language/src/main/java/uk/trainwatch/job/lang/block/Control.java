/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.block;

import java.util.Objects;
import java.util.stream.Stream;
import uk.trainwatch.job.Scope;
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
        return (s, args) ->
        {
            Objects.requireNonNull( exp, "No if exp" );
            Objects.requireNonNull( trueBlock, "No if trueBlock" );
            if( Logic.isTrue( exp.invoke( s ) ) )
            {
                trueBlock.invokeStatement(s, null );
            }
        };
    }

    public static Statement ifThenElse( ExpressionOperation exp, Statement trueBlock, Statement falseBlock )
    {
        return (s, args) ->
        {
            Objects.requireNonNull( exp, "No if exp" );
            Objects.requireNonNull( trueBlock, "No if trueBlock" );
            Objects.requireNonNull( falseBlock, "No if falseBlock" );
            if( Logic.isTrue( exp.invoke( s ) ) )
            {
                trueBlock.invokeStatement(s, null );
            }
            else
            {
                falseBlock.invokeStatement(s, null );
            }
        };
    }

    public static Statement whileLoop( ExpressionOperation exp, Statement statement )
    {
        return (s, args) ->
        {
            while( Logic.isTrue( exp.invoke( s ) ) )
            {
                statement.invokeStatement(s, null );
            }
        };
    }

    public static Statement doWhile( Statement statement, ExpressionOperation exp )
    {
        return (s, args) ->
        {
            do
            {
                statement.invokeStatement(s, null );
            } while( Logic.isTrue( exp.invoke( s ) ) );
        };
    }

    /**
     * Basic for statement: {@code for( init; expression; update ) statement }
     *
     * @param init
     * @param exp
     * @param update
     * @param statement
     * @return
     */
    public static Statement basicFor( Statement init, ExpressionOperation exp, Statement update, Statement statement )
    {
        return (scope, args) ->
        {
            try( Scope s = scope.begin() )
            {
                init.invokeStatement(s, null );
                while( Logic.isTrue( exp.invoke( s ) ) )
                {
                    statement.invokeStatement(s, null );
                    update.invokeStatement(s, null );
                }
            }
        };
    }

    public static Statement enhancedFor( String name, ExpressionOperation exp, Statement statement )
    {
        return (scope, args) ->
        {
            try( Scope s = scope.begin() )
            {
                Object col = exp.invoke( s );
                if( col instanceof Iterable )
                {
                    for( Object val : (Iterable) col )
                    {
                        s.setVar( name, val );
                        statement.invokeStatement(s, null );
                    }
                }
                else if( col instanceof Stream )
                {
                    ((Stream<Object>) col).forEach(val ->
                    {
                        try
                        {
                            s.setVar( name, val );
                            statement.invokeStatement(s, null );
                        } catch( Exception ex )
                        {
                            throw new RuntimeException( ex );
                        }
                    } );
                }
                // TODO add range operator here
                else
                {
                    throw new UnsupportedOperationException( "Don't know how to iterate " + col );
                }
            } catch( RuntimeException ex )
            {
                Throwable cause = ex.getCause();
                throw cause == null ? ex : (Exception) cause;
            }
        };
    }
}
