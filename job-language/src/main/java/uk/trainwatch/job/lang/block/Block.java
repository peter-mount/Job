/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.block;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import uk.trainwatch.job.AbstractScope;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.expr.ExpressionOperation;

/**
 * A simple block of statements
 * <p>
 * @author peter
 */
public class Block
{

    private static Statement[] toArray( Collection<Statement> statements )
    {
        if( statements == null || statements.isEmpty() ) {
            return new Statement[0];
        }
        return statements.toArray( new Statement[statements.size()] );
    }

    public static Statement declare( Collection<Statement> statements )
    {
        if( statements == null || statements.isEmpty() ) {
            return ( s, a ) -> {
            };
        }

        Statement body[] = toArray( statements );
        return ( scope, args ) -> {
            for( Statement s: body ) {
                s.invoke( scope );
            }
        };

    }

    public static Statement block( Collection<Statement> statements )
    {
        if( statements == null || statements.isEmpty() ) {
            return ( s, a ) -> {
            };
        }

        Statement body[] = toArray( statements );
        return ( scope, args ) -> {
            try( Scope child = scope.begin() ) {
                for( Statement s: body ) {
                    s.invoke( child );
                }
            }
            catch( Throw ex ) {
                Throwable t = ex.getCause();
                if( ex instanceof Exception ) {
                    throw (Exception) ex;
                }
                else {
                    throw new RuntimeException( t );
                }
            }
        };
    }

    public static Statement breakOp()
    {
        return ( s, a ) -> {
            throw new Break();
        };
    }

    public static Statement continueOp()
    {
        return ( s, a ) -> {
            throw new Continue();
        };
    }

    public static Statement throwOp( ExpressionOperation exp )
    {
        return ( s, a ) -> {
            throw new Throw( (Throwable) exp.invoke( s ) );
        };
    }

    public static Statement returnOp()
    {
        return ( s, a ) -> {
            throw new Return( null );
        };
    }

    public static Statement returnOp( ExpressionOperation exp )
    {
        return ( s, a ) -> {
            throw new Return( exp.invoke( s ) );
        };
    }

    private static Statement resolveCatch( Map<String, Statement> catches, Scope s, Throwable t1 )
            throws Exception
    {
        Throwable t = t1 instanceof Block.Throw ? ((Block.Throw) t1).getCause() : t1;

        String cn = t.getClass().getName();

        String type = s.getImports()
                .stream()
                .filter( c -> c.equals( cn ) )
                .map( s::resolveClass )
                .findAny()
                .orElse( null );

        Statement stat = type == null ? null : catches.get( type );

        if( stat != null ) {
            return stat;
        }

        if( t instanceof Exception ) {
            throw (Exception) t;
        }
        else {
            throw new InvocationTargetException( t );
        }
    }

    public static Statement tryOp( Statement resourceBlock, Statement block, Map<String, Statement> catches, Statement finallyBlock )
    {
        Objects.requireNonNull( block, "No method body in try" );

        return resourceBlock == null ? tryOpStandard( block, catches, finallyBlock ) : tryResource( resourceBlock, block, catches, finallyBlock );
    }

    private static Statement tryOpStandard( Statement block, Map<String, Statement> catches, Statement finallyBlock )
    {
        if( catches == null || catches.isEmpty() ) {
            Objects.requireNonNull( finallyBlock, "No finally block in try" );

            return ( s, a ) -> {
                try {
                    block.invoke( s );
                }
                finally {
                    finallyBlock.invoke( s );
                }
            };
        }

        if( finallyBlock == null ) {
            return ( s, a ) -> {
                try {
                    block.invoke( s );
                }
                catch( Throwable t ) {
                    resolveCatch( catches, s, t ).invoke( s );
                }
            };
        }
        else {
            return ( s, a ) -> {
                try {
                    block.invoke( s );
                }
                catch( Throwable t ) {
                    resolveCatch( catches, s, t ).invoke( s );
                }
                finally {
                    finallyBlock.invoke( s );
                }
            };
        }
    }

    private static Statement tryResource( Statement resourceBlock, Statement block, Map<String, Statement> catches, Statement finallyBlock )
    {
        if( finallyBlock == null ) {
            if( catches == null || catches.isEmpty() ) {
                return ( s, a ) -> {
                    try( Scope scope = AbstractScope.resourceScope( s ) ) {
                        resourceBlock.invoke( scope, a );
                        block.invoke( scope );
                    }
                };
            }
            else {
                return ( s, a ) -> {
                    try( Scope scope = AbstractScope.resourceScope( s ) ) {
                        resourceBlock.invoke( scope, a );
                        block.invoke( scope );
                    }
                    catch( Throwable t ) {
                        resolveCatch( catches, s, t ).invoke( s );
                    }
                };
            }
        }

        if( catches == null || catches.isEmpty() ) {
            return ( s, a ) -> {
                try( Scope scope = AbstractScope.resourceScope( s ) ) {
                    resourceBlock.invoke( scope, a );
                    block.invoke( scope );
                }
                finally {
                    finallyBlock.invoke( s );
                }
            };
        }
        else {
            return ( s, a ) -> {
                try( Scope scope = AbstractScope.resourceScope( s ) ) {
                    resourceBlock.invoke( scope, a );
                    block.invoke( scope );
                }
                catch( Throwable t ) {
                    resolveCatch( catches, s, t ).invoke( s );
                }
                finally {
                    finallyBlock.invoke( s );
                }
            };
        }

    }

    public static class ControlException
            extends RuntimeException
    {

        protected ControlException()
        {
        }

        protected ControlException( Throwable cause )
        {
            super( cause );
        }

    };

    public static class Continue
            extends ControlException
    {

        private Continue()
        {
        }

    };

    public static class Break
            extends ControlException
    {

        private Break()
        {
        }

    };

    public static class Throw
            extends ControlException
    {

        public Throw( Throwable cause )
        {
            super( cause );
        }

        public Exception getException()
        {
            Throwable cause = getCause();
            if( cause instanceof Exception ) {
                return (Exception) cause;
            }
            return new InvocationTargetException( cause );
        }
    }

    public static class Return
            extends ControlException
    {

        private final Object value;

        private Return( Object value )
        {
            this.value = value;
        }

        public Object getValue()
        {
            return value;
        }

    }
}
