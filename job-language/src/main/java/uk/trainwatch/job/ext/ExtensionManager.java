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
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.trainwatch.job.lang.Statement;
import uk.trainwatch.job.lang.expr.ExpressionOperation;

/**
 *
 * @author peter
 */
public enum ExtensionManager
{

    INSTANCE;
    private final Logger LOG = Logger.getLogger( ExtensionManager.class.getName() );
    private final Collection<Extension> extensions = new ArrayList<>();

    private ExtensionManager()
    {
        ServiceLoader<Extension> loader = ServiceLoader.load( Extension.class );
        Iterator<Extension> it = loader.iterator();
        while( it.hasNext() ) {
            Extension ex = it.next();
            LOG.log( Level.INFO, () -> "Registering extension " + ex.getName() + ":" + ex.getVersion() );
            extensions.add( ex );
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

    public ExpressionOperation getExpression( String name, ExpressionOperation... args )
    {
        return extensions.stream()
                .map( e -> e.getExpression( name, args ) )
                .filter( Objects::nonNull )
                .findAny()
                .orElse( null );
    }

    public ExpressionOperation getExpression( ExpressionOperation src, String name, ExpressionOperation... args )
    {
        return extensions.stream()
                .map( e -> e.getExpression( src, name, args ) )
                .filter( Objects::nonNull )
                .findAny()
                .orElse( null );
    }
}
