/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.table;

import java.util.Objects;
import org.kohsuke.MetaInfServices;
import uk.trainwatch.job.ext.Extension;
import uk.trainwatch.job.lang.expr.ExpressionOperation;

/**
 * Implements the table functionality used mainly in report generation
 * <p>
 * @author peter
 */
@MetaInfServices(Extension.class)
public class TableExtension
        implements Extension
{

    @Override
    public String getName()
    {
        return "Table";
    }

    @Override
    public String getVersion()
    {
        return "1.0";
    }

    @Override
    public ExpressionOperation getExpression( String name, ExpressionOperation... args )
    {
        switch( name ) {
            case "table":
                if( args == null || args.length == 0 ) {
                    return ( s, a ) -> new Table();
                }
                else {
                    throw new UnsupportedOperationException( "Syntax: table()" );
                }

            case "stringFormat":
                switch( args.length ) {
                    case 1:
                        return ( s, a ) -> {
                            Object p0 = args[0].invoke( s );
                            return new TableStringFormat()
                                    .setMaxLength( getInt( p0 ) )
                                    .setAlignment( Alignment.get( p0 ) );
                        };
                    case 2:
                        return ( s, a ) -> new TableStringFormat()
                                .setAlignment( Alignment.get( args[0].invoke( s ) ) )
                                .setMaxLength( getInt( args[1].invoke( s ) ) );
                    default:
                        return null;
                }

            case "numberFormat":
                switch( args.length ) {
                    case 1:
                        return ( s, a ) -> {
                            Object p0 = args[0].invoke( s );
                            return new TableDecimalFormat()
                                    .setFormat( Objects.toString( args[0].invoke( s ) ) );
                        };
                    case 2:
                        return ( s, a ) -> {
                            Object p0 = args[1].invoke( s );
                            return new TableDecimalFormat()
                                    .setFormat( Objects.toString( args[0].invoke( s ) ) )
                                    .setAlignment( Alignment.get( p0 ) )
                                    .setMaxLength( getInt( p0 ) );
                        };
                    case 3:
                        return ( s, a ) -> new TableDecimalFormat()
                                .setFormat( Objects.toString( args[0].invoke( s ) ) )
                                .setAlignment( Alignment.get( args[1].invoke( s ) ) )
                                .setMaxLength( getInt( args[2].invoke( s ) ) );
                    default:
                        return null;
                }

            default:
                return null;
        }
    }

    private static int getInt( Object p0 )
    {
        return Math.max( 0, p0 instanceof Number ? ((Number) p0).intValue() : 0 );
    }

    private static TableStringFormat getFormat( Object p0 )
            throws Exception
    {
        String s0 = Objects.toString( p0 );
        Alignment al = Alignment.get( s0 );
        if( al == null ) {
            return new TableDecimalFormat().setFormat( s0 );
        }
        return new TableStringFormat().setAlignment( al );
    }

    @Override
    public ExpressionOperation getExpression( ExpressionOperation src, String name, ExpressionOperation... args )
    {
        return null;
    }

}
