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
import uk.trainwatch.job.lang.expr.ExpressionOperation;

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
    private String name;

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
                block = Block.block( statements );
            }
            else {
                block = Block.declare( statements );
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
    public void enterLocalVariableDeclarationStatement( JobParser.LocalVariableDeclarationStatementContext ctx )
    {
        enterRule( ctx.localVariableDeclaration() );
    }

    @Override
    public void enterLocalVariableDeclaration( JobParser.LocalVariableDeclarationContext ctx )
    {
        enterRule( ctx.variableDeclaratorList() );
    }

    @Override
    public void enterVariableDeclaratorList( JobParser.VariableDeclaratorListContext ctx )
    {
        enterRule( ctx.variableDeclarator() );
    }

    @Override
    public void enterVariableDeclarator( JobParser.VariableDeclaratorContext ctx )
    {
        enterRule( ctx.variableDeclaratorId() );
        enterRule( ctx.variableInitializer() );
        ExpressionOperation expr = expressionCompiler.getExpression();
        statements.add( scope -> scope.setVar( name, expr.invoke( scope ) ) );
    }

    @Override
    public void enterVariableDeclaratorId( JobParser.VariableDeclaratorIdContext ctx )
    {
        name = ctx.Identifier().getText();
    }

    @Override
    public void enterVariableInitializer( JobParser.VariableInitializerContext ctx )
    {
        enterRule( ctx.expression(), expressionCompiler.reset() );
    }

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

        ExpressionOperation expr = expressionCompiler.getExpression();

        statements.add( Log.log( level, expr ) );
    }

}
