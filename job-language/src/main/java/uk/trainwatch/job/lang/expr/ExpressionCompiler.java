/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang.expr;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import uk.trainwatch.job.lang.AbstractCompiler;
import uk.trainwatch.job.lang.JobParser;
import uk.trainwatch.job.lang.block.TypeOp;

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
                    biop( ctx.relationalExpression(), ctx.shiftExpression(), Logic::lessThan );
                    break;
                case ">":
                    biop( ctx.relationalExpression(), ctx.shiftExpression(), Logic::greaterThan );
                    break;
                case "<=":
                    biop( ctx.relationalExpression(), ctx.shiftExpression(), Logic::lessThanEqual );
                    break;
                case ">=":
                    biop( ctx.relationalExpression(), ctx.shiftExpression(), Logic::greaterThanEqual );
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

    @Override
    public void enterAdditiveExpression( JobParser.AdditiveExpressionContext ctx )
    {
        if( ctx.additiveExpression() == null ) {
            enterRule( ctx.multiplicativeExpression() );
        }
        else {
            switch( ctx.getChild( 1 ).getText() ) {
                case "+":
                    biop( ctx.additiveExpression(), ctx.multiplicativeExpression(), Arithmetic::add );
                    break;
                case "-":
                    biop( ctx.additiveExpression(), ctx.multiplicativeExpression(), Arithmetic::sub );
                    break;
            }
        }
    }

    @Override
    public void enterMultiplicativeExpression( JobParser.MultiplicativeExpressionContext ctx )
    {
        if( ctx.multiplicativeExpression() == null ) {
            enterRule( ctx.unaryExpression() );
        }
        else {
            switch( ctx.getChild( 1 ).getText() ) {
                case "*":
                    biop( ctx.multiplicativeExpression(), ctx.unaryExpression(), Arithmetic::mult );
                    break;
                case "/":
                    biop( ctx.multiplicativeExpression(), ctx.unaryExpression(), Arithmetic::div );
                    break;
                case "%":
                    biop( ctx.multiplicativeExpression(), ctx.unaryExpression(), Arithmetic::mod );
                    break;
            }
        }
    }

    @Override
    public void enterUnaryExpression( JobParser.UnaryExpressionContext ctx )
    {
        enterRule( ctx.preIncrementExpression() );
        enterRule( ctx.preDecrementExpression() );

        enterRule( ctx.unaryExpressionNotPlusMinus() );

        if( ctx.unaryExpression() != null ) {
            enterRule( ctx.unaryExpression() );
            if( "-".equals( ctx.getChild( 0 ).getText() ) ) {
                expression = Arithmetic.negate( expression );
            }
        }
    }

    @Override
    public void enterPreIncrementExpression( JobParser.PreIncrementExpressionContext ctx )
    {
        throw new UnsupportedOperationException( ctx.getText() );
    }

    @Override
    public void enterPreDecrementExpression( JobParser.PreDecrementExpressionContext ctx )
    {
        throw new UnsupportedOperationException( ctx.getText() );
    }

    @Override
    public void enterUnaryExpressionNotPlusMinus( JobParser.UnaryExpressionNotPlusMinusContext ctx )
    {
        if( ctx.unaryExpression() != null ) {
            enterRule( ctx.unaryExpression() );
            switch( ctx.getChild( 0 ).getText() ) {
                case "~":
                    expression = Arithmetic.tilde( expression );
                    break;
                case "!":
                    expression = Arithmetic.not( expression );
                    break;
            }
        }
        else {
            enterRule( ctx.postfixExpression() );
        }
    }

    @Override
    public void enterPostfixExpression( JobParser.PostfixExpressionContext ctx )
    {
        if( ctx.expressionName() != null ) {
            enterRule( ctx.expressionName() );
            final String varName = name;
            expression = scope -> scope.getVar( varName );
        }
        else {
            enterRule( ctx.primary() );

            // Postfix not supported
            enterRule( ctx.postDecrementExpression_lf_postfixExpression() );
            enterRule( ctx.postIncrementExpression_lf_postfixExpression() );
        }
    }

    @Override
    public void enterPostDecrementExpression( JobParser.PostDecrementExpressionContext ctx )
    {
        throw new UnsupportedOperationException( ctx.getText() );
    }

    @Override
    public void enterPostIncrementExpression( JobParser.PostIncrementExpressionContext ctx )
    {
        throw new UnsupportedOperationException( ctx.getText() );
    }

    @Override
    public void enterLiteral( JobParser.LiteralContext ctx )
    {
        op( ctx.BooleanLiteral(), n -> Boolean.valueOf( n.getText() ) ? Logic.trueOp() : Logic.falseOp() );
        op( ctx.FloatingPointLiteral(), n -> Constants.constant( Float.valueOf( n.getText() ) ) );
        op( ctx.IntegerLiteral(), n -> Constants.constant( Integer.valueOf( n.getText() ) ) );
        op( ctx.StringLiteral(), n -> Constants.constant( getString( n ) ) );
        op( ctx.NullLiteral(), n -> scope -> null );

        if( ctx.CharacterLiteral() != null ) {
            throw new UnsupportedOperationException( "Character literals currently not supported" );
        }
        //op( ctx.CharacterLiteral(), n -> null );
    }

    private List<ExpressionOperation> args;

    @Override
    public void enterPrimary( JobParser.PrimaryContext ctx )
    {
        super.enterPrimary( ctx );

        if( ctx.Identifier() != null && !ctx.Identifier().isEmpty() ) {
            String type = ctx.Identifier()
                    .stream()
                    .map( TerminalNode::getText )
                    .collect( Collectors.joining( "." ) );

            enterRule( ctx.argumentList() );

            expression = TypeOp.construct( type, TypeOp.toArray( args ) );
        }

        if( ctx.expressionName() != null ) {
            enterRule( ctx.expressionName() );
            String type = name;

            enterRule( ctx.argumentList() );

            expression = TypeOp.construct( type, TypeOp.toArray( args ) );
        }

//        if( ctx.primary()!=null) {
//            enterRule( ctx.primary());
//            ExpressionOperation primary = expression;
//            args=new LinkedList<>();
//            enterRule( ctx.argumentList());
//            expression = TypeOp.construct( type, TypeOp.toArray( args ));
//        }
    }

    @Override
    public void enterArgumentList( JobParser.ArgumentListContext ctx )
    {
        args = new LinkedList<>();
        if( ctx.expression() != null ) {
            for( JobParser.ExpressionContext exp: ctx.expression() ) {
                enterRule( exp );
                args.add( expression );
            }
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Assignment">
    @Override
    public void enterAssignment( JobParser.AssignmentContext ctx )
    {
        enterRule( ctx.leftHandSide() );
        final String varName = name;
        System.out.println( varName );

        enterRule( ctx.expression() );

        final String op = ctx.assignmentOperator().getText();
        switch( op ) {
            case "=":
                expression = Assignment.setVariable( varName, expression );
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
        if( ctx.ambiguousName() == null ) {
            name = ctx.Identifier().getText();
        }
        else {
            ambiguousName = new StringJoiner( "." );
            enterRule( ctx.ambiguousName() );
            ambiguousName.add( ctx.Identifier().getText() );
            name = ambiguousName.toString();
        }
    }

    @Override
    public void enterMethodName( JobParser.MethodNameContext ctx )
    {
        name = ctx.Identifier().getText();
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

    private <T> void op( T val, Function<T, ExpressionOperation> mapper )
    {
        if( val != null ) {
            expression = mapper.apply( val );
        }
    }

    public static interface BiOp
    {

        ExpressionOperation apply( ExpressionOperation lhs, ExpressionOperation rhs );
    }

}
