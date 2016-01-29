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
package onl.area51.job.os;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.function.Consumer;

/**
 * A class that will log the contents of a Reader to a Logger
 *
 * @author peter
 */
public class ReaderLogger
        implements Closeable
{

    private final BufferedReader br;
    private final Consumer<String> consumer;
    private final Thread t;
    private volatile boolean running = true;

    public static ReaderLogger log( Reader r, Consumer<String> c )
    {
        return new ReaderLogger( r instanceof BufferedReader ? (BufferedReader) r : new BufferedReader( r ), c )
                .start();
    }

    public static ReaderLogger log( InputStream is, Consumer<String> c )
    {
        return log( new InputStreamReader( is ), c );
    }

    private ReaderLogger( BufferedReader r, Consumer<String> c )
    {
        this.consumer = c;
        this.br = r;

        t = new Thread( ()
                -> 
                {
                    try
                    {
                        String l = br.readLine();
                        while( running && l != null )
                        {
                            consumer.accept( l );
                            l = br.readLine();
                        }
                    } catch( IOException ex )
                    {
                    }
        } );
    }

    private ReaderLogger start()
    {
        t.start();
        return this;
    }

    @Override
    public void close()
            throws IOException
    {
        running = false;
        t.interrupt();
        br.close();
    }

}
