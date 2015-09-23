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

    protected String getString( ParserRuleContext ctx, int index )
    {
        ParseTree t = ctx.getChild( index );
        if( t instanceof TerminalNode ) {
            // Remove " from each end
            String s = t.getText();
            if( s == null || s.isEmpty() || s.length() < 3 ) {
                return "";
            }
            return s.substring( 1, s.length() - 1 );
        }
        throw new UnsupportedOperationException( String.format( 
                "Unable to decode %s %s",
                t.getText(),
                t.getClass()
        ));
    }
}
