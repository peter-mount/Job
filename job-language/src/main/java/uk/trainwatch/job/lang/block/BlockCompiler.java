/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.block;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.logging.Level;
import uk.trainwatch.job.lang.AbstractCompiler;
import uk.trainwatch.job.lang.JobParser;
import uk.trainwatch.job.lang.Operation;
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.expr.ExpressionCompiler;

/**
 *
 * @author peter
 */
public class BlockCompiler
        extends AbstractCompiler
{

    private final ExpressionCompiler expressionCompiler = new ExpressionCompiler();

    private final Deque<List<Statement>> stack = new ArrayDeque<>();
    private List<Statement> statements = null;
    private Statement block;
    private boolean appendMode;

    public BlockCompiler setAppendMode( boolean appendMode )
    {
        this.appendMode = appendMode;
        return this;
    }

    public BlockCompiler reset()
    {
        statements = null;
        stack.clear();
        return this;
    }

    public Statement getBlock()
    {
        return block;
    }

    @Override
    public void enterBlock( JobParser.BlockContext ctx )
    {
        final boolean lastAppendMode = appendMode;
        try {
            appendMode = true;

            // Push on the stack
            final boolean push = statements != null;
            if( push ) {
                stack.addLast( statements );
            }

            statements = new ArrayList<>();

            enterRule( ctx.blockStatements() );

            // Empty then do nothing. This is better than an empty block
            // as we'll not even create a sub Scope etc when invoked
            boolean nop = statements.isEmpty();
            if( nop ) {
                block = Operation.nop();
            }
            else if( appendMode ) {
                block = new Block.Normal( statements.toArray( new Statement[statements.size()] ) );
            }
            else {
                block = new Block.Global( statements.toArray( new Statement[statements.size()] ) );
            }

            if( push ) {
                statements = stack.removeLast();

                if( appendMode && !nop ) {
                    statements.add( block );
                }
            }
            else {
                statements = null;
            }
        }
        finally {
            appendMode = lastAppendMode;
        }
    }

//<editor-fold defaultstate="collapsed" desc="General Statement processing">
    @Override
    public void enterBlockStatements( JobParser.BlockStatementsContext ctx )
    {
        enterRule( ctx.blockStatement() );
    }

    @Override
    public void enterBlockStatement( JobParser.BlockStatementContext ctx )
    {
        enterRule( ctx.localVariableDeclarationStatement() );
        enterRule( ctx.statement() );
    }

    @Override
    public void enterLocalVariableDeclarationStatement( JobParser.LocalVariableDeclarationStatementContext ctx )
    {
    }

    @Override
    public void enterStatement( JobParser.StatementContext ctx )
    {
        enterRule( ctx.statementWithoutTrailingSubstatement() );
    }

    @Override
    public void enterStatementWithoutTrailingSubstatement( JobParser.StatementWithoutTrailingSubstatementContext ctx )
    {
        enterRule( ctx.block() );
        enterRule( ctx.expressionStatement() );
    }

    @Override
    public void enterExpressionStatement( JobParser.ExpressionStatementContext ctx )
    {
        enterRule( ctx.statementExpression() );
    }

    @Override
    public void enterStatementExpression( JobParser.StatementExpressionContext ctx )
    {
        enterRule( ctx.logStatement() );
    }
    //</editor-fold>

    @Override
    public void enterLogStatement( JobParser.LogStatementContext ctx )
    {
        Level level;
        String l = ctx.getChild( 0 ).getText();
        switch( l ) {
            case "log":
                level = Level.INFO;
                break;

            case "debug":
                level = Level.FINE;
                break;

            case "warn":
                level = Level.WARNING;
                break;

            case "severe":
                level = Level.SEVERE;
                break;

            default:
                throw new IllegalArgumentException( "Unsupported Log level " + l );
        }

        enterRule( ctx.stringExpression(), expressionCompiler.reset() );

        Operation<Object> op = expressionCompiler.getExpression();
        statements.add( new Log( level, op ) );
    }

}
