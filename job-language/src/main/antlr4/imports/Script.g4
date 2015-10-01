/*
 * The basic script language grammar
 *
 * Copyright (c) 2015 Peter Mount
 *
 * Due to this being based on the Java8 grammar available on github via antlr.org
 * this file is under the BSD license and not the Apache one.
 *
 * [The "BSD license"]
 *  Copyright (c) 2014 Terence Parr
 *  Copyright (c) 2014 Sam Harwell
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
    :  (  literal
    |    '(' expression ')'
    |   newObject
    ) ( methodInvocation )*
    ;

newObject
    :   'new' Identifier ('.' Identifier)* '(' argumentList? ')'
    ;

methodInvocation
    :   '.' methodName '(' argumentList? ')'
    ;

argumentList
        :       expression (',' expression)*
        ;

stringExpression
    : ( StringLiteral | expression )
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


