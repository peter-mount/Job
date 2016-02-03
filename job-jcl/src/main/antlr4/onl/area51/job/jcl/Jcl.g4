/*
 * Grammar for the JCL schedule definition section
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
grammar Jcl;

import JclLiterals;

jclScript
    :   (PREFIX job EOS (PREFIX jclStatement EOS)*)? EOS*
    ;

jclStatement
    :   runAt
    |   runEvery
    |   schedule
    ;

// job Node.Name
job
    :   JOB Identifier DOT Identifier
    ;

// run at yyyy-dd-mm hh:mm
// run at yyyy/dd/mm hh:mm
// run at yyyy-dd-mm
// run at yyyy/dd/mm
// run at hh:mm
runAt
    :   RUN AT dateAndOrTime
    ;

// run every interval
runEvery
    :   RUN EVERY interval (FROM dateOptionalTime)?
    ;

schedule
    : SCHEDULE (CRON scheduleCronTab)
    ;

// Cron m h dom mon dow
scheduleCronTab
    :   INT INT INT INT INT
    ;

// Interval
interval
    : INT? (DAY| HOUR| MINUTE)
    ;

// A date and time
dateTime : date time ;

dateOptionalTime : date time? ;

// A date and/or time
dateAndOrTime : (date time | date | time );

// A Date in y-m-d format
date : INT DATESEP INT DATESEP INT ;

// A time in h:m format
time : INT COLON INT ;
