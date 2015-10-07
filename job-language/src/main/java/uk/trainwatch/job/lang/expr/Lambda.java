/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.expr;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import uk.trainwatch.job.AbstractScope;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.lang.Operation;
import uk.trainwatch.job.lang.block.TypeOp;

/**
 *
 * @author peter
 */
public class Lambda
        implements ExpressionOperation
{

    private final Operation<Object, Object> lambda;
    private final String params[];
    private final String paramDef;

    public Lambda( Operation<Object, Object> lambda, String paramDef, String... params )
    {
        this.lambda = lambda;
        this.params = params;
        this.paramDef = paramDef;
    }

    @Override
    public Object invoke( Scope scope, Object... args )
            throws Exception
    {
        if( args.length != params.length ) {
            throw new IllegalStateException( "Expected " + params.length + " arguments, got " + args.length + " for " + paramDef );
        }
        try( Scope s1 = scope.begin() ) {
            for( int i = 0; i < params.length; i++ ) {
                s1.setLocalVar( params[i], args[i] );
            }
            return lambda.invoke( s1, args );
        }
    }

    public static Object translate( Lambda l, Class<?> c )
    {
        if( Consumer.class.isAssignableFrom( c ) ) {
            return l.asConsumer();
        }

        if( Function.class.isAssignableFrom( c ) ) {
            return l.asFunction();
        }

        if( Supplier.class.isAssignableFrom( c ) ) {
            return l.asSupplier();
        }

        if( BiConsumer.class.isAssignableFrom( c ) ) {
            return l.asBiConsumer();
        }

        if( BiFunction.class.isAssignableFrom( c ) ) {
            return l.asBiFunction();
        }

        // Default invoke it now
        try {
            return l.invoke( AbstractScope.getCurrentScope() );
        }
        catch( Exception ex ) {
            throw new RuntimeException( ex );
        }
    }

    public BiConsumer asBiConsumer()
    {
        return ( a, b ) -> {
            try {
                invoke( AbstractScope.getCurrentScope(), a, b );
            }
            catch( Exception ex ) {
                throw new RuntimeException( ex );
            }
        };
    }

    public BiFunction asBiFunction()
    {
        return ( a, b ) -> {
            try {
                return invoke( AbstractScope.getCurrentScope(), a, b );
            }
            catch( Exception ex ) {
                throw new RuntimeException( ex );
            }
        };
    }

    public Consumer asConsumer()
    {
        return v -> {
            try {
                invoke( AbstractScope.getCurrentScope(), v );
            }
            catch( Exception ex ) {
                throw new RuntimeException( ex );
            }
        };
    }

    public Function asFunction()
    {
        return v -> {
            try {
                return invoke( AbstractScope.getCurrentScope(), v );
            }
            catch( Exception ex ) {
                throw new RuntimeException( ex );
            }
        };
    }

    public Supplier asSupplier()
    {
        return () -> {
            try {
                return invoke( AbstractScope.getCurrentScope() );
            }
            catch( Exception ex ) {
                throw new RuntimeException( ex );
            }
        };
    }

    /**
     * Create a lambda function
     *
     * @param parameters
     * @param lambda
     *                   <p>
     * @return
     */
    public static ExpressionOperation lambda( List<String> parameters, Operation<Object, Object> lambda )
    {
        if( parameters.isEmpty() ) {
            return new Lambda( lambda, "()" );
        }
        else {
            return new Lambda( lambda,
                               parameters.stream().collect( Collectors.joining( ",", "(", ")" ) ),
                               parameters.toArray( new String[parameters.size()] )
            );
        }
    }

    /**
     * Invoke a lambda function stored in a variable
     *
     * @param name
     * @param args
     *             <p>
     * @return
     */
    public static ExpressionOperation invoke( String name, ExpressionOperation... args )
    {
        return ( s, a ) -> {
            Object obj = Objects.requireNonNull( s.getVar( name ), () -> "No lambda " + name );
            if( obj instanceof ExpressionOperation ) {
                ExpressionOperation lambda = (ExpressionOperation) obj;
                return lambda.invoke( s, TypeOp.invokeArguments( s, args ) );
            }
            throw new IllegalArgumentException( name + " is not a lambda function" );
        };
    }
}
