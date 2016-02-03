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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.antlr.v4.runtime.ANTLRInputStream;
import uk.trainwatch.io.IOFunction;
import uk.trainwatch.util.StateEngine;

/**
 * An instance which contains both a Jcl &amp; a script.
 * <p>
 * A combined script consists of:
 * <ol>
 * <li>A prefix, usually blank lines but also a line starting with #! - usually used for shell scripts on Unix/Linux systems</li>
 * <li>The Jcl</li>
 * <li>The actual script</li>
 * </ol>
 * <p>
 * The returned instance from the read() methods allows access to both the parsed Jcl and Script instances
 *
 * @author peter
 * @param <S> Type of script
 */
public interface JclScript<S>
{

    /**
     * The Jcl associated with this script
     *
     * @return
     */
    Jcl getJcl();

    /**
     * The underlying script
     *
     * @return
     */
    S getScript();

    /**
     * Read a Jcl and a string script
     *
     * @param str String containing script
     *
     * @return JclScript
     *
     * @throws IOException
     */
    static JclScript<String> read( String str )
            throws IOException
    {
        return read( str, s -> s );
    }

    /**
     * Read a Jcl and a string script
     *
     * @param r Reader containing script
     *
     * @return JclScript
     *
     * @throws IOException
     */
    static JclScript<String> read( Reader r )
            throws IOException
    {
        return read( r, s -> s );
    }

    /**
     * Read a Jcl and a script
     *
     * @param <S> Type of script
     * @param r   String containing script
     * @param s   function to compile the script
     *
     * @return JclScript
     *
     * @throws IOException
     */
    static <S> JclScript<S> read( String r, IOFunction<String, S> s )
            throws IOException
    {
        return read( new StringReader( r ), s );
    }

    /**
     * Read a Jcl and a script
     *
     * @param <S> Type of script
     * @param r   Reader
     * @param s   function to compile the script
     *
     * @return JclScript
     *
     * @throws IOException
     */
    static <S> JclScript<S> read( Reader r, IOFunction<String, S> s )
            throws IOException
    {
        if( r instanceof BufferedReader ) {
            return read( (BufferedReader) r, s );
        }
        else {
            return read( new BufferedReader( r ), s );
        }
    }

    /**
     * Read a Jcl and a script
     *
     * @param <S>            Type of script
     * @param r              BufferedReader
     * @param scriptFunction function to compile the script
     *
     * @return JclScript
     *
     * @throws IOException
     */
    static <S> JclScript<S> read( BufferedReader r, IOFunction<String, S> scriptFunction )
            throws IOException
    {
        final String SCRIPT = "SCRIPT";
        final String JCL = "JCL";
        final String PREFIX = "PREFIX";

        StringBuilder jclBuilder = new StringBuilder();
        StringBuilder scriptBuilder = new StringBuilder();

        // Initial state
        StateEngine.State<String> state = StateEngine.<String>builder()
                // Script prefix - we keep nothing here. next is JCL, fail to SCRIPT if no JCL is present
                .add( PREFIX )
                .initial()
                .next( JCL )
                .fail( SCRIPT )
                .action( ( l, s ) -> {
                    // Ignore empty lines or the Shell script prefix
                    String l1 = l.trim();

                    // Switch to jcl if we have ## but ignore ### here as thats a comment
                    if( l1.startsWith( "##" ) && !l1.startsWith( "###" ) ) {
                        jclBuilder.append( l1 ).append('\n');
                        return s.next();
                    }

                    // Blank lines or any remaining with # then ignore and stay in this state
                    if( l1.isEmpty() || l1.startsWith( "#" ) ) {
                        return s;
                    }
                    
                    // Anything else then fail to SCRIPT as there is no JCL
                    scriptBuilder.append( l ).append('\n');
                    return s.fail();
                } )
                .build()
                // JCL body. Next is SCRIPT
                .add( JCL )
                .next( SCRIPT )
                .action( ( l, s ) -> {
                    String l1 = l.trim();
                    if( l1.startsWith( "##" ) ) {
                        jclBuilder.append( l ).append('\n');
                        return s;
                    }

                    // Not JCL so terminate and start the script
                    scriptBuilder.append( l ).append('\n');
                    return s.next();
                } )
                .build()
                // Script body
                .add( SCRIPT )
                .terminal()
                .action( ( l, s ) -> {
                    scriptBuilder.append( l ).append('\n');
                    return s;
                } )
                .build()
                // Build the engine & retrieve the initial prefix state
                .build()
                .initial();

        String l = r.readLine();
        while( l != null ) {
            state = state.apply( l );
            l = r.readLine();
        }
        
        Jcl jcl = JclFactory.compileJcl( new ANTLRInputStream( jclBuilder.toString() ) );
        S script = scriptFunction.apply( scriptBuilder.toString() );

        return new JclScript<S>()
        {
            @Override
            public Jcl getJcl()
            {
                return jcl;
            }

            @Override
            public S getScript()
            {
                return script;
            }
        };
    }
}
