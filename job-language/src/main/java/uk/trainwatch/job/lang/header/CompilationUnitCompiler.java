/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.header;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.tree.TerminalNode;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.lang.AbstractCompiler;
import uk.trainwatch.job.lang.block.BlockCompiler;
import uk.trainwatch.job.lang.JobParser;
import uk.trainwatch.job.lang.JobParser.*;
import uk.trainwatch.job.lang.Operation;
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.block.Block;
import uk.trainwatch.job.lang.block.TypeOp;
import uk.trainwatch.job.lang.expr.ExpressionOperation;

/**
 *
 * @author peter
 */
public class CompilationUnitCompiler
        extends AbstractCompiler
{

    private final BlockCompiler blockCompiler = new BlockCompiler();
    private Job job;
    private Statement block;
    private Statement declareBlock;
    private Statement outputBlock;
    private JobDefinitionContext jobDefinitionContext;
    private String name;

    public Job compile( JobParser parser )
    {
        job = null;
        enterRule( parser.compilationUnit() );
        return job;
    }

    @Override
    public void enterCompilationUnit( CompilationUnitContext ctx )
    {
        enterRule( ctx.jobDefinition() );

        // Optional declare { }
        blockCompiler.enterRule( ctx.declare() );
        declareBlock = blockCompiler.getBlock();

        // Optional output { }
        if( ctx.output() == null ) {
            outputBlock = ( s, a ) -> {
            };
        }
        else {
            outputStatements.clear();
            enterOutput( ctx.output() );
            outputBlock = Block.declare( outputStatements );
        }

        // The main body
        block = blockCompiler.getBlock( ctx.block(), false );

        List<TerminalNode> strings = jobDefinitionContext.StringLiteral();
        final String id = getString( strings.get( 0 ) );
        final String runAs = strings.size() > 1 ? getString( strings.get( 1 ) ) : "Local";

        try {
            job = new JobImpl( id, runAs, declareBlock, outputBlock, block );
        }
        catch( IOException ex ) {
            throw new UncheckedIOException( ex );
        }
    }

    @Override
    public void enterJobDefinition( JobDefinitionContext ctx )
    {
        jobDefinitionContext = ctx;
    }

    //<editor-fold defaultstate="collapsed" desc="Output Parsing">
    @Override
    public void enterOutputStatement( OutputStatementContext ctx )
    {
        enterRule( ctx.extensionStatement() );
    }

    @Override
    public void enterStatement( StatementContext ctx )
    {
        if( ctx.statementWithoutTrailingSubstatement() == null ) {
            throw new IllegalStateException( "Unsupported statement within job output" );
        }

        enterRule( ctx.statementWithoutTrailingSubstatement() );
    }

    @Override
    public void enterStatementWithoutTrailingSubstatement( StatementWithoutTrailingSubstatementContext ctx )
    {
        if( ctx.expressionStatement() == null ) {
            throw new IllegalStateException( "Unsupported statement within job output" );
        }
        enterRule( ctx.expressionStatement() );
    }

    @Override
    public void enterStatementExpression( StatementExpressionContext ctx )
    {
        if( ctx.extensionStatement() == null ) {
            throw new IllegalStateException( "Unsupported statement within job output" );
        }
        enterRule( ctx.extensionStatement() );
    }

    private final List<Statement> outputStatements = new ArrayList<>();

    @Override
    public void enterExtensionStatement( JobParser.ExtensionStatementContext ctx )
    {
        String methodName = ctx.methodName().getText();

        List<ExpressionOperation> newArgs = blockCompiler.getExpressionCompiler().getArgs().apply(
                () -> blockCompiler.getExpressionCompiler().enterRule( ctx.argumentList() )
        );

        Statement stat = TypeOp.invokeOutputStatement( methodName, TypeOp.toArray( newArgs ) );
        outputStatements.add( stat );
    }
    //</editor-fold>

}
