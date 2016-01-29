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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.lang.expr.ExpressionOperation;

/**
 *
 * @author peter
 */
public class ProcessOps
{

    static List<String> invoke( ExpressionOperation ops[], Scope s )
            throws Exception
    {
        List<String> l = new ArrayList<>( ops.length );
        for( ExpressionOperation op : ops )
        {
            l.add( op.getString( s ) );
        }
        return l;
    }

    public static ExpressionOperation execute( ExpressionOperation args[] )
    {
        return ( s, a )
                -> 
                {
                    Logger log = s.getLogger();

                    Process p = new ProcessBuilder( invoke( args, s ) )
                            .start();
                    try( ReaderLogger stdin = ReaderLogger.log( p.getInputStream(), log::info );
                            ReaderLogger stdout = ReaderLogger.log( p.getErrorStream(), log::severe ) )
                    {
                        return p.waitFor();
                    }
        };
    }

}
