/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.commons;

import java.util.concurrent.TimeUnit;
import org.kohsuke.MetaInfServices;
import uk.trainwatch.job.ext.Extension;
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.expr.ExpressionOperation;

/**
 * Provides common extensions
 * <p>
 * <h3>Statements</h3>
 * <ul>
 * <li>sleep(s); - Sleep for s seconds</li>
 * </ul>
 * <p>
 * @author peter
 */
@MetaInfServices(Extension.class)
public class CommonsExtension
        implements Extension
{

    @Override
    public String getName()
    {
        return "Commons";
    }

    @Override
    public String getVersion()
    {
        return "1.0";
    }

    @Override
    public Statement getStatement( String name, ExpressionOperation... args )
    {
        final int argc = args == null ? 0 : args.length;

        switch( name ) {
            case "sleep":
                if( argc == 1 ) {
                    return ( s, a ) -> Thread.sleep( TimeUnit.MILLISECONDS.convert( ((Number) args[0].invoke( s, a )).longValue(), TimeUnit.SECONDS ) );
                }
                break;

            default:
        }

        return null;
    }

}
