/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.header;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.antlr.v4.runtime.tree.TerminalNode;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.lang.AbstractCompiler;
import uk.trainwatch.job.lang.block.BlockCompiler;
import uk.trainwatch.job.lang.JobParser;
import uk.trainwatch.job.lang.JobParser.*;
import uk.trainwatch.job.lang.Statement;

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
    private Set<String> imports;
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

        imports = new HashSet<>();
        enterRule( ctx.importDeclaration() );

        // Optional declare { }
        enterRule( ctx.declare(), blockCompiler );
        declareBlock = blockCompiler.getBlock();

        // Optional output { }
        enterRule( ctx.output(), blockCompiler );
        outputBlock = blockCompiler.getBlock();

        // The main body
        block = blockCompiler.getBlock( ctx.block(), false );

        List<TerminalNode> strings = jobDefinitionContext.StringLiteral();
        final String id = getString( strings.get( 0 ) );
        final String runAs = strings.size() > 1 ? getString( strings.get( 1 ) ) : "Local";

        job = new JobImpl( id, runAs, imports, declareBlock, outputBlock, block );
    }

    @Override
    public void enterImportDeclaration( ImportDeclarationContext ctx )
    {
        name = null;
        enterRule( ctx.typeName() );
        imports.add( name );
    }

    @Override
    public void enterTypeName( TypeNameContext ctx )
    {
        name = ctx.Identifier().getText();
    }

    @Override
    public void enterJobDefinition( JobDefinitionContext ctx )
    {
        jobDefinitionContext = ctx;
    }

    @Override
    public void enterOutputStatement( OutputStatementContext ctx )
    {
        throw new UnsupportedOperationException();
    }

}
