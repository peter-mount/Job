/*
 * Copyright 2016 peter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package onl.area51.job.jcl;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Factory that can parse a Jcl
 *
 * @author peter
 */
public class JclFactory
{

    private static JclParser parse( CharStream input )
    {

        JclLexer lexer = new JclLexer( input );
        // FIXME restore error listeners here
        //lexer.removeErrorListeners();
        //lexer.addErrorListener( errorListener );

        CommonTokenStream tokens = new CommonTokenStream( lexer );

        JclParser parser = new JclParser( tokens );
        // FIXME restore error listeners here
        //parser.removeErrorListeners();
        //parser.addErrorListener( errorListener );

        return parser;
    }

    /**
     * Compile Jcl from a string
     *
     * @param s Jcl string
     *
     * @return Jcl
     */
    public static Jcl compileJcl( String s )
    {
        return compileJcl( new ANTLRInputStream( s ) );
    }

    /**
     * Compile Jcl from a CharStream
     *
     * @param s CharStream
     *
     * @return Jcl
     */
    public static Jcl compileJcl( CharStream s )
    {
        JclParser p = parse( s );
        JclBuilder b = new JclBuilder();
        b.enterJclScript( p.jclScript() );
        return new DefaultJcl( b.getNode(), b.getName() );
    }
}
