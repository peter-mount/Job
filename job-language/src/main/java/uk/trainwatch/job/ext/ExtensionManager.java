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
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.trainwatch.job.lang.Operation;
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

    public void init()
    {
        Iterator<Extension> it = extensions.iterator();
        while( it.hasNext() ) {
            Extension ext = it.next();
            try {
                ext.init();
            }
            catch( Exception ex ) {
                LOG.log( Level.WARNING, () -> "Removing " + ext.getName() + " " + ex.getMessage() );
                it.remove();
            }
        }
    }

    private <T extends Operation> T locate( Function<Extension, T> lookup )
    {
        return extensions.stream()
                .map( lookup )
                .filter( Objects::nonNull )
                .findAny()
                .orElse( null );
    }

    public ExpressionOperation construct( String name, ExpressionOperation... args )
    {
        return locate( e -> e.construct( name, args ) );
    }

    public Statement getStatement( String name, ExpressionOperation... args )
    {
        return locate( e -> e.getStatement( name, args ) );
    }

    public Statement getOutputStatement( String name, ExpressionOperation... args )
    {
        return locate( e -> e.getOutputStatement( name, args ) );
    }

    public ExpressionOperation getExpression( String name, ExpressionOperation... args )
    {
        return locate( e -> e.getExpression( name, args ) );
    }

    public ExpressionOperation getExpression( ExpressionOperation src, String name, ExpressionOperation... args )
    {
        return locate( e -> e.getExpression( src, name, args ) );
    }
}
