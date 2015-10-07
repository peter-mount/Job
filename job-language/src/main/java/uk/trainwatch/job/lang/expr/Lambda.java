/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.expr;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.lang.Operation;
import uk.trainwatch.job.lang.block.TypeOp;

/**
 *
 * @author peter
 */
public class Lambda
{

    /**
     * Create a lambda function
     *
     * @param parameters
     * @param lambda
     * @return
     */
    public static ExpressionOperation lambda( List<String> parameters, Operation<Object, Object> lambda )
    {
        if( parameters.isEmpty() )
        {
            return ( s, a ) -> lambda.invoke( s, a );
        }
        else
        {
            String params[] = parameters.toArray( new String[parameters.size()] );
            String paramDef = parameters.stream().collect( Collectors.joining( ",", "(", ")" ) );

            return ( s, a ) ->
            {
                if( a.length != params.length )
                {
                    throw new IllegalStateException( "Expected " + params.length + " arguments, got " + a.length + " for " + paramDef );
                }
                try( Scope s1 = s.begin() )
                {
                    for( int i = 0; i < params.length; i++ )
                    {
                        s1.setLocalVar( params[i], a[i] );
                    }
                    return lambda.invoke( s1, a );
                }
            };
        }
    }

    /**
     * Invoke a lambda function stored in a variable
     *
     * @param name
     * @param args
     * @return
     */
    public static ExpressionOperation invoke( String name, ExpressionOperation... args )
    {
        return ( s, a ) ->
        {
            Object obj = Objects.requireNonNull( s.getVar( name ), () -> "No lambda " + name );
            if( obj instanceof ExpressionOperation )
            {
                ExpressionOperation lambda = (ExpressionOperation) obj;
                return lambda.invoke( s, TypeOp.invokeArguments( s, args ) );
            }
            throw new IllegalArgumentException( name + " is not a lambda function" );
        };
    }
}
