package onl.area51.job.jcl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 *
 * @author peter
 */
class AbstractJclBuilder
        extends JclBaseListener
{

    private JclXmlWriter xml;

    protected void parse( JclParser.JclScriptContext ctx, JclXmlWriter xml )
    {
        this.xml = xml;
        enterJclScript( ctx );
    }

    protected final void start( String name, Runnable a )
    {
        JclXmlWriter xml0 = xml;
        try {
            xml.start( name, w -> {
                   xml = w;
                   a.run();
               } );
        }
        finally {
            xml = xml0;
        }
    }

    protected final void attr( String name, Object value )
    {
        xml.attr( name, value );
    }

    protected final void attr( String name, LocalDateTime dt )
    {
        xml.attr( name, dt );
    }

    protected final <T extends ParserRuleContext> void enter( T c, Consumer<T> a )
    {
        if( c != null ) {
            a.accept( c );
        }
    }

    protected final <T extends ParserRuleContext> void forEach( Collection<T> c, Consumer<T> a )
    {
        if( c != null && !c.isEmpty() ) {
            c.forEach( a );
        }
    }

    protected final int getInt( TerminalNode i )
    {
        return getInt( i, 0 );
    }

    protected final int getInt( TerminalNode i, int d )
    {
        return i == null || i.getText().isEmpty() ? d : Integer.parseInt( i.getText() );
    }

    protected final LocalDate getDate( JclParser.DateContext ctx, Supplier<LocalDate> defaultDate )
    {
        return ctx == null ? defaultDate.get() : getDate( ctx );
    }

    protected final LocalDate getDate( JclParser.DateContext ctx )
    {
        List<TerminalNode> l = ctx.INT();
        return LocalDate.of( getInt( l.get( 0 ) ), getInt( l.get( 1 ) ), getInt( l.get( 2 ) ) );
    }

    protected final LocalTime getTime( JclParser.TimeContext ctx, Supplier<LocalTime> defaultTime )
    {
        return (ctx == null ? defaultTime.get() : getTime( ctx ))
                .truncatedTo( ChronoUnit.MINUTES );
    }

    protected final LocalTime getTime( JclParser.TimeContext ctx )
    {
        List<TerminalNode> l = ctx.INT();
        return LocalTime.of( getInt( l.get( 0 ) ), getInt( l.get( 1 ) ) )
                .truncatedTo( ChronoUnit.MINUTES );
    }

    protected final LocalDateTime getDateTime( JclParser.DateTimeContext c, Supplier<LocalDateTime> defaultDateTime )
    {
        return (c == null ? defaultDateTime.get() : LocalDateTime.of( getDate( c.date() ), getTime( c.time() ) ))
                .truncatedTo( ChronoUnit.MINUTES );
    }

    protected final LocalDateTime getDateAndOrTime( JclParser.DateAndOrTimeContext c, Supplier<LocalDate> defaultDate, Supplier<LocalTime> defaultTime )
    {
        return LocalDateTime.of( getDate( c == null ? null : c.date(), defaultDate ),
                                 getTime( c == null ? null : c.time(), defaultTime ) )
                .truncatedTo( ChronoUnit.MINUTES );
    }

    protected final LocalDateTime getDateOptionalTime( JclParser.DateOptionalTimeContext c, Supplier<LocalDateTime> defaultDate,
                                                       Supplier<LocalTime> defaultTime )
    {
        return (c == null ? defaultDate.get() : LocalDateTime.of( getDate( c.date() ), getTime( c.time(), defaultTime ) ))
                .truncatedTo( ChronoUnit.MINUTES );
    }

    /**
     * Append an IntervalContext
     *
     * @param name attribute name
     * @param ctx
     */
    protected void interval( String name, JclParser.IntervalContext ctx )
    {
        attr( name, String.format( "%d %s",
                                   getInt( ctx.INT(), 1 ),
                                   ctx.DAY() != null ? "day" : ctx.HOUR() != null ? "hour" : "minute" ) );
    }

    /**
     * If RetryContext is not null appends " retry=" and the interval within it.
     * <p>
     * e.g.
     * "retry 1 minute" generates &lt;e value="d" retry="1 minute"/&gt;
     * "retry 1 minute maximum 3" generates &lt;e value="d" retry="1 minute" max="3"/&gt;
     *
     *
     * @param b
     * @param ctx
     */
    protected void retry( JclParser.RetryContext ctx )
    {
        if( ctx != null ) {
            if( ctx.interval() != null ) {
                interval( "retry", ctx.interval() );
            }
            else if( ctx.ONCE() != null ) {
                attr( "retry", "once" );
            }

            if( ctx.MAXIMUM() != null ) {
                attr( "max", getInt( ctx.INT(), 1 ) );
            }
        }
    }

    // 23:59
    private static final LocalTime MAXTIME = LocalTime.MAX.truncatedTo( ChronoUnit.MINUTES );

    /**
     * For entries that supports between, if betweenCtx is null then just runs action.
     * Otherwise gets the start and end times. If start is before end then runs the action with the start/end attributes.
     * If start is after end (i.e. crosses midnight) then runs the action twice. Once from start to midnight, once for midnight to end.
     *
     * @param <T>
     * @param name
     * @param betweenCtx
     * @param ctx
     * @param action
     */
    protected void between( String name, JclParser.BetweenContext betweenCtx, Runnable action )
    {
        if( betweenCtx == null ) {
            start( name, action );
        }
        else {
            LocalTime start = getTime( betweenCtx.time( 0 ) );
            LocalTime end = getTime( betweenCtx.time( 1 ) );

            BiConsumer<LocalTime, LocalTime> c = ( s, e ) -> start( name, () -> {
                                                                attr( "betweenStart", s );
                                                                attr( "betweenEnd", e );
                                                                action.run();
                                                            } );

            if( start.isBefore( end ) ) {
                c.accept( start, end );
            }
            else {
                c.accept( LocalTime.MIN, end );
                c.accept( start, MAXTIME );
            }
        }
    }

}
