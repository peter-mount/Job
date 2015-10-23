/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.table.visitors;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import uk.trainwatch.job.table.Header;
import uk.trainwatch.job.table.Row;

/**
 *
 * @author peter
 */
public class CSVTableVisitor
        implements TableVisitor
{

    private final Appendable w;
    private CSVFormat format;
    private CSVPrinter printer;

    public CSVTableVisitor( Appendable w )
    {
        this( w, CSVFormat.DEFAULT );
    }

    public CSVTableVisitor( Appendable w, CSVFormat format )
    {
        this.w = w;
        this.format = format;
    }

    @Override
    public void visit( Header h )
    {
        Collection<String> headers = h.stream()
                .map( c -> c.getValue() )
                .map( Objects::toString )
                .collect( Collectors.toList() );
        format = format.withHeader( headers.toArray( new String[headers.size()] ) );
    }

    @Override
    public void visit( Row r )
    {
        try {
            if( printer == null ) {
                printer = format.print( w );
            }

            printer.printRecord( r.stream()
                    .map( c -> c.getValue() )
                    .collect( Collectors.toList() )
            );
        }
        catch( IOException ex ) {
            throw new UncheckedIOException( ex );
        }
    }

}
