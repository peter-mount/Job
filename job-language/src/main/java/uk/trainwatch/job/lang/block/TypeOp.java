/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.block;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.ext.ExtensionManager;
import uk.trainwatch.job.lang.Statement;
import static uk.trainwatch.job.lang.expr.Arithmetic.decode;
import uk.trainwatch.job.lang.expr.ExpressionOperation;
import uk.trainwatch.job.lang.expr.Lambda;

/**
 *
 * @author peter
 */
public class TypeOp
{

    /**
     * Temp fix for https://github.com/peter-mount/Job/issues/1
     */
    private static final Lookup TRUSTED_LOOKUP;
    static {
        Lookup myLookup = MethodHandles.lookup(); // the Lookup which should be trusted

        try {
            Field impl_lookup = Lookup.class.getDeclaredField( "IMPL_LOOKUP" );
            impl_lookup.setAccessible( true );
            TRUSTED_LOOKUP = (Lookup) impl_lookup.get( myLookup );
        } catch( Exception e ) {
            e.printStackTrace();
            throw new InstantiationError( e.getMessage() );
        }
    }

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
            args[i] = decode( exp[i].invoke( s ) );
        }
        return args;
    }

    public static ExpressionOperation construct( String type, ExpressionOperation... expArgs )
    {
        ExpressionOperation exp = ExtensionManager.INSTANCE.construct( type, expArgs );
        if( exp != null ) {
            return exp;
        }

        return ( s, a ) -> {
            try {
                String realType = s.resolveType( type );
                if( realType == null ) {
                    throw new ClassNotFoundException( "Type " + type + " has not been defined" );
                }

                Class clazz = Class.forName( realType );

                Object args[] = invokeArguments( s, expArgs );

                // See top of class for why
                //return MethodHandles.lookup()
                return TRUSTED_LOOKUP
                        .findConstructor( clazz, MethodType.methodType( void.class ) )
                        .invokeWithArguments( args );
            } catch( Exception ex ) {
                throw ex;
            } catch( Throwable ex ) {
                throw new InvocationTargetException( ex );
            }
        };
    }

    @SuppressWarnings( "ThrowableInstanceNotThrown" )
    public static ExpressionOperation invoke( ExpressionOperation srcExp, String methodName, ExpressionOperation... argExp )
    {
        Objects.requireNonNull( methodName, "methodName is null" );
        return ( s, a ) -> {
            try {
                Object obj = Objects.requireNonNull( srcExp.invoke( s ), "Cannot dereference null" );

                Class clazz = obj.getClass();

                Object args[] = invokeArguments( s, argExp );

                // Not ideal but appears to work, locates the first method with same name and number of arguments
                //MethodHandle h = MethodHandles.publicLookup().unreflect(
                MethodHandle h = TRUSTED_LOOKUP.unreflect(
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
            } catch( Exception ex ) {
                throw ex;
            } catch( Throwable ex ) {
                throw new InvocationTargetException( ex );
            }
        };
    }

    public static Statement invokeExtensionStatement( String methodName, ExpressionOperation... argExp )
    {
        return Objects.requireNonNull( ExtensionManager.INSTANCE.getStatement( methodName, argExp ), "Cannot locate " + methodName );
    }

    public static Statement invokeOutputStatement( String methodName, ExpressionOperation... argExp )
    {
        return Objects.requireNonNull( ExtensionManager.INSTANCE.getOutputStatement( methodName, argExp ), "Cannot locate output " + methodName );
    }

    public static ExpressionOperation invokeExtension( String methodName, ExpressionOperation... argExp )
    {
        Statement extension = invokeExtensionStatement( methodName, argExp );

        return ( s, a ) -> extension.invoke( s );
    }
}
