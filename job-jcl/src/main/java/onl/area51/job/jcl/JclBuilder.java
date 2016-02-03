package onl.area51.job.jcl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 *
 * @author peter
 */
class JclBuilder
        extends JclBaseListener
{

    static Jcl build( JclParser p )
    {
        JclBuilder b = new JclBuilder();
        b.enterJclScript( p.jclScript() );

        return Jcl.create( b.getNode(), b.getName(),
                           b.type,
                           b.type == JclType.SCHEDULABLE ? b.schedule.append( "</schedule>" ).toString() : "<schedule/>" );
    }

    private static <T extends ParserRuleContext> void enter( T c, Consumer<T> a )
    {
        if( c != null ) {
            a.accept( c );
        }
    }

    private static <T extends ParserRuleContext> void forEach( Collection<T> c, Consumer<T> a )
    {
        if( c != null && !c.isEmpty() ) {
            c.forEach( a );
        }
    }

    private static int getInt( TerminalNode i )
    {
        return getInt( i, 0 );
    }

    private static int getInt( TerminalNode i, int d )
    {
        return i == null || i.getText().isEmpty() ? d : Integer.parseInt( i.getText() );
    }

    private static LocalDate getDate( JclParser.DateContext ctx, Supplier<LocalDate> defaultDate )
    {
        return ctx == null ? defaultDate.get() : getDate( ctx );
    }

    private static LocalDate getDate( JclParser.DateContext ctx )
    {

        List<TerminalNode> l = ctx.INT();
        return LocalDate.of( getInt( l.get( 0 ) ), getInt( l.get( 1 ) ), getInt( l.get( 2 ) ) );
    }

    private static LocalTime getTime( JclParser.TimeContext ctx, Supplier<LocalTime> defaultTime )
    {
        return (ctx == null ? defaultTime.get() : getTime( ctx ))
                .truncatedTo( ChronoUnit.MINUTES );
    }

    private static LocalTime getTime( JclParser.TimeContext ctx )
    {
        List<TerminalNode> l = ctx.INT();
        return LocalTime.of( getInt( l.get( 0 ) ), getInt( l.get( 1 ) ) )
                .truncatedTo( ChronoUnit.MINUTES );
    }

    private static LocalDateTime getDateTime( JclParser.DateTimeContext c, Supplier<LocalDateTime> defaultDateTime )
    {
        return (c == null ? defaultDateTime.get() : LocalDateTime.of( getDate( c.date() ), getTime( c.time() ) ))
                .truncatedTo( ChronoUnit.MINUTES );
    }

    private static LocalDateTime getDateAndOrTime( JclParser.DateAndOrTimeContext c, Supplier<LocalDate> defaultDate, Supplier<LocalTime> defaultTime )
    {
        return LocalDateTime.of( getDate( c == null ? null : c.date(), defaultDate ),
                                 getTime( c == null ? null : c.time(), defaultTime ) )
                .truncatedTo( ChronoUnit.MINUTES );
    }

    private static LocalDateTime getDateOptionalTime( JclParser.DateOptionalTimeContext c, Supplier<LocalDateTime> defaultDate, Supplier<LocalTime> defaultTime )
    {
        return (c == null ? defaultDate.get() : LocalDateTime.of( getDate( c.date() ), getTime( c.time(), defaultTime ) ))
                .truncatedTo( ChronoUnit.MINUTES );
    }

    private static void append( StringBuilder b, LocalDateTime dt )
    {
        b.append( dt.toLocalDate() )
                .append( ' ' )
                .append( dt.toLocalTime() );
    }

    private String node;
    private String name;
    private JclType type = JclType.UNKNOWN;
    private final StringBuilder schedule = new StringBuilder( "<schedule>" );

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
        enter( ctx.job(), this::enterJob );
        forEach( ctx.jclStatement(), this::enterJclStatement );
    }

    @Override
    public void enterJclStatement( JclParser.JclStatementContext ctx )
    {
        enter( ctx.runAt(), this::enterRunAt );
        enter( ctx.runEvery(), this::enterRunEvery );
        enter( ctx.schedule(), this::enterSchedule );
    }

    @Override
    public void enterJob( JclParser.JobContext ctx )
    {
        List<TerminalNode> l = ctx.Identifier();
        if( l.size() != 2 ) {
            throw new IllegalArgumentException( "Missing args" );
        }
        node = l.get( 0 ).getText();
        name = l.get( 1 ).getText();

        type = ctx.JOB() != null ? JclType.EXECUTABLE
               : ctx.SUBJOB() != null ? JclType.SUBROUTINE
                 : ctx.DELETEJOB() != null ? JclType.DELETE
                   : JclType.UNKNOWN;
    }

    /**
     * Mark the job as schedulable only if we are executable
     */
    private void schedule()
    {
        if( type == JclType.EXECUTABLE || type == JclType.SCHEDULABLE ) {
            type = JclType.SCHEDULABLE;
        }
        else {
            throw new IllegalArgumentException( "Unable to schedule Jcl type " + type );
        }
    }

    @Override
    public void enterRunAt( JclParser.RunAtContext ctx )
    {
        schedule();
        schedule.append( "<once at=\"" );
        append( schedule, getDateAndOrTime( ctx.dateAndOrTime(),
                                            LocalDate::now,
                                            () -> LocalTime.now().truncatedTo( ChronoUnit.MINUTES ) ) );
        schedule.append( "\"/>" );
    }

    @Override
    public void enterRunEvery( JclParser.RunEveryContext ctx )
    {
        BiFunction<TerminalNode, String, String> c = ( t, f ) -> {
            if( t != null ) {
                int i = getInt( t );
                if( i > 0 ) {
                    return " " + i + " " + f + (i > 1 ? "s" : "");
                }
            }
            return "";
        };
        JclParser.IntervalContext ic = ctx.interval();
        schedule();
        schedule.append( "<repeat next=\"" );
        append( schedule, getDateOptionalTime( ctx.dateOptionalTime(),
                                               LocalDateTime::now,
                                               () -> LocalTime.now().truncatedTo( ChronoUnit.MINUTES ) ) );
        schedule.append( "\" step=\"" )
                .append( getInt( ic.INT(), 1 ) )
                .append( ' ' )
                .append( ic.DAY() != null ? "day" : ic.HOUR() != null ? "hour" : "minute" )
                .append( "\"/>" );
    }

}
