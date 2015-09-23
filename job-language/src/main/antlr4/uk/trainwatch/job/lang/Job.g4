// Grammar for the Job Control Language
grammar Job;

import Script,Literals;

compilationUnit
    :   jobDefinition output? declare? block EOF
    ;

jobDefinition
    :   'job' StringLiteral ';'
    |   'job' StringLiteral ';' 'run' 'as' StringLiteral ';'
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
    :   localVariableDeclarationStatement
    // CDI injection goes in here
    ;
