/*
 * The basic script language grammar
 */
grammar Script;

import Literals;

literal
    :    IntegerLiteral
    |    FloatingPointLiteral
    |    BooleanLiteral
    |    CharacterLiteral
    |    StringLiteral
    |    NullLiteral
    ;

variableDeclaratorList
    :    variableDeclarator (',' variableDeclarator)*
    ;

variableDeclarator
    :    variableDeclaratorId ('=' variableInitializer)?
    ;

variableDeclaratorId
    :    Identifier //dims?
    ;

variableInitializer
    :    expression
//  |    arrayInitializer
    ;

block
    :    '{' blockStatements? '}'
    ;

blockStatements
    :    blockStatement blockStatement*
    ;

blockStatement
    :    localVariableDeclarationStatement
    |    statement
    ;

localVariableDeclarationStatement
    :    localVariableDeclaration ';'
    ;

localVariableDeclaration
    :    variableDeclaratorList
    ;

statement
    :    statementWithoutTrailingSubstatement
    |    ifThenStatement
    |    ifThenElseStatement
    |    whileStatement
    |    forStatement
    ;

statementWithoutTrailingSubstatement
    :    block
    |    emptyStatement
    |    expressionStatement
//  |    assertStatement
//  |    switchStatement
    |    doStatement
//  |    breakStatement
//  |    continueStatement
//  |    returnStatement
//  |    throwStatement
//  |    tryStatement
    ;

emptyStatement
    :    ';'
    ;

expressionStatement
    :    statementExpression ';'
    ;

statementExpression
    :    assignment
//  |    preIncrementExpression
//  |    preDecrementExpression
//  |    postIncrementExpression
//  |    postDecrementExpression
    |    methodInvocation
//  |    classInstanceCreationExpression
    |   logStatement
    ;

ifThenStatement
    :    'if' '(' expression ')' statement
    ;

ifThenElseStatement
    :    'if' '(' expression ')' statement 'else' statement
    ;

whileStatement
    :    'while' '(' expression ')' statement
    ;

doStatement
    :    'do' statement 'while' '(' expression ')' ';'
    ;

forStatement
    :    basicForStatement
    |    enhancedForStatement
    ;

basicForStatement
    :    'for' '(' forInit? ';' expression? ';' forUpdate? ')' statement
    ;

forInit
    :    statementExpressionList
    |    localVariableDeclaration
    ;

forUpdate
    :    statementExpressionList
    ;

statementExpressionList
    :    statementExpression (',' statementExpression)*
    ;

enhancedForStatement
    :    'for' '('  variableDeclaratorId ':' expression ')' statement
    ;
packageName
        :       Identifier
        |       packageName '.' Identifier
        ;

typeName
        :       Identifier
        |       packageOrTypeName '.' Identifier
        ;

packageOrTypeName
        :       Identifier
        |       packageOrTypeName '.' Identifier
        ;

expressionName
    :    Identifier
    |    ambiguousName '.' Identifier
    ;

methodName
    :    Identifier
    ;

ambiguousName
    :    Identifier
    |    ambiguousName '.' Identifier
    ;

assignmentOperator
    :    '='
    |    '*='
    |    '/='
    |    '%='
    |    '+='
    |    '-='
    |    '<<='
    |    '>>='
    |    '>>>='
    |    '&='
    |    '^='
    |    '|='
    ;

expression
    :    assignmentExpression
    ;

assignmentExpression
    :    conditionalExpression
    |    assignment
    ;

assignment
    :    leftHandSide assignmentOperator expression
    ;

leftHandSide
    :    expressionName
//  |    fieldAccess
//  |    arrayAccess
    ;

conditionalExpression
    :    conditionalOrExpression
    |    conditionalOrExpression '?' expression ':' conditionalExpression
    ;

conditionalOrExpression
    :    conditionalAndExpression
    |    conditionalOrExpression '||' conditionalAndExpression
    ;

conditionalAndExpression
    :    inclusiveOrExpression
    |    conditionalAndExpression '&&' inclusiveOrExpression
    ;

inclusiveOrExpression
    :    exclusiveOrExpression
    |    inclusiveOrExpression '|' exclusiveOrExpression
    ;

exclusiveOrExpression
    :    andExpression
    |    exclusiveOrExpression '^' andExpression
    ;

andExpression
    :    equalityExpression
    |    andExpression '&' equalityExpression
    ;

equalityExpression
    :    relationalExpression
    |    equalityExpression '==' relationalExpression
    |    equalityExpression '!=' relationalExpression
    ;

relationalExpression
    :    shiftExpression
    |    relationalExpression '<' shiftExpression
    |    relationalExpression '>' shiftExpression
    |    relationalExpression '<=' shiftExpression
    |    relationalExpression '>=' shiftExpression
//  |    relationalExpression 'instanceof' referenceType
    ;

shiftExpression
    :    additiveExpression
    |    shiftExpression '<' '<' additiveExpression
    |    shiftExpression '>' '>' additiveExpression
    |    shiftExpression '>' '>' '>' additiveExpression
    ;

additiveExpression
    :    multiplicativeExpression
    |    additiveExpression '+' multiplicativeExpression
    |    additiveExpression '-' multiplicativeExpression
    ;

multiplicativeExpression
    :    unaryExpression
    |    multiplicativeExpression '*' unaryExpression
    |    multiplicativeExpression '/' unaryExpression
    |    multiplicativeExpression '%' unaryExpression
    ;

unaryExpression
    :    preIncrementExpression
    |    preDecrementExpression
    |    '+' unaryExpression
    |    '-' unaryExpression
    |    unaryExpressionNotPlusMinus
    ;

preIncrementExpression
    :    '++' unaryExpression
    ;

preDecrementExpression
    :    '--' unaryExpression
    ;

unaryExpressionNotPlusMinus
    :    postfixExpression
    |    '~' unaryExpression
    |    '!' unaryExpression
//  |    castExpression
    ;

postfixExpression
    :    (    primary
        |    expressionName
        )
        (    postIncrementExpression_lf_postfixExpression
        |    postDecrementExpression_lf_postfixExpression
        )*
    ;

postIncrementExpression
    :    postfixExpression '++'
    ;

postIncrementExpression_lf_postfixExpression
    :    '++'
    ;

postDecrementExpression
    :    postfixExpression '--'
    ;

postDecrementExpression_lf_postfixExpression
    :    '--'
    ;

/*
 * Productions from §15 (Expressions)
 */

primary
    :    literal
    |    '(' expression ')'
//  |    fieldAccess
//  |    methodInvocation
//  |    methodReference
    |   'new' Identifier ('.' Identifier)* '(' argumentList? ')'
    |   expressionName '.' 'new' Identifier '(' argumentList? ')'
//    |   primary '.' 'new' Identifier '(' argumentList? ')'
    ;

stringExpression
    : ( StringLiteral | expression )
    ;

methodInvocation
        :       methodName '(' argumentList? ')'
        |       typeName '.' Identifier '(' argumentList? ')'
        |       expressionName '.' Identifier '(' argumentList? ')'
        |       primary '.' Identifier '(' argumentList? ')'
        ;
methodInvocation_lf_primary
        :       '.' Identifier '(' argumentList? ')'
        ;

argumentList
        :       expression (',' expression)*
        ;

/*
 * Our logging statements
 */
logStatement
    :   'log' stringExpression
    |   'debug' stringExpression
    |   'warn' stringExpression
    |   'severe' stringExpression
    ;


