// Grammar for the Job Control Language
grammar Job;

import Script,Literals;

compilationUnit
    :   jobDefinition importDeclaration* output? declare? block EOF
    ;

jobDefinition
    :   'job' StringLiteral ';'
    |   'job' StringLiteral ';' 'run' 'as' StringLiteral ';'
    ;

importDeclaration
    :   'import' typeName ';'
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
    :   declareStatement
    ;

declareStatement
    :   localVariableDeclarationStatement
    // CDI injection goes in here
    ;
