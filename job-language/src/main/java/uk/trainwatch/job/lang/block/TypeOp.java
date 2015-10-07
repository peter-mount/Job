/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.block;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.ext.ExtensionManager;
import uk.trainwatch.job.lang.expr.ExpressionOperation;
import uk.trainwatch.job.lang.expr.Lambda;

/**
 *
 * @author peter
 */
public class TypeOp
{

    public static ExpressionOperation[] toArray( Collection<ExpressionOperation> col )
    {
        if( col == null || col.isEmpty() ) {
            return new ExpressionOperation[0];
        }
        return col.toArray( new ExpressionOperation[col.size()] );
    }

    public static Object[] invokeArguments( Scope s, ExpressionOperation... exp )
            throws Exception
    {
        if( exp == null ) {
            return new Object[0];
        }

        Object args[] = new Object[exp.length];
        for( int i = 0; i < exp.length; i++ ) {
            args[i] = exp[i].invoke( s );
        }
        return args;
    }

    public static ExpressionOperation construct( String type, ExpressionOperation... exp )
    {
        return ( s, a ) -> {
            try {
                String realType = s.resolveType( type );
                Class clazz = Class.forName( realType );

                Object args[] = invokeArguments( s, exp );

                return MethodHandles.lookup()
                        .findConstructor( clazz, MethodType.methodType( void.class ) )
                        .invokeWithArguments( args );
            }
            catch( Exception ex ) {
                throw ex;
            }
            catch( Throwable ex ) {
                throw new InvocationTargetException( ex );
            }
        };
    }

    @SuppressWarnings("ThrowableInstanceNotThrown")
    public static ExpressionOperation invoke( ExpressionOperation srcExp, String methodName, ExpressionOperation... argExp )
    {
        return ( s, a ) -> {
            try {
                Object obj = Objects.requireNonNull( srcExp.invoke( s ), "Cannot dereference null" );

                Class clazz = obj.getClass();

                Object args[] = invokeArguments( s, argExp );

                // Not ideal but appears to work, locates the first method with same name and number of arguments
                MethodHandle h = MethodHandles.publicLookup().unreflect(
                        Stream.of( clazz.getMethods() ).
                        filter( m -> methodName.equals( m.getName() ) ).
                        filter( m -> m.getParameterCount() == args.length ).
                        findAny().orElseThrow( () -> new NoSuchMethodException( methodName + " in " + clazz ) )
                )
                        .bindTo( obj );

                // See if we need to translate any arguments, specifically lambdas
                MethodType t = h.type();
                if( t.parameterCount() > 0 ) {
                    if( t.parameterCount() != args.length ) {
                        throw new IndexOutOfBoundsException( "Got " + args.length + " args but expecting " + t.parameterCount() + " parameters" );
                    }
                    Class<?> params[] = t.parameterArray();
                    for( int p = 0; p < params.length; p++ ) {
                        if( args[p] instanceof Lambda ) {
                            args[p] = Lambda.translate( (Lambda) args[p], params[p] );
                        }
                    }
                }

                return h.invokeWithArguments( args );
            }
            catch( Exception ex ) {
                throw ex;
            }
            catch( Throwable ex ) {
                throw new InvocationTargetException( ex );
            }
        };
    }

    public static ExpressionOperation invokeExtension( String methodName, ExpressionOperation... argExp )
    {
        return ( s, a ) -> Objects.requireNonNull( ExtensionManager.INSTANCE.getStatement( methodName ), "Cannot locate " + methodName )
                .invoke( s, invokeArguments( s, argExp ) );
    }
}
