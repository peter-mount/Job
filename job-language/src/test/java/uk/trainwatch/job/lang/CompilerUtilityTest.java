/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.lang;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Methods used by the generated CompilerTest class
 * <p>
 * @author peter
 */
public class CompilerUtilityTest
{

    /**
     * Test of getString method, of class AbstractCompiler.
     */
    @Test
    public void testGetString_WithDefault()
    {
        ParserRuleContext ctx = new ParserRuleContext();
        // job "id" ;
        ctx.addChild( new NullParseTree() );
        ctx.addChild( new TextNode( "\"id\"" ) );
        ctx.addChild( new NullParseTree() );

        AbstractCompiler instance = new AbstractCompilerImpl();

        String id = instance.getString( ctx, 1 );
        assertNotNull( id );
        assertEquals( "id", id );

        String runAs = instance.getString( ctx, 5, null );
        assertNull( runAs );

        // Now ctx becomes
        // job "id" ; run as "Test" ;
        ctx.addChild( new NullParseTree() );
        ctx.addChild( new NullParseTree() );
        ctx.addChild( new TextNode( "\"Test\"" ) );
        ctx.addChild( new NullParseTree() );
        
        runAs = instance.getString( ctx, 5, null );
        assertNotNull( runAs );
        assertEquals( "Test", runAs );
    }

    /**
     * Test of getString method, of class AbstractCompiler.
     */
    @Test
    public void testGetString()
    {
        ParserRuleContext ctx = new ParserRuleContext();
        ctx.addChild( new NullParseTree() );
        ctx.addChild( new TextNode( "\"id\"" ) );
        ctx.addChild( new NullParseTree() );

        AbstractCompiler instance = new AbstractCompilerImpl();

        String id = instance.getString( ctx, 1 );
        assertNotNull( id );
        assertEquals( "id", id );
    }

    public class AbstractCompilerImpl
            extends AbstractCompiler
    {
    }

    private static class NullParseTree
            implements TerminalNode
    {

        @Override
        public Token getSymbol()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ParseTree getParent()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ParseTree getChild( int i )
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T accept( ParseTreeVisitor<? extends T> visitor )
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getText()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toStringTree( Parser parser )
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Interval getSourceInterval()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getPayload()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getChildCount()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toStringTree()
        {
            throw new UnsupportedOperationException();
        }

    }

    private static class TextNode
            extends NullParseTree
            implements TerminalNode
    {

        private final String text;

        public TextNode( String text )
        {
            this.text = text;
        }

        @Override
        public String getText()
        {
            return text;
        }

        @Override
        public Token getSymbol()
        {
            throw new UnsupportedOperationException();
        }

    }
}
