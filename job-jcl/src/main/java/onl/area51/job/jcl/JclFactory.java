package onl.area51.job.jcl;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 *
 * @author peter
 */
public class JclFactory
{

    private static JclParser parse( CharStream input )
    {

        JclLexer lexer = new JclLexer( input );
        // FIXME restore error listeners here
        //lexer.removeErrorListeners();
        //lexer.addErrorListener( errorListener );

        CommonTokenStream tokens = new CommonTokenStream( lexer );

        JclParser parser = new JclParser( tokens );
        // FIXME restore error listeners here
        //parser.removeErrorListeners();
        //parser.addErrorListener( errorListener );

        return parser;
    }

    public static Jcl compileJcl( ANTLRInputStream is )
    {
        JclParser p = parse( is );
        JclBuilder b = new JclBuilder();
        b.enterJclScript( p.jclScript() );
        return new DefaultJcl( b.getNode(), b.getName() );
    }
}
