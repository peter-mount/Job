/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.io;

import java.io.StringWriter;
import org.kohsuke.MetaInfServices;
import uk.trainwatch.job.ext.Extension;
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.expr.ExpressionOperation;

/**
 *
 * @author peter
 */
@MetaInfServices(Extension.class)
public class IOExtension
        implements Extension
{

    @Override
    public String getName()
    {
        return "IO";
    }

    @Override
    public String getVersion()
    {
        return "1.0";
    }

    @Override
    public ExpressionOperation construct( String type, ExpressionOperation... exp )
    {
        int argc = exp == null ? 0 : exp.length;
        switch( argc ) {
            case 0:
                switch( type ) {
                    case "StringWriter":
                        return ( s, a ) -> new WriterWrapper( new StringWriter() );

                    default:
                        return null;
                }

            case 1:
                switch( type ) {
                    case "File":
                        return FileOp.newFile( exp[0] );

                    case "FTP":
                        return Ftp.create( exp[0] );

                    case "gunzip":
                        return FileOp.gunzip( exp[0] );

                    case "gzip":
                        return FileOp.gzip( exp[0] );

                    case "Reader":
                        return FileOp.newReader( exp[0] );

                    case "Writer":
                        return FileOp.newWriter( exp[0] );

                    default:
                        return null;
                }

            case 2:
                switch( type ) {
                    case "File":
                        return FileOp.newFile( exp[0], exp[1] );

                    default:
                        return null;
                }

            default:
                return null;
        }
    }

    @Override
    public Statement getStatement( String name, ExpressionOperation... args )
    {
        if( "delete".equals( name ) ) {
            return FileOp.delete( args );
        }
        return null;
    }

}
