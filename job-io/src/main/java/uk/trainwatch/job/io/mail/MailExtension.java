/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.io.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import javax.mail.Session;
import org.kohsuke.MetaInfServices;
import uk.trainwatch.job.ext.Extension;
import uk.trainwatch.job.lang.Statement;
import static uk.trainwatch.job.lang.expr.Arithmetic.decode;
import uk.trainwatch.job.lang.expr.ExpressionOperation;
import uk.trainwatch.util.config.Configuration;
import uk.trainwatch.util.config.ConfigurationService;

/**
 * Extension that supports mailing output to a user
 * <p>
 * @author peter
 */
@MetaInfServices(Extension.class)
public class MailExtension
        implements Extension
{

    @Override
    public String getName()
    {
        return "Mailer";
    }

    @Override
    public String getVersion()
    {
        return "1.0";
    }

    private Session getSession()
    {
        Configuration config = ConfigurationService.getInstance().getConfiguration( "mailer" );
        Properties mailProps = new Properties();

        mailProps.put( "mail.transport.protocol", "smtp" );
        mailProps.put( "mail.from", config.getString( "mail.from" ) );
        mailProps.put( "mail.smtp.from", config.getString( "mail.from" ) );
        mailProps.put( "mail.smtp.host", config.getString( "mail.host" ) );

        int port = config.getInt( "mail.port", -1 );
        if( port > -1 ) {
            mailProps.put( "mail.smtp.port", port );
        }

        // Use SSL/TLS?
        if( config.getBoolean( "mail.ssl", false ) ) {
            mailProps.setProperty( "mail.smtp.ssl.enable", "true" );
        }

        return Session.getDefaultInstance( mailProps );
    }

    @Override
    public Statement getOutputStatement( String name, ExpressionOperation... args )
    {
        if( "mail".equals( name ) && args != null && args.length > 0 ) {

            return ( s, a ) -> {
                String subject = args.length == 1 ? "Job result" : null;

                Collection<String> addresses = new ArrayList<>();
                for( ExpressionOperation exp: args ) {
                    Object o = decode( exp.invoke( s, a ) );
                    if( o != null ) {
                        String emailAddress = o.toString();
                        if( subject == null ) {
                            subject = emailAddress;
                        }
                        else {
                            addresses.add( emailAddress );
                        }
                    }
                }

                s.getJob().getJobOutput().addJobOutputArchiver( new MailArchiver( s.getJob(), getSession(), subject, addresses ) );
            };
        }

        return null;
    }

}
