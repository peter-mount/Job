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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author peter
 */
public class JclXmlWriterTest
{

    @Test
    public void test1()
    {
        assertEquals( "<a/>",
                      JclXmlWriter.begin( "a", a -> {
                                  } ) );
    }

    @Test
    public void test2()
    {
        assertEquals( "<a><b/></a>",
                      JclXmlWriter.begin( "a", a -> {
                                      a.start( "b", b -> {
                                       } );
                                  } ) );
    }

    @Test
    public void test2a()
    {
        assertEquals( "<a><b ab=\"v1\"/></a>",
                      JclXmlWriter.begin( "a", a -> {
                                      a.start( "b", b -> {
                                           b.attr( "ab", "v1" );
                                       } );
                                  } ) );
    }

    @Test
    public void test2b()
    {
        assertEquals( "<a aa=\"v0\"><b ab=\"v1\"/></a>",
                      JclXmlWriter.begin( "a", a -> {
                                      a.attr( "aa", "v0" );
                                      a.start( "b", b -> {
                                           b.attr( "ab", "v1" );
                                       } );
                                  } ) );
    }

}
