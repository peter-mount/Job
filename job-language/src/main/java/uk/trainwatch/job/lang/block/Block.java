/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.block;

import java.util.Collection;
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

        private Throw( Throwable cause )
        {
            super( cause );
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
