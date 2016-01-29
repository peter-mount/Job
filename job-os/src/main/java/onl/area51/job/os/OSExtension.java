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
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import org.kohsuke.MetaInfServices;
import uk.trainwatch.job.Scope;
import uk.trainwatch.job.ext.Extension;
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.expr.ExpressionOperation;

/**
 * Job extension that provides access to tool sets on the local operating system.
 *
 * In it's initial incarnation this extension will allow access to any tool, however future versions will have the ability to
 * restrict access to specific tools.
 *
 * @author peter
 */
@MetaInfServices( Extension.class )
public class OSExtension
        implements Extension
{

    @Override
    public String getName()
    {
        return "OS";
    }

    @Override
    public String getVersion()
    {
        return "1.0";
    }

    @Override
    public Statement getStatement( String name, ExpressionOperation... args )
    {
        if( "execute".equals( name ) && args.length > 0 )
        {
            return ( s, a ) -> ProcessOps.execute( args );
        }
        return null;
    }

    @Override
    public ExpressionOperation getExpression( String name, ExpressionOperation... args )
    {
        if( "execute".equals( name ) && args.length > 0 )
        {
            return ProcessOps.execute( args );
        }
        return null;
    }

}
