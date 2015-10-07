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
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.expr.Assignment;
import uk.trainwatch.job.lang.expr.ExpressionCompiler;
import uk.trainwatch.job.lang.expr.ExpressionOperation;
import uk.trainwatch.job.util.NestedMap;
import uk.trainwatch.job.util.NestedString;

/**
 *
 * @author peter
 */
public class BlockCompiler
        extends AbstractCompiler
{

    private final ExpressionCompiler expressionCompiler = new ExpressionCompiler( this );

    // The current block's statements
    private List<Statement> statements = null;
    // The last block visited by {@link #enterBlock(uk.trainwatch.job.lang.JobParser.BlockContext) }
    private Statement block;
    private final NestedString name = new NestedString();
    private final NestedMap<String, Statement> catches = new NestedMap.Linked<>();

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
        return new BlockScope();
    }

    public BlockScope begin( boolean declare )
    {
        return new BlockScope( declare );
    }

    public Statement getBlock( ParserRuleContext ctx )
    {
        return getBlock( ctx, false );
    }

    public Statement getBlock( ParserRuleContext ctx, boolean declare )
    {
        if( ctx == null ) {
            return null;
        }

        try( BlockScope scope = begin( declare ) ) {
            if( ctx instanceof JobParser.BlockContext ) {
                enterRule( ((JobParser.BlockContext) ctx).blockStatements() );
            }
            else {
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

        public BlockScope()
        {
            this( false );
        }

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
            if( nop ) {
                block = ( s, a ) -> {
                };
            }
            else if( declare ) {
                block = Block.declare( statements );
            }
            else {
                block = Block.block( statements );
            }
            return block;
        }
    }

    @Override
    public void enterBlock( JobParser.BlockContext ctx )
    {
        try( BlockScope st = new BlockScope() ) {
            enterRule( ctx.blockStatements() );
            block = st.getStatement();
        }
        statements.add( block );
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="General Statement processing">
    @Override
    public void enterAssignment( JobParser.AssignmentContext ctx )
    {
        // Assignment is actually handled by expressionCompiler
        // so create an Expression & wrap it into a Statement
        ExpressionOperation expr = expressionCompiler.apply( () -> expressionCompiler.enterAssignment( ctx ) );
        statements.add( ( s, a ) -> expr.invoke( s, a ) );
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Control Statements">
    @Override
    public void enterIfThenStatement( JobParser.IfThenStatementContext ctx )
    {
        ExpressionOperation exp = expressionCompiler.apply( () -> expressionCompiler.enterRule( ctx.expression() ) );

        Statement trueBlock = getBlock( ctx.statement(), true );

        statements.add( Control.ifThen( exp, trueBlock ) );
    }

    @Override
    public void enterIfThenElseStatement( JobParser.IfThenElseStatementContext ctx )
    {
        ExpressionOperation exp = expressionCompiler.apply( () -> expressionCompiler.enterRule( ctx.expression() ) );

        Statement trueBlock = getBlock( ctx.statement( 0 ) );
        Statement falseBlock = getBlock( ctx.statement( 1 ) );

        statements.add( Control.ifThenElse( exp, trueBlock, falseBlock ) );
    }

    @Override
    public void enterForStatement( JobParser.ForStatementContext ctx )
    {
        enterRule( ctx.basicForStatement() );
        enterRule( ctx.enhancedForStatement() );
    }

    @Override
    public void enterBasicForStatement( JobParser.BasicForStatementContext ctx )
    {
        // true here as we want these blocks to access the same scope & not run their own ones.
        // This scope will then be bound to the entire for() expression.
        Statement init = getBlock( ctx.forInit(), true );

        ExpressionOperation exp = expressionCompiler.apply( () -> expressionCompiler.enterRule( ctx.expression() ) );

        Statement update = getBlock( ctx.forUpdate(), true );

        Statement statement = getBlock( ctx.statement(), true );

        statements.add( Control.basicFor( init, exp, update, statement ) );
    }

    @Override
    public void enterForInit( JobParser.ForInitContext ctx )
    {
        if( ctx.localVariableDeclaration() == null ) {
            enterRule( ctx.statementExpressionList() );
        }
        else {
            enterRule( ctx.localVariableDeclaration() );
        }
    }

    @Override
    public void enterForUpdate( JobParser.ForUpdateContext ctx )
    {
        enterRule( ctx.statementExpressionList() );
    }

    @Override
    public void enterEnhancedForStatement( JobParser.EnhancedForStatementContext ctx )
    {
        String varName = name.apply( () -> enterRule( ctx.variableDeclaratorId() ) );

        ExpressionOperation expr = expressionCompiler.apply( () -> expressionCompiler.enterRule( ctx.expression() ) );

        Statement statement = getBlock( ctx.statement(), true );
        statements.add( Control.enhancedFor( varName, expr, statement ) );
    }

    @Override
    public void enterWhileStatement( JobParser.WhileStatementContext ctx )
    {
        ExpressionOperation expr = expressionCompiler.apply( () -> expressionCompiler.enterRule( ctx.expression() ) );

        Statement statement = getBlock( ctx.statement() );

        statements.add( Control.whileLoop( expr, statement ) );
    }

    @Override
    public void enterDoStatement( JobParser.DoStatementContext ctx )
    {
        Statement statement = getBlock( ctx.statement() );

        ExpressionOperation expr = expressionCompiler.apply( () -> expressionCompiler.enterRule( ctx.expression() ) );

        statements.add( Control.doWhile( statement, expr ) );
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Local Variable declaration">
    @Override
    public void enterVariableDeclarator( JobParser.VariableDeclaratorContext ctx )
    {
        String varName = name.apply( () -> enterRule( ctx.variableDeclaratorId() ) );

        ExpressionOperation expr = expressionCompiler.apply( () -> expressionCompiler.enterRule( ctx.variableInitializer() ) );
        statements.add( ( s, a ) -> Assignment.setVariable( varName, expr ).invoke( s, a ) );
    }

    @Override
    public void enterVariableDeclaratorId( JobParser.VariableDeclaratorIdContext ctx )
    {
        name.set( ctx.Identifier() );
    }
//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Break/throw">

    @Override
    public void enterThrowStatement( JobParser.ThrowStatementContext ctx )
    {
        statements.add( Block.throwOp( expressionCompiler.apply( () -> expressionCompiler.enterRule( ctx.expression() ) ) ) );
    }

    @Override
    public void enterBreakStatement( JobParser.BreakStatementContext ctx )
    {
        statements.add( Block.breakOp() );
    }

    @Override
    public void enterContinueStatement( JobParser.ContinueStatementContext ctx )
    {
        statements.add( Block.continueOp() );
    }

    @Override
    public void enterReturnStatement( JobParser.ReturnStatementContext ctx )
    {
        if( ctx.expression() == null ) {
            statements.add( Block.returnOp() );
        }
        else {
            statements.add( Block.returnOp(
                    expressionCompiler.apply( () -> expressionCompiler.enterRule( ctx.expression() ) )
            ) );
        }
    }
//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Try">

    @Override
    public void enterTryStatement( JobParser.TryStatementContext ctx )
    {
        statements.add( Block.tryOp(
                getBlock( ctx.resourceSpecification(), true ),
                getBlock( ctx.block(), false ),
                catches.apply( () -> enterRule( ctx.catches() ) ),
                getBlock( ctx.finally_(), false )
        ) );
    }

    @Override
    public void enterResourceSpecification( JobParser.ResourceSpecificationContext ctx )
    {
        enterRule( ctx.resourceList() );
    }

    @Override
    public void enterResourceList( JobParser.ResourceListContext ctx )
    {
        enterRule( ctx.resource() );
    }

    @Override
    public void enterResource( JobParser.ResourceContext ctx )
    {
        statements.add( ( s, a ) -> Assignment.setVariable(
                name.apply( () -> enterRule( ctx.variableDeclaratorId() ) ),
                expressionCompiler.apply( () -> expressionCompiler.enterRule( ctx.expression() ) )
        ).invoke( s ) );
    }

    @Override
    public void enterCatches( JobParser.CatchesContext ctx )
    {
        enterRule( ctx.catchClause() );
    }

    @Override
    public void enterCatchClause( JobParser.CatchClauseContext ctx )
    {
        Statement block = getBlock( ctx.block(), true );

        ctx.catchFormalParameter()
                .catchType()
                .Identifier()
                .forEach( id -> catches.put( id.getText(), block ) );
    }

    @Override
    public void enterFinally_( JobParser.Finally_Context ctx )
    {
        enterRule( ctx.block() );
    }
//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Global declaration section">

    @Override
    public void enterDeclare( JobParser.DeclareContext ctx )
    {
        try( BlockCompiler.BlockScope st = new BlockCompiler.BlockScope( true ) ) {
            enterRule( ctx.declareStatements() );
            block = st.getStatement();
        }
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

        ExpressionOperation expr = expressionCompiler.apply( () -> expressionCompiler.enterRule( ctx.stringExpression() ) );

        statements.add( Log.log( level, expr ) );
    }

    @Override
    public void enterExtensionStatement( JobParser.ExtensionStatementContext ctx )
    {
        expressionCompiler.apply( () -> expressionCompiler.enterRule( ctx ) );
    }

}
