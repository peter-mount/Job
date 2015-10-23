/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A {@link JobOutputArchiver} that generates a ZIP archive of the job output
 * <p>
 * @author peter
 */
public class ZipArchiver
        implements JobOutputArchiver
{

    private final ZipOutputStream zos;
    private final byte buffer[] = new byte[1024];

    /**
     * Create an archive of the job output
     * <p>
     * @param file File to write to
     * <p>
     * @return
     * @throws IOException on error
     */
    public static JobOutputArchiver archive( File file )
            throws IOException
    {
        return archive( new FileOutputStream( file ) );
    }

    /**
     * Create an archive of the job output
     * <p>
     * @param path    to write to
     * @param options OpenOptions
     * <p>
     * @return
     * @throws IOException on error
     */
    public static JobOutputArchiver archive( Path path, OpenOption... options )
            throws IOException
    {
        return archive( Files.newOutputStream( path, options ) );
    }

    /**
     * Create an archive of the job output to the specified OutputStream
     * <p>
     * @param os OutputStream
     * <p>
     * @return archiver
     * <p>
     * @throws IOException on failure
     */
    public static JobOutputArchiver archive( OutputStream os )
            throws IOException
    {
        try {
            return new JobOutputArchiverWrapper( new ZipArchiver( os ), os )
            {

                @Override
                public void close()
                        throws IOException
                {
                    try {
                        super.close();
                    }
                    finally {
                        os.close();
                    }
                }

            };
        }
        catch( IOException ex ) {
            os.close();
            throw ex;
        }
    }

    /**
     * Create an archive of the job output
     * <p>
     * @param os OutputStream to write to
     * <p>
     * @throws IOException on error
     */
    public ZipArchiver( OutputStream os )
            throws IOException
    {
        zos = new ZipOutputStream( new BufferedOutputStream( os ) );
    }

    @Override
    public void close()
            throws IOException
    {
        zos.close();
    }

    @Override
    public void archive( String name, File file )
            throws IOException
    {
        try( InputStream fis = new FileInputStream( file ) ) {
            ZipEntry ze = new ZipEntry( normalise( name ) );
            zos.putNextEntry( ze );

            int i = 0;
            while( (i = fis.read( buffer )) > -1 ) {
                zos.write( buffer, 0, i );
            }
        }
        catch( FileNotFoundException ex ) {
            // Ignore, we'll just not add it to the archive
        }
    }

    public static abstract class JobOutputArchiverWrapper
            implements JobOutputArchiver
    {

        private final JobOutputArchiver archiver;
        private final OutputStream os;

        public JobOutputArchiverWrapper( JobOutputArchiver archiver, OutputStream os )
        {
            this.archiver = archiver;
            this.os = os;
        }

        @Override
        public void close()
                throws IOException
        {
            try {
                archiver.close();
            }
            finally {
                os.close();
            }
        }

        public JobOutputArchiver getArchiver()
        {
            return archiver;
        }

        @Override
        public void archive( String name, File file )
                throws IOException
        {
            archiver.archive( name, file );
        }

    }
}
