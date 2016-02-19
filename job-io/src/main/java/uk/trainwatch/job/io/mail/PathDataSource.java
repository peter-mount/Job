package uk.trainwatch.job.io.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import javax.activation.DataSource;
import javax.activation.FileTypeMap;

/**
 *
 * @author peter
 */
public class PathDataSource
        implements DataSource
{

    private final Path path;

    private final FileTypeMap fileTypeMap;

    public PathDataSource( Path path )
    {
        this.path = path;
        fileTypeMap = FileTypeMap.getDefaultFileTypeMap();
    }

    @Override
    public InputStream getInputStream()
            throws IOException
    {
        return Files.newInputStream( path, StandardOpenOption.READ );
    }

    @Override
    public OutputStream getOutputStream()
            throws IOException
    {
        return Files.newOutputStream( path, StandardOpenOption.CREATE, StandardOpenOption.WRITE );
    }

    @Override
    public String getContentType()
    {
        return fileTypeMap.getContentType( getName() );
    }

    @Override
    public String getName()
    {
        return path.getFileName().toString();
    }

}
