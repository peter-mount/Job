/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.util;

import org.antlr.v4.runtime.tree.TerminalNode;

/**
 *
 * @author peter
 */
public class NestedString
        extends Nestable<String>
{

    @Override
    protected String newInstance()
    {
        return null;
    }

    public void set( String instance )
    {
        this.instance = instance;
    }

    public void set( TerminalNode node )
    {
        set( node == null ? null : node.getText() );
    }

    public void append( String instance )
    {
        if( this.instance == null ) {
            set( instance );
        }
        else {
            set( this.instance + "." + instance );
        }
    }

    public void append( TerminalNode node )
    {
        append( node == null ? null : node.getText() );
    }

}
