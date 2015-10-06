/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.block;

import java.util.Collection;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.lang.Operation;
import uk.trainwatch.job.lang.Statement;

/**
 * A simple block of statements
 * <p>
 * @author peter
 */
class Block
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
            return Operation.nop();
        }

        Statement body[] = toArray( statements );
        return (scope, args) -> {
            for( Statement s: body ) {
                s.invoke( scope );
            }
        };

    }

    public static Statement block( Collection<Statement> statements )
    {
        if( statements == null || statements.isEmpty() ) {
            return Operation.nop();
        }

        Statement body[] = toArray( statements );
        return (scope, args) -> {
            try( Scope child = scope.begin() ) {
                for( Statement s: body ) {
                    s.invoke( child );
                }
            }
        };
    }
    
}
