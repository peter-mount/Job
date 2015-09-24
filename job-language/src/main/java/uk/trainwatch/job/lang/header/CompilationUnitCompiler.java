/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.header;

import java.util.List;
import org.antlr.v4.runtime.tree.TerminalNode;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.lang.AbstractCompiler;
import uk.trainwatch.job.lang.block.BlockCompiler;
import uk.trainwatch.job.lang.JobParser;
import uk.trainwatch.job.lang.JobParser.*;
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.block.BlockCompiler.BlockScope;

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

        declareBlock = blockCompiler.getBlock( ctx.declare(), true );
        outputBlock = blockCompiler.getBlock( ctx.output(), true );
        block = blockCompiler.getBlock( ctx.block(), false );

        List<TerminalNode> strings = jobDefinitionContext.StringLiteral();
        final String id = getString( strings.get( 0 ) );
        final String runAs = strings.size() > 1 ? getString( strings.get( 1 ) ) : "Local";

        job = new JobImpl( id, runAs, declareBlock, outputBlock, block );
    }

    @Override
    public void enterJobDefinition( JobDefinitionContext ctx )
    {
        jobDefinitionContext = ctx;
    }

    @Override
    public void enterDeclare( DeclareContext ctx )
    {
        enterRule( ctx.declareStatements() );
    }

    @Override
    public void enterDeclareStatements( DeclareStatementsContext ctx )
    {
        enterRule( ctx.declareStatement() );
    }

    @Override
    public void enterDeclareStatement( DeclareStatementContext ctx )
    {
        enterRule( ctx.localVariableDeclarationStatement(), blockCompiler );
    }

    @Override
    public void enterOutput( OutputContext ctx )
    {
        enterRule( ctx.outputStatements() );
    }

    @Override
    public void enterOutputStatements( OutputStatementsContext ctx )
    {
        enterRule( ctx.outputStatement() );
    }

    @Override
    public void enterOutputStatement( OutputStatementContext ctx )
    {
        throw new UnsupportedOperationException();
    }

}
