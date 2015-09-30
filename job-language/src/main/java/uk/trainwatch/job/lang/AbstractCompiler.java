/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 *
 * @author peter
 */
public abstract class AbstractCompiler
        extends JobListenerAdapter
{

    protected String getString( ParserRuleContext ctx, int index, String def )
    {
        final String s = index < ctx.getChildCount() ? getString( ctx, index ) : null;
        return s == null || s.isEmpty() ? def : s;
    }

    protected String getString( TerminalNode n )
    {
        // Remove " from each end
        String s = n.getText();
        if( s == null || s.isEmpty() || s.length() < 3 )
        {
            return "";
        }
        return s.substring( 1, s.length() - 1 );
    }

    protected String getString( ParserRuleContext ctx, int index )
    {
        ParseTree t = ctx.getChild( index );
        if( t instanceof TerminalNode )
        {
            return getString( (TerminalNode) t );
        }
        else if( t != null )
        {
            throw new UnsupportedOperationException( String.format(
                    "Unable to decode %s %s",
                    t.getText(),
                    t.getClass()
            ) );
        }
        return null;
    }

}
