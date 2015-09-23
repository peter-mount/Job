// Grammar for the Job Control Language
grammar Job;

import Script,Literals;
//import Identifiers,Expression,Script;

job : 'job' ID jobList* output? declare? block ;

jobList : 'run as' ID ;

output: 'output' outputList+ ;
outputList: mailOutput | logOutput ;
mailOutput : 'mail to' expression SEMI ;
logOutput : 'log' expression SEMI ;

declare : 'declare' ;
