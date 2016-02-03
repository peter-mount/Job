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

import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * Utility to aid in creating the schedule XML from a Jcl
 *
 * @author peter
 */
public interface JclXmlWriter
        extends AutoCloseable
{

    void start( String tag, Consumer<JclXmlWriter> action );

    @Override
    void close();

    JclXmlWriter attr( String name, Object value );

    /**
     * Handles LocalDateTime to timestamp
     *
     * @param name
     * @param dt
     *
     * @return
     */
    default JclXmlWriter attr( String name, LocalDateTime dt )
    {
        return attr( name, dt.toLocalDate().toString() + " " + dt.toLocalTime().toString() );
    }

    static String begin( String tag, Consumer<JclXmlWriter> action )
    {
        StringBuilder b = new StringBuilder();
        class W
                implements JclXmlWriter
        {

            private final String tag;
            private boolean inner;

            public W( String tag, boolean inner )
            {
                this.tag = tag;
                this.inner = inner;
            }

            @Override
            public void start( String tag, Consumer<JclXmlWriter> action )
            {
                if( inner ) {
                    b.append( '>' );
                    inner = false;
                }
                try( W w = new W( tag, true ) ) {
                    b.append( '<' ).append( w.tag );
                    action.accept( w );
                }
            }

            @Override
            public void close()
            {
                if( inner ) {
                    b.append( "/>" );
                }
                else {
                    b.append( "</" ).append( tag ).append( '>' );
                }
            }

            @Override
            public JclXmlWriter attr( String name, Object value )
            {
                if( !inner ) {
                    throw new IllegalStateException( "Already written content" );
                }
                b.append( ' ' ).append( name ).append( "=\"" ).append( value ).append( '"' );
                return this;
            }

            @Override
            public String toString()
            {
                return b.toString();
            }

        }

        new W( tag, false ).start( tag, action );

        return b.toString();
    }
}
