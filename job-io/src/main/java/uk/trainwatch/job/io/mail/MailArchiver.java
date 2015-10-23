/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.io.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
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
import javax.mail.util.ByteArrayDataSource;
import uk.trainwatch.job.Job;
import uk.trainwatch.job.JobOutputArchiver;

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

        for( String to: recipients ) {
            msg.addRecipient( Message.RecipientType.TO, new InternetAddress( to ) );
        }

        multipart = new MimeMultipart();
        msg.setContent( multipart );
    }

    @Override
    public void archiveLog( Job job, File file )
            throws IOException
    {
        try {
            StringBuilder b = new StringBuilder()
                    .append( "Please find the output of Job " ).append( job.getId() ).append( " run on " ).append( new Date() )
                    .append( ".\n\n" );

            try( BufferedReader r = new BufferedReader( new FileReader( file ) ) ) {
                r.lines().forEach( l -> b.append( l ).append( '\n' ) );
            }

            MimeBodyPart part = new MimeBodyPart();
            part.setDataHandler( new DataHandler( new ByteArrayDataSource( b.toString(), "text/plain" ) ) );
            part.setFileName( job.getId() + ".log" );
            part.setDisposition( Part.INLINE );
            multipart.addBodyPart( part );

            part = new MimeBodyPart();

            part.setDataHandler( new DataHandler( new FileDataSource( file )
            {

                @Override
                public String getContentType()
                {
                    return "text/plain";
                }

            } ) );
            part.setFileName( job.getId() + ".log" );
            part.setDisposition( Part.ATTACHMENT );
            multipart.addBodyPart( part );
        }
        catch( MessagingException ex ) {
            throw new IOException( ex );
        }
    }

    private final MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

    @Override
    public void archive( String name, File file )
            throws IOException
    {
        try {
            MimeBodyPart part = new MimeBodyPart();
            part.setDataHandler( new DataHandler( new FileDataSource( file ) ) );
            part.setFileName( name );
            part.setDisposition( Part.ATTACHMENT );
            multipart.addBodyPart( part );
        }
        catch( MessagingException ex ) {
            throw new IOException( ex );
        }
    }

    @Override
    public void close()
            throws IOException
    {
        try {
            LOG.log( Level.INFO, () -> "Sending mail to " + recipients );
            Transport.send( msg );
        }
        catch( MessagingException ex ) {
            LOG.log( Level.SEVERE, ex, () -> "Failed to send mail to " + recipients );
            throw new IOException( ex );
        }
    }
}
