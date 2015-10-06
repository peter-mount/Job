/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.ext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;
import uk.trainwatch.job.lang.Statement;

/**
 *
 * @author peter
 */
public enum ExtensionManager
{

    INSTANCE;
    private final Collection<Extension> extensions = new ArrayList<>();

    private ExtensionManager()
    {
        ServiceLoader<Extension> loader = ServiceLoader.load( Extension.class );
        Iterator<Extension> it = loader.iterator();
        while( it.hasNext() )
        {
            extensions.add( it.next() );
        }
    }

    public Statement getStatement( String name )
    {
        return extensions.stream()
                .map( e -> e.getStatement( name ) )
                .filter( Objects::nonNull )
                .findAny()
                .orElse( null );
    }
}
