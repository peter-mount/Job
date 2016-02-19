/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.io.mail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.activation.DataHandler;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.configuration.Configuration;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.JobOutputArchiver;
import uk.trainwatch.util.config.ConfigurationService;

/**
 *
 * @author peter
 */
public class MailArchiver
        implements JobOutputArchiver
{

    private static final Logger LOG = Logger.getLogger( MailArchiver.class.getName() );

    private final Collection<String> recipients;
    private final Message msg;
    private final Multipart multipart;

    public MailArchiver( Job job, Session session, String subject, Collection<String> recipients )
            throws AddressException,
                   MessagingException
    {
        this.recipients = recipients;

        //Remove
        session.setDebugOut( System.out );

        msg = new MimeMessage( session );
        msg.setFrom();
        msg.setSubject( subject );

        for( String to : recipients )
        {
            msg.addRecipient( Message.RecipientType.TO, new InternetAddress( to ) );
        }

        multipart = new MimeMultipart();
        msg.setContent( multipart );

    }

    @Override
    public void archiveLog( Job job, Path file )
            throws IOException
    {
        try
        {
            StringBuilder txt = new StringBuilder()
                    .append( "Please find the output of Job " )
                    .append( ".\n\n" );

            StringBuilder html = new StringBuilder()
                    .append( "<p>Please find the output of Job " )
                    .append( ".</p>" );

            String log;
            try( Stream<String> s = Files.lines( file ) )
            {
                log = s.collect( Collectors.joining( "\n" ) );
            }

            if( log != null )
            {
                txt.append( log );
                html.append( "<pre>" ).append( log ).append( "</pre>" );
            }

            // Create the cover containing text and html versions of the body
            MimeMultipart bodypart = new MimeMultipart( "alternative" );

            MimeBodyPart part = new MimeBodyPart();
            part.setText( txt.toString(), "utf-8" );
            bodypart.addBodyPart( part );

            part = new MimeBodyPart();
            part.setContent( html.toString(), "text/html; charset=utf-8" );
            bodypart.addBodyPart( part );

            MimeBodyPart cover = new MimeBodyPart();
            cover.setContent( bodypart );
            multipart.addBodyPart( cover );

            // Now attach the log as an attachment
            part = new MimeBodyPart();
            part.setDataHandler( new DataHandler( new PathDataSource( file )
            {

                @Override
                public String getContentType()
                {
                    return "text/plain";
                }

            } ) );
            part.setFileName( "job.log" );
            part.setDisposition( Part.ATTACHMENT );
            multipart.addBodyPart( part );
        } catch( MessagingException ex )
        {
            throw new IOException( ex );
        }
    }

    private final MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

    @Override
    public void archive( String name, Path file )
            throws IOException
    {
        try
        {
            MimeBodyPart part = new MimeBodyPart();
            part.setDataHandler( new DataHandler( new PathDataSource( file ) ) );
            part.setFileName( name );
            part.setDisposition( Part.ATTACHMENT );
            multipart.addBodyPart( part );
        } catch( MessagingException ex )
        {
            throw new IOException( ex );
        }
    }

    @Override
    public void close()
            throws IOException
    {
        try
        {
            LOG.log( Level.INFO, () -> "Sending mail to " + recipients );

            Configuration config = ConfigurationService.getInstance().getPrivateConfiguration( "mailer" );
            String user = config.getString( "mail.user" );
            if( user != null && !user.isEmpty() )
            {
                Transport.send( msg, user, config.getString( "mail.pass" ) );
            }
            else
            {
                Transport.send( msg );
            }
        } catch( MessagingException ex )
        {
            LOG.log( Level.SEVERE, ex, () -> "Failed to send mail to " + recipients );
            throw new IOException( ex );
        }
    }
}
