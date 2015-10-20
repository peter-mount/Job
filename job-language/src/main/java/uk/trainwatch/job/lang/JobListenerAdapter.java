/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang;

import java.util.Collection;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 *
 * @author peter
 */
public class JobListenerAdapter
        implements JobListener
{

    public final void enterRule( Collection<? extends ParserRuleContext> l )
    {
        if( l != null && !l.isEmpty() ) {
            l.forEach( this::enterRule );
        }
    }

    public final void enterRule( ParserRuleContext ctx )
    {
        if( ctx != null ) {
            ctx.enterRule( this );
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Compilation Unit">
    @Override
    public void enterCompilationUnit( JobParser.CompilationUnitContext ctx )
    {

    }

    @Override
    public void exitCompilationUnit( JobParser.CompilationUnitContext ctx )
    {

    }

    @Override
    public void enterJobDefinition( JobParser.JobDefinitionContext ctx )
    {

    }

    @Override
    public void exitJobDefinition( JobParser.JobDefinitionContext ctx )
    {

    }

    @Override
    public void enterOutput( JobParser.OutputContext ctx )
    {
        enterRule( ctx.outputStatements() );
    }

    @Override
    public void exitOutput( JobParser.OutputContext ctx )
    {

    }

    @Override
    public void enterOutputStatements( JobParser.OutputStatementsContext ctx )
    {
        enterRule( ctx.outputStatement() );
    }

    @Override
    public void exitOutputStatements( JobParser.OutputStatementsContext ctx )
    {

    }

    @Override
    public void enterOutputStatement( JobParser.OutputStatementContext ctx )
    {

    }

    @Override
    public void exitOutputStatement( JobParser.OutputStatementContext ctx )
    {

    }

    @Override
    public void enterMailOutput( JobParser.MailOutputContext ctx )
    {

    }

    @Override
    public void exitMailOutput( JobParser.MailOutputContext ctx )
    {

    }

    @Override
    public void enterLogOutput( JobParser.LogOutputContext ctx )
    {

    }

    @Override
    public void exitLogOutput( JobParser.LogOutputContext ctx )
    {

    }

    @Override
    public void enterDeclare( JobParser.DeclareContext ctx )
    {

    }

    @Override
    public void exitDeclare( JobParser.DeclareContext ctx )
    {

    }

    @Override
    public void enterDeclareStatements( JobParser.DeclareStatementsContext ctx )
    {
        enterRule( ctx.declareStatement() );
    }

    @Override
    public void exitDeclareStatements( JobParser.DeclareStatementsContext ctx )
    {

    }

    @Override
    public void enterDeclareStatement( JobParser.DeclareStatementContext ctx )
    {
        enterRule( ctx.localVariableDeclarationStatement() );
    }

    @Override
    public void exitDeclareStatement( JobParser.DeclareStatementContext ctx )
    {

    }
//</editor-fold>

    @Override
    public void enterLiteral( JobParser.LiteralContext ctx )
    {

    }

    @Override
    public void exitLiteral( JobParser.LiteralContext ctx )
    {

    }

    //<editor-fold defaultstate="collapsed" desc="Variables">
    @Override
    public void enterVariableDeclaratorList( JobParser.VariableDeclaratorListContext ctx )
    {
        enterRule( ctx.variableDeclarator() );
    }

    @Override
    public void exitVariableDeclaratorList( JobParser.VariableDeclaratorListContext ctx )
    {

    }

    @Override
    public void enterVariableDeclarator( JobParser.VariableDeclaratorContext ctx )
    {

    }

    @Override
    public void exitVariableDeclarator( JobParser.VariableDeclaratorContext ctx )
    {

    }

    @Override
    public void enterVariableDeclaratorId( JobParser.VariableDeclaratorIdContext ctx )
    {

    }

    @Override
    public void exitVariableDeclaratorId( JobParser.VariableDeclaratorIdContext ctx )
    {

    }

    @Override
    public void enterVariableInitializer( JobParser.VariableInitializerContext ctx )
    {

    }

    @Override
    public void exitVariableInitializer( JobParser.VariableInitializerContext ctx )
    {

    }

    @Override
    public void enterLocalVariableDeclarationStatement( JobParser.LocalVariableDeclarationStatementContext ctx )
    {
        enterRule( ctx.localVariableDeclaration() );
    }

    @Override
    public void exitLocalVariableDeclarationStatement( JobParser.LocalVariableDeclarationStatementContext ctx )
    {

    }

    @Override
    public void enterLocalVariableDeclaration( JobParser.LocalVariableDeclarationContext ctx )
    {
        enterRule( ctx.variableDeclaratorList() );
    }

    @Override
    public void exitLocalVariableDeclaration( JobParser.LocalVariableDeclarationContext ctx )
    {

    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Blocks">
    @Override
    public void enterBlock( JobParser.BlockContext ctx )
    {

    }

    @Override
    public void exitBlock( JobParser.BlockContext ctx )
    {

    }

    @Override
    public void enterBlockStatements( JobParser.BlockStatementsContext ctx )
    {
        enterRule( ctx.blockStatement() );
    }

    @Override
    public void exitBlockStatements( JobParser.BlockStatementsContext ctx )
    {

    }

    @Override
    public void enterBlockStatement( JobParser.BlockStatementContext ctx )
    {
        enterRule( ctx.localVariableDeclarationStatement() );
        enterRule( ctx.statement() );
    }

    @Override
    public void exitBlockStatement( JobParser.BlockStatementContext ctx )
    {

    }

    @Override
    public void enterStatement( JobParser.StatementContext ctx )
    {
        enterRule( ctx.forStatement() );
        enterRule( ctx.ifThenElseStatement() );
        enterRule( ctx.ifThenStatement() );
        enterRule( ctx.statementWithoutTrailingSubstatement() );
        enterRule( ctx.whileStatement() );
    }

    @Override
    public void exitStatement( JobParser.StatementContext ctx )
    {

    }

    @Override
    public void enterStatementWithoutTrailingSubstatement( JobParser.StatementWithoutTrailingSubstatementContext ctx )
    {
        enterRule( ctx.block() );
        enterRule( ctx.breakStatement() );
        enterRule( ctx.continueStatement() );
        enterRule( ctx.doStatement() );
        enterRule( ctx.emptyStatement() );
        enterRule( ctx.expressionStatement() );
        enterRule( ctx.returnStatement() );
        enterRule( ctx.throwStatement() );
        enterRule( ctx.tryStatement() );
    }

    @Override
    public void exitStatementWithoutTrailingSubstatement( JobParser.StatementWithoutTrailingSubstatementContext ctx )
    {

    }

    @Override
    public void enterEmptyStatement( JobParser.EmptyStatementContext ctx )
    {

    }

    @Override
    public void exitEmptyStatement( JobParser.EmptyStatementContext ctx )
    {

    }

    @Override
    public void enterExpressionStatement( JobParser.ExpressionStatementContext ctx )
    {
        enterRule( ctx.statementExpression() );
    }

    @Override
    public void exitExpressionStatement( JobParser.ExpressionStatementContext ctx )
    {

    }

    @Override
    public void enterStatementExpression( JobParser.StatementExpressionContext ctx )
    {
        enterRule( ctx.assignment() );
        enterRule( ctx.extensionStatement() );
        enterRule( ctx.logStatement() );
        enterRule( ctx.methodInvocation() );
    }

    @Override
    public void exitStatementExpression( JobParser.StatementExpressionContext ctx )
    {

    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Control statements">
    @Override
    public void enterIfThenStatement( JobParser.IfThenStatementContext ctx )
    {

    }

    @Override
    public void exitIfThenStatement( JobParser.IfThenStatementContext ctx )
    {

    }

    @Override
    public void enterIfThenElseStatement( JobParser.IfThenElseStatementContext ctx )
    {

    }

    @Override
    public void exitIfThenElseStatement( JobParser.IfThenElseStatementContext ctx )
    {

    }

    @Override
    public void enterWhileStatement( JobParser.WhileStatementContext ctx )
    {

    }

    @Override
    public void exitWhileStatement( JobParser.WhileStatementContext ctx )
    {

    }

    @Override
    public void enterDoStatement( JobParser.DoStatementContext ctx )
    {

    }

    @Override
    public void exitDoStatement( JobParser.DoStatementContext ctx )
    {

    }

    @Override
    public void enterForStatement( JobParser.ForStatementContext ctx )
    {

    }

    @Override
    public void exitForStatement( JobParser.ForStatementContext ctx )
    {

    }

    @Override
    public void enterBasicForStatement( JobParser.BasicForStatementContext ctx )
    {

    }

    @Override
    public void exitBasicForStatement( JobParser.BasicForStatementContext ctx )
    {

    }

    @Override
    public void enterForInit( JobParser.ForInitContext ctx )
    {

    }

    @Override
    public void exitForInit( JobParser.ForInitContext ctx )
    {

    }

    @Override
    public void enterForUpdate( JobParser.ForUpdateContext ctx )
    {

    }

    @Override
    public void exitForUpdate( JobParser.ForUpdateContext ctx )
    {

    }

    @Override
    public void enterStatementExpressionList( JobParser.StatementExpressionListContext ctx )
    {
        enterRule( ctx.statementExpression() );
    }

    @Override
    public void exitStatementExpressionList( JobParser.StatementExpressionListContext ctx )
    {

    }

    @Override
    public void enterEnhancedForStatement( JobParser.EnhancedForStatementContext ctx )
    {

    }

    @Override
    public void exitEnhancedForStatement( JobParser.EnhancedForStatementContext ctx )
    {

    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Expressions">
    @Override
    public void enterExpressionName( JobParser.ExpressionNameContext ctx )
    {

    }

    @Override
    public void exitExpressionName( JobParser.ExpressionNameContext ctx )
    {

    }

    @Override
    public void enterAssignmentOperator( JobParser.AssignmentOperatorContext ctx )
    {

    }

    @Override
    public void exitAssignmentOperator( JobParser.AssignmentOperatorContext ctx )
    {

    }

    @Override
    public void enterExpression( JobParser.ExpressionContext ctx )
    {
        enterRule( ctx.lambdaExpression() );
        enterRule( ctx.assignmentExpression() );
    }

    @Override
    public void exitExpression( JobParser.ExpressionContext ctx )
    {

    }

    @Override
    public void enterAssignmentExpression( JobParser.AssignmentExpressionContext ctx )
    {
        enterRule( ctx.conditionalExpression() );
        enterRule( ctx.assignment() );
    }

    @Override
    public void exitAssignmentExpression( JobParser.AssignmentExpressionContext ctx )
    {

    }

    @Override
    public void enterAssignment( JobParser.AssignmentContext ctx )
    {

    }

    @Override
    public void exitAssignment( JobParser.AssignmentContext ctx )
    {

    }

    @Override
    public void enterLeftHandSide( JobParser.LeftHandSideContext ctx )
    {

    }

    @Override
    public void exitLeftHandSide( JobParser.LeftHandSideContext ctx )
    {

    }

    @Override
    public void enterConditionalExpression( JobParser.ConditionalExpressionContext ctx )
    {

    }

    @Override
    public void exitConditionalExpression( JobParser.ConditionalExpressionContext ctx )
    {

    }

    @Override
    public void enterConditionalOrExpression( JobParser.ConditionalOrExpressionContext ctx )
    {

    }

    @Override
    public void exitConditionalOrExpression( JobParser.ConditionalOrExpressionContext ctx )
    {

    }

    @Override
    public void enterConditionalAndExpression( JobParser.ConditionalAndExpressionContext ctx )
    {

    }

    @Override
    public void exitConditionalAndExpression( JobParser.ConditionalAndExpressionContext ctx )
    {

    }

    @Override
    public void enterInclusiveOrExpression( JobParser.InclusiveOrExpressionContext ctx )
    {

    }

    @Override
    public void exitInclusiveOrExpression( JobParser.InclusiveOrExpressionContext ctx )
    {

    }

    @Override
    public void enterExclusiveOrExpression( JobParser.ExclusiveOrExpressionContext ctx )
    {

    }

    @Override
    public void exitExclusiveOrExpression( JobParser.ExclusiveOrExpressionContext ctx )
    {

    }

    @Override
    public void enterAndExpression( JobParser.AndExpressionContext ctx )
    {

    }

    @Override
    public void exitAndExpression( JobParser.AndExpressionContext ctx )
    {

    }

    @Override
    public void enterEqualityExpression( JobParser.EqualityExpressionContext ctx )
    {

    }

    @Override
    public void exitEqualityExpression( JobParser.EqualityExpressionContext ctx )
    {

    }

    @Override
    public void enterRelationalExpression( JobParser.RelationalExpressionContext ctx )
    {

    }

    @Override
    public void exitRelationalExpression( JobParser.RelationalExpressionContext ctx )
    {

    }

    @Override
    public void enterShiftExpression( JobParser.ShiftExpressionContext ctx )
    {

    }

    @Override
    public void exitShiftExpression( JobParser.ShiftExpressionContext ctx )
    {

    }

    @Override
    public void enterAdditiveExpression( JobParser.AdditiveExpressionContext ctx )
    {

    }

    @Override
    public void exitAdditiveExpression( JobParser.AdditiveExpressionContext ctx )
    {

    }

    @Override
    public void enterMultiplicativeExpression( JobParser.MultiplicativeExpressionContext ctx )
    {

    }

    @Override
    public void exitMultiplicativeExpression( JobParser.MultiplicativeExpressionContext ctx )
    {

    }

    @Override
    public void enterUnaryExpression( JobParser.UnaryExpressionContext ctx )
    {

    }

    @Override
    public void exitUnaryExpression( JobParser.UnaryExpressionContext ctx )
    {

    }

    @Override
    public void enterPreIncrementExpression( JobParser.PreIncrementExpressionContext ctx )
    {

    }

    @Override
    public void exitPreIncrementExpression( JobParser.PreIncrementExpressionContext ctx )
    {

    }

    @Override
    public void enterPreDecrementExpression( JobParser.PreDecrementExpressionContext ctx )
    {

    }

    @Override
    public void exitPreDecrementExpression( JobParser.PreDecrementExpressionContext ctx )
    {

    }

    @Override
    public void enterUnaryExpressionNotPlusMinus( JobParser.UnaryExpressionNotPlusMinusContext ctx )
    {

    }

    @Override
    public void exitUnaryExpressionNotPlusMinus( JobParser.UnaryExpressionNotPlusMinusContext ctx )
    {

    }

    @Override
    public void enterPostfixExpression( JobParser.PostfixExpressionContext ctx )
    {

    }

    @Override
    public void exitPostfixExpression( JobParser.PostfixExpressionContext ctx )
    {

    }

    @Override
    public void enterPostIncrementExpression( JobParser.PostIncrementExpressionContext ctx )
    {

    }

    @Override
    public void exitPostIncrementExpression( JobParser.PostIncrementExpressionContext ctx )
    {

    }

    @Override
    public void enterPostIncrementExpression_lf_postfixExpression(
            JobParser.PostIncrementExpression_lf_postfixExpressionContext ctx )
    {

    }

    @Override
    public void exitPostIncrementExpression_lf_postfixExpression(
            JobParser.PostIncrementExpression_lf_postfixExpressionContext ctx )
    {

    }

    @Override
    public void enterPostDecrementExpression( JobParser.PostDecrementExpressionContext ctx )
    {

    }

    @Override
    public void exitPostDecrementExpression( JobParser.PostDecrementExpressionContext ctx )
    {

    }

    @Override
    public void enterPostDecrementExpression_lf_postfixExpression(
            JobParser.PostDecrementExpression_lf_postfixExpressionContext ctx )
    {

    }

    @Override
    public void exitPostDecrementExpression_lf_postfixExpression(
            JobParser.PostDecrementExpression_lf_postfixExpressionContext ctx )
    {

    }

    @Override
    public void enterPrimary( JobParser.PrimaryContext ctx )
    {
    }

    @Override
    public void exitPrimary( JobParser.PrimaryContext ctx )
    {

    }

    @Override
    public void visitTerminal( TerminalNode tn )
    {

    }

    @Override
    public void visitErrorNode( ErrorNode en )
    {

    }

    @Override
    public void enterEveryRule( ParserRuleContext prc )
    {

    }

    @Override
    public void exitEveryRule( ParserRuleContext prc )
    {

    }

    @Override
    public void enterStringExpression( JobParser.StringExpressionContext ctx )
    {
    }

    @Override
    public void exitStringExpression( JobParser.StringExpressionContext ctx )
    {
    }
//</editor-fold>

    @Override
    public void enterLogStatement( JobParser.LogStatementContext ctx )
    {
    }

    @Override
    public void exitLogStatement( JobParser.LogStatementContext ctx )
    {
    }

    //<editor-fold defaultstate="collapsed" desc="Method invocation">
    @Override
    public void enterNewObject( JobParser.NewObjectContext ctx )
    {
    }

    @Override
    public void exitNewObject( JobParser.NewObjectContext ctx )
    {
    }

    @Override
    public void enterMethodName( JobParser.MethodNameContext ctx )
    {
    }

    @Override
    public void exitMethodName( JobParser.MethodNameContext ctx )
    {
    }

    @Override
    public void enterMethodInvocation( JobParser.MethodInvocationContext ctx )
    {
    }

    @Override
    public void exitMethodInvocation( JobParser.MethodInvocationContext ctx )
    {
    }

    @Override
    public void enterArgumentList( JobParser.ArgumentListContext ctx )
    {
    }

    @Override
    public void exitArgumentList( JobParser.ArgumentListContext ctx )
    {
    }

    @Override
    public void enterExtensionStatement( JobParser.ExtensionStatementContext ctx )
    {
    }

    @Override
    public void exitExtensionStatement( JobParser.ExtensionStatementContext ctx )
    {
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Lambda expressions">
    @Override
    public void enterLambdaExpression( JobParser.LambdaExpressionContext ctx )
    {
        enterRule( ctx.lambdaParameters() );
        enterRule( ctx.lambdaBody() );
    }

    @Override
    public void exitLambdaExpression( JobParser.LambdaExpressionContext ctx )
    {
    }

    @Override
    public void enterLambdaParameters( JobParser.LambdaParametersContext ctx )
    {
        enterRule( ctx.inferredFormalParameterList() );
    }

    @Override
    public void exitLambdaParameters( JobParser.LambdaParametersContext ctx )
    {
    }

    @Override
    public void enterInferredFormalParameterList( JobParser.InferredFormalParameterListContext ctx )
    {
    }

    @Override
    public void exitInferredFormalParameterList( JobParser.InferredFormalParameterListContext ctx )
    {
    }

    @Override
    public void enterLambdaBody( JobParser.LambdaBodyContext ctx )
    {
        enterRule( ctx.block() );
        enterRule( ctx.expression() );
    }

    @Override
    public void exitLambdaBody( JobParser.LambdaBodyContext ctx )
    {
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Block break/throw statements">
    @Override
    public void enterThrowStatement( JobParser.ThrowStatementContext ctx )
    {
    }

    @Override
    public void exitThrowStatement( JobParser.ThrowStatementContext ctx )
    {
    }

    @Override
    public void enterBreakStatement( JobParser.BreakStatementContext ctx )
    {
    }

    @Override
    public void exitBreakStatement( JobParser.BreakStatementContext ctx )
    {
    }

    @Override
    public void enterContinueStatement( JobParser.ContinueStatementContext ctx )
    {
    }

    @Override
    public void exitContinueStatement( JobParser.ContinueStatementContext ctx )
    {
    }

    @Override
    public void enterReturnStatement( JobParser.ReturnStatementContext ctx )
    {
    }

    @Override
    public void exitReturnStatement( JobParser.ReturnStatementContext ctx )
    {
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="try">
    @Override
    public void enterCatchClause( JobParser.CatchClauseContext ctx )
    {

    }

    @Override
    public void exitCatchClause( JobParser.CatchClauseContext ctx )
    {

    }

    @Override
    public void enterCatchFormalParameter( JobParser.CatchFormalParameterContext ctx )
    {

    }

    @Override
    public void exitCatchFormalParameter( JobParser.CatchFormalParameterContext ctx )
    {

    }

    @Override
    public void enterCatchType( JobParser.CatchTypeContext ctx )
    {

    }

    @Override
    public void exitCatchType( JobParser.CatchTypeContext ctx )
    {

    }

    @Override
    public void enterCatches( JobParser.CatchesContext ctx )
    {

    }

    @Override
    public void exitCatches( JobParser.CatchesContext ctx )
    {

    }

    @Override
    public void enterFinally_( JobParser.Finally_Context ctx )
    {

    }

    @Override
    public void exitFinally_( JobParser.Finally_Context ctx )
    {

    }

    @Override
    public void enterResource( JobParser.ResourceContext ctx )
    {

    }

    @Override
    public void exitResource( JobParser.ResourceContext ctx )
    {

    }

    @Override
    public void enterResourceList( JobParser.ResourceListContext ctx )
    {

    }

    @Override
    public void exitResourceList( JobParser.ResourceListContext ctx )
    {

    }

    @Override
    public void enterResourceSpecification( JobParser.ResourceSpecificationContext ctx )
    {

    }

    @Override
    public void exitResourceSpecification( JobParser.ResourceSpecificationContext ctx )
    {

    }

    @Override
    public void enterTryStatement( JobParser.TryStatementContext ctx )
    {
    }

    @Override
    public void exitTryStatement( JobParser.TryStatementContext ctx )
    {

    }
//</editor-fold>

}
