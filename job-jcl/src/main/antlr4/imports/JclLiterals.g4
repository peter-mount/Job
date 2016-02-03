/*
 * Main lexer grammar for the Job Control Language
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
lexer grammar JclLiterals;

// LEXER

PREFIX : '##';

JOB         : 'job';
SUBJOB      : 'sub' WS? 'job';
DELETEJOB   : 'delete' WS? 'job' ;

RETRY       : 'retry' ;
MAXIMUM     : 'maximum' ;
TIMES       : 'times' ;
ONCE        : 'once' ;

RUN         : 'run';
AT          : 'at';
EVERY       : 'every';
BETWEEN     : 'between';
AND         : 'and';

SCHEDULE : 'schedule';
CRON : 'cron' ;

INT : DIGIT+ ;
fragment DIGIT : [0-9] ;

// Intervals
MINUTE : 'minute' 's'? ;
HOUR : 'hour' 's'? ;
DAY : 'day' 's'? ;

FROM : 'from' ;

// §3.10.3 Boolean Literals

BooleanLiteral
	:	'true'
	|	'false'
	;

// §3.10.5 String Literals
StringLiteral : '"' StringCharacters? '"' ;
fragment StringCharacters : StringCharacter+ ;
fragment StringCharacter : ~["] ;

// §3.11 Separators

DATESEP : HYPHEN | SLASH ;
HYPHEN : '-';
SLASH  : '/';
COLON  : ':';
LPAREN : '(';
RPAREN : ')';
LBRACE : '{';
RBRACE : '}';
LBRACK : '[';
RBRACK : ']';
SEMI   : ';';
COMMA  : ',';
DOT    : '.';

// §3.8 Identifiers (must appear after all keywords in the grammar)

Identifier
	:	JavaLetter JavaLetterOrDigit*
	;

fragment
JavaLetter
	:	[a-zA-Z$_] // these are the "java letters" below 0xFF
	|	// covers all characters above 0xFF which are not a surrogate
		~[\u0000-\u00FF\uD800-\uDBFF]
		{Character.isJavaIdentifierStart(_input.LA(-1))}?
	|	// covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
		[\uD800-\uDBFF] [\uDC00-\uDFFF]
		{Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
	;

fragment
JavaLetterOrDigit
	:	[a-zA-Z0-9$_] // these are the "java letters or digits" below 0xFF
	|	// covers all characters above 0xFF which are not a surrogate
		~[\u0000-\u00FF\uD800-\uDBFF]
		{Character.isJavaIdentifierPart(_input.LA(-1))}?
	|	// covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
		[\uD800-\uDBFF] [\uDC00-\uDFFF]
		{Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
	;

// End of statement
EOS :  [\r\n]+
    ;

//
// Whitespace and comments
//

WS  :  [ \t\u000C]+ -> skip
    ;

COMMENT
    :   '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    :   ('//' | '#' ~['#'] ) ~[\r\n]* -> skip
    ;

// A shell script header is ignored
SCRIPT_HEADER
    :   '#!' ~[\r\n]* -> skip
    ;
