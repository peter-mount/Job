/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.commons;

import java.util.Objects;
import org.kohsuke.MetaInfServices;
import uk.trainwatch.job.ext.Extension;
import uk.trainwatch.job.lang.expr.ExpressionOperation;
import uk.trainwatch.util.config.ConfigurationService;

/**
 *
 * @author peter
 */
@MetaInfServices(Extension.class)
public class ConfigurationExtension
        implements Extension
{

    @Override
    public String getName()
    {
        return "Configuration";
    }

    @Override
    public String getVersion()
    {
        return "1.0";
    }

    /**
     * Return a valid expression or null.
     * <ul>
     * <li>config("name") returns Configuration object called name.</li>
     * <li>config("name","key") returns Configuration value key.</li>
     * <li>config("name","key","default") returns Configuration value key or default if none</li>
     * </ul>
     * <p>
     * @param name
     * @param args
     *             <p>
     * @return
     */
    @Override
    public ExpressionOperation getExpression( String name, ExpressionOperation... args )
    {
        if( "config".equals( name ) ) {
            switch( args.length ) {
                case 1:
                    return ( s, a ) -> ConfigurationService.getInstance()
                            .getPrivateConfiguration( Objects.toString( args[0].invoke( s ), "" ) );
                case 2:
                    return ( s, a ) -> ConfigurationService.getInstance()
                            .getPrivateConfiguration( Objects.toString( args[0].invoke( s ), "" ) )
                            .getString( Objects.toString( args[1].invoke( s ), "" ) );
                case 3:
                    return ( s, a ) -> {
                        String v = ConfigurationService.getInstance()
                                .getPrivateConfiguration( Objects.toString( args[0].invoke( s ), "" ) )
                                .getString( Objects.toString( args[1].invoke( s ), "" ) );
                        return v == null ? args[2].invoke( s ) : v;
                    };
            }
        }

        return null;
    }

}
