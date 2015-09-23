/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.expr;

import java.beans.Expression;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.StringJoiner;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import uk.trainwatch.job.lang.AbstractCompiler;
import uk.trainwatch.job.lang.JobParser;

/**
 *
 * @author peter
 */
public class ExpressionCompiler
        extends AbstractCompiler
{

    private final Deque<ExpressionOperation> stack = new ArrayDeque<>();
    private ExpressionOperation expression;
    private String name;
    private StringJoiner ambiguousName;

    private void push( ExpressionOperation op )
    {
        stack.push( op == null ? s -> null : op );
    }

    private ExpressionOperation pop()
    {
        return stack.pop();
    }

    public ExpressionOperation getExpression()
    {
        return expression;
    }

    public ExpressionCompiler reset()
    {
        expression = null;
        return this;
    }

    @Override
    public void enterStringExpression( JobParser.StringExpressionContext ctx )
    {
        TerminalNode tn = ctx.StringLiteral();
        if( tn != null ) {
            expression = Constants.constant( getString( tn ) );
        }
        else {
            enterRule( ctx.expression() );
        }
    }

    @Override
    public void enterExpression( JobParser.ExpressionContext ctx )
    {
        enterRule( ctx.assignmentExpression() );
    }

    //<editor-fold defaultstate="collapsed" desc="Logic">
    @Override
    public void enterConditionalExpression( JobParser.ConditionalExpressionContext ctx )
    {
        enterRule( ctx.conditionalOrExpression() );
        if( ctx.expression() != null && ctx.conditionalExpression() != null ) {
            ExpressionOperation expr = expression;

            enterRule( ctx.expression() );
            ExpressionOperation t = expression;

            enterRule( ctx.conditionalExpression() );
            expression = Logic.conditional( expr, t, expression );
        }
    }

    @Override
    public void enterConditionalOrExpression( JobParser.ConditionalOrExpressionContext ctx )
    {
        biop( ctx.conditionalOrExpression(), ctx.conditionalAndExpression(), Logic::conditionalOr );
    }

    @Override
    public void enterConditionalAndExpression( JobParser.ConditionalAndExpressionContext ctx )
    {
        biop( ctx.conditionalAndExpression(), ctx.inclusiveOrExpression(), Logic::conditionalAnd );
    }

    @Override
    public void enterInclusiveOrExpression( JobParser.InclusiveOrExpressionContext ctx )
    {
        biop( ctx.inclusiveOrExpression(), ctx.exclusiveOrExpression(), Logic::inclusiveOr );
    }

    @Override
    public void enterExclusiveOrExpression( JobParser.ExclusiveOrExpressionContext ctx )
    {
        biop( ctx.exclusiveOrExpression(), ctx.andExpression(), Logic::exclusiveOr );
    }

    @Override
    public void enterAndExpression( JobParser.AndExpressionContext ctx )
    {
        biop( ctx.andExpression(), ctx.equalityExpression(), Logic::and );
    }

    @Override
    public void enterEqualityExpression( JobParser.EqualityExpressionContext ctx )
    {
        if( ctx.equalityExpression() == null ) {
            enterRule( ctx.relationalExpression() );
        }
        else {
            switch( ctx.getChild( 1 ).getText() ) {
                case "==":
                    biop( ctx.equalityExpression(), ctx.relationalExpression(), Logic::equality );
                    break;
                case "!=":
                    biop( ctx.equalityExpression(), ctx.relationalExpression(), Logic::inequality );
                    break;
            }
        }
    }

    @Override
    public void enterRelationalExpression( JobParser.RelationalExpressionContext ctx )
    {
        if( ctx.relationalExpression() == null ) {
            enterRule( ctx.shiftExpression() );
        }
        else {
            switch( ctx.getChild( 1 ).getText() ) {
                case "<":
                    biop( ctx.relationalExpression(), ctx.relationalExpression(), Logic::lessThan );
                    break;
                case ">":
                    biop( ctx.relationalExpression(), ctx.relationalExpression(), Logic::greaterThan );
                    break;
                case "<=":
                    biop( ctx.relationalExpression(), ctx.relationalExpression(), Logic::lessThanEqual );
                    break;
                case ">=":
                    biop( ctx.relationalExpression(), ctx.relationalExpression(), Logic::greaterThanEqual );
                    break;
            }
        }
    }

    @Override
    public void enterShiftExpression( JobParser.ShiftExpressionContext ctx )
    {
        if( ctx.shiftExpression() == null ) {
            enterRule( ctx.additiveExpression() );
        }
        else {
            switch( ctx.getChild( 1 ).getText() ) {
                case "<":
                    biop( ctx.shiftExpression(), ctx.additiveExpression(), Logic::shiftLeft );
                    break;
                case ">":
                    if( ctx.getChildCount() == 5 ) {
                        // >>>
                        biop( ctx.shiftExpression(), ctx.additiveExpression(), Logic::shiftRightClear );
                    }
                    else {
                        // >>
                        biop( ctx.shiftExpression(), ctx.additiveExpression(), Logic::shiftRight );
                    }
                    break;
            }
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Assignment">
    @Override
    public void enterAssignmentExpression( JobParser.AssignmentExpressionContext ctx )
    {
        enterRule( ctx.conditionalExpression() );
        enterRule( ctx.assignment() );
    }

    @Override
    public void enterAssignment( JobParser.AssignmentContext ctx )
    {
        enterRule( ctx.leftHandSide() );
        final String varName = name;

        enterRule( ctx.expression() );

        final String op = ctx.assignmentOperator().getText();
        switch( op ) {
            case "=":
                expression = Assignment.setVariable( name, expression );
                break;
            default:
                throw new IllegalArgumentException( "Unsupported assignment " + op );
        }
    }

    @Override
    public void enterLeftHandSide( JobParser.LeftHandSideContext ctx )
    {
        enterRule( ctx.expressionName() );
    }

    @Override
    public void enterExpressionName( JobParser.ExpressionNameContext ctx )
    {
        name = ctx.Identifier().getText();
        if( ctx.ambiguousName() != null ) {
            ambiguousName = new StringJoiner( "." );
            enterRule( ctx.ambiguousName() );
            ambiguousName.add( name );
            name = ambiguousName.toString();
        }
    }

    @Override
    public void enterAmbiguousName( JobParser.AmbiguousNameContext ctx )
    {
        enterRule( ctx.ambiguousName() );
        ambiguousName.add( ctx.Identifier().getText() );
    }
    //</editor-fold>

    private void biop( ParserRuleContext lc, ParserRuleContext rc, BiOp op )
    {
        if( lc == null ) {
            enterRule( rc );
        }
        else {
            enterRule( lc );
            ExpressionOperation lhs = expression;
            enterRule( rc );
            expression = op.apply( lhs, expression );
        }
    }

    public static interface BiOp
    {

        ExpressionOperation apply( ExpressionOperation lhs, ExpressionOperation rhs );
    }

}
