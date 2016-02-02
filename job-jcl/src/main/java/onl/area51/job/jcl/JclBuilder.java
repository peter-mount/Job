package onl.area51.job.jcl;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 *
 * @author peter
 */
class JclBuilder
        extends JclBaseListener
{

    private static <T extends ParserRuleContext> void forEach( T c, Consumer<T> a )
    {
        if( c != null )
        {
            a.accept( c );
        }
    }

    private static <T extends ParserRuleContext> void forEach( Collection<T> c, Consumer<T> a )
    {
        if( c != null && !c.isEmpty() )
        {
            c.forEach( a );
        }
    }
    private String node;
    private String name;

    public String getNode()
    {
        return node;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public void enterJclScript( JclParser.JclScriptContext ctx )
    {
        // Job is mandatory
        enterJob( ctx.job() );
        forEach( ctx.jclStatement(), this::enterJclStatement );
    }

    @Override
    public void enterJclStatement( JclParser.JclStatementContext ctx )
    {
    }

    @Override
    public void enterJob( JclParser.JobContext ctx )
    {
        List<TerminalNode> l = ctx.Identifier();
        if( l.size() != 2 )
        {
            throw new IllegalArgumentException( "Missing args" );
        }
        node = l.get( 0 ).getText();
        name = l.get( 1 ).getText();
    }
}
