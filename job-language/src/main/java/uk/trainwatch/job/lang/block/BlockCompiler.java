/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.block;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.antlr.v4.runtime.ParserRuleContext;
import uk.trainwatch.job.lang.AbstractCompiler;
import uk.trainwatch.job.lang.JobParser;
import uk.trainwatch.job.lang.Operation;
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.expr.Assignment;
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

    // The current block's statements
    private List<Statement> statements = null;
    // The last block visited by {@link #enterBlock(uk.trainwatch.job.lang.JobParser.BlockContext) }
    private Statement block;
    private String name;
    
    //<editor-fold defaultstate="collapsed" desc="Blocks">
    
    /**
     * The last block visited by {@link #enterBlock(uk.trainwatch.job.lang.JobParser.BlockContext) }
     *
     * @return
     */
    public Statement getBlock()
    {
        return block;
    }
    
    public BlockScope begin()
    {
        return begin( true );
    }
    
    public BlockScope begin( boolean declare )
    {
        BlockScope scope = new BlockScope( declare );
        return scope;
    }
    
    public Statement getBlock( ParserRuleContext ctx )
    {
        return getBlock( ctx, false );
    }
    
    public Statement getBlock( ParserRuleContext ctx, boolean declare )
    {
        try( BlockScope scope = begin( declare ) )
        {
            if( ctx instanceof JobParser.BlockContext )
            {
                enterRule( ((JobParser.BlockContext) ctx).blockStatements() );
            }
            else
            {
                enterRule( ctx );
            }
            return scope.getStatement();
        }
    }
    
    /**
     * A parsing scope
     */
    public class BlockScope
    implements AutoCloseable
    {
        
        private final List<Statement> oldStatements;
        private final boolean declare;
        
        private BlockScope( boolean declare )
        {
            this.declare = declare;
            oldStatements = statements;
            statements = new ArrayList<>();
        }
        
        public boolean isDeclare()
        {
            return declare;
        }
        
        @Override
        public void close()
        {
            statements = oldStatements;
        }
        
        public Statement getStatement()
        {
            // Empty then do nothing. This is better than an empty block
            // as we'll not even create a sub Scope etc when invoked
            boolean nop = statements.isEmpty();
            if( nop )
            {
                block = Operation.nop();
            }
            else if( declare )
            {
                block = Block.declare( statements );
            }
            else
            {
                block = Block.block( statements );
            }
            return block;
        }
    }
    
    @Override
    public void enterBlock( JobParser.BlockContext ctx )
    {
        try( BlockScope st = new BlockScope( false ) )
        {
            enterRule( ctx.blockStatements() );
            block = st.getStatement();
        }
        statements.add( block );
    }
    //</editor-fold>

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
        enterRule( ctx.ifThenStatement() );
        enterRule( ctx.ifThenElseStatement() );
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
        // Although assignment is a form of statement it's handled entirely by ExpressionCompiler
        enterRule( ctx.assignment(), expressionCompiler.reset() );

        enterRule( ctx.logStatement() );
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Control Statements">
    @Override
    public void enterIfThenStatement( JobParser.IfThenStatementContext ctx )
    {
        enterRule( ctx.expression(), expressionCompiler.reset() );
        ExpressionOperation exp = expressionCompiler.getExpression();

        Statement trueBlock = getBlock( ctx.statement(), true );

        statements.add( Control.ifThen( exp, trueBlock ) );
    }

    @Override
    public void enterIfThenElseStatement( JobParser.IfThenElseStatementContext ctx )
    {
        enterRule( ctx.expression(), expressionCompiler.reset() );
        ExpressionOperation exp = expressionCompiler.getExpression();

        Statement trueBlock = getBlock( ctx.statement( 0 ) );
        Statement falseBlock = getBlock( ctx.statement( 1 ) );

        statements.add( Control.ifThenElse( exp, trueBlock, falseBlock ) );
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Local Variable declaration">
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
        String varName = name;
        
        enterRule( ctx.variableInitializer() );
        ExpressionOperation expr = expressionCompiler.getExpression();
        statements.add( s -> Assignment.setVariable( varName, expr ).invoke( s ) );
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
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Global declaration section">
    @Override
    public void enterDeclare( JobParser.DeclareContext ctx )
    {
        try( BlockCompiler.BlockScope st = new BlockCompiler.BlockScope( true ) )
        {
            enterRule( ctx.declareStatements() );
            block = st.getStatement();
        }
    }
    
    @Override
    public void enterDeclareStatements( JobParser.DeclareStatementsContext ctx )
    {
        enterRule( ctx.declareStatement() );
    }
    
    @Override
    public void enterDeclareStatement( JobParser.DeclareStatementContext ctx )
    {
        enterRule( ctx.localVariableDeclarationStatement() );
    }
//</editor-fold>
    
    @Override
    public void enterLogStatement( JobParser.LogStatementContext ctx )
    {
        Level level;
        String l = ctx.getChild( 0 ).getText();
        switch( l )
        {
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
