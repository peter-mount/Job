// Grammar for the Job Control Language
grammar Job;

import Script,Literals;

compilationUnit
    :   jobDefinition? output? declare? block? EOF
    ;

jobDefinition
    :   'job' StringLiteral ';' //runAs?
    ;

runAs
    :   'run as' expression ';'
    ;

output
    :   'output' '{' outputStatements* '}'
    ;

outputStatements
    :   outputStatement ';'
    ;

outputStatement
    :   mailOutput
    |   logOutput
    ;

mailOutput
    :   'mail' 'to' expression
    ;

logOutput
    :   'log' expression
    ;

declare
    :   'declare' '{' declareStatements* '}'
    ;

declareStatements
    :   declareStatement ';'
    ;

declareStatement
    :   variableDeclaratorList
    ;
