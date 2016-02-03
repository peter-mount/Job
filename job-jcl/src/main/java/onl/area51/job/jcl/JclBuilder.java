package onl.area51.job.jcl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.IntConsumer;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 *
 * @author peter
 */
class JclBuilder
        extends AbstractJclBuilder
{

    static Jcl build( JclParser p )
    {
        JclBuilder b = new JclBuilder();

        // Parse the job. Note: the outer xml tag is meaningless here
        String xml = JclXmlWriter.begin( "schedule", w -> {
                                     b.parse( p.jclScript(), w );
                                 } );

        return Jcl.create( b.getNode(), b.getName(), b.type, xml );
    }

    private String node;
    private String name;
    private JclType type = JclType.UNKNOWN;

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
        enter( ctx.runCron(), this::enterRunCron );
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

    /**
     * Run a job once at a a datetime/time with optional retry
     *
     * @param ctx
     */
    @Override
    public void enterRunAt( JclParser.RunAtContext ctx )
    {
        schedule();
        start( "once", () -> {
           attr( "at", getDateAndOrTime( ctx.dateAndOrTime(),
                                         LocalDate::now,
                                         () -> LocalTime.now().truncatedTo( ChronoUnit.MINUTES ) ) );
           retry( ctx.retry() );
       } );
    }

    /**
     * Run a job at regular intervals
     *
     * @param ctx
     */
    @Override
    public void enterRunEvery( JclParser.RunEveryContext ctx )
    {
        schedule();
        between( "repeat", ctx.between(), () -> {
             LocalDateTime dt;
             if( ctx.dateOptionalTime() != null ) {
                 dt = getDateOptionalTime( ctx.dateOptionalTime(),
                                           LocalDateTime::now,
                                           () -> LocalTime.now().truncatedTo( ChronoUnit.MINUTES ) );
             }
             else if( ctx.time() != null ) {
                 // AT time. If time is before now then make certain the next time occurs tomorrow
                 dt = LocalDateTime.of( LocalDate.now(), getTime( ctx.time() ) );
                 if( dt.isBefore( LocalDateTime.now() ) ) {
                     dt = dt.plusDays( 1 );
                 }
             }
             else {
                 dt = LocalDateTime.now();
             }
             attr( "next", dt.truncatedTo( ChronoUnit.MINUTES ) );

             interval( "step", ctx.interval() );

             retry( ctx.retry() );
         } );
    }

    @Override
    public void enterRunCron( JclParser.RunCronContext ctx )
    {
        schedule();

        // 0=m, 1=h, 2=dom, 3=mon, 4=dow
        List<JclParser.CronEntryContext> entries = ctx.scheduleCronTab().cronEntry();

        forCron( entries.get( 0 ), 0, 59,
                 m -> forCron( entries.get( 1 ), 0, 59,
                               h -> forCron( entries.get( 2 ), 0, 59,
                                             dom -> forCron( entries.get( 3 ), 0, 31,
                                                             mon -> forCron( entries.get( 4 ), 0, 6,
                                                                             dow -> between( "cron", ctx.between(), () -> {
                                                                                         cronAttr( "m", m );
                                                                                         cronAttr( "h", h );
                                                                                         cronAttr( "d", dom );
                                                                                         cronAttr( "m", mon );
                                                                                         cronAttr( "w", dow );
                                                                                         retry( ctx.retry() );
                                                                                     } )
                                                             )
                                             )
                               )
                 )
        );
    }

    /**
     * Run through a cron entry
     *
     * @param ctx  context
     * @param min  min valid value
     * @param max  max valid value
     * @param each consumer to run. when value is -1 then its wild card
     */
    private void forCron( JclParser.CronEntryContext ctx, int min, int max, IntConsumer each )
    {
        if( ctx.STAR() != null ) {
            each.accept( -1 );
        }
        else {
            // For now INT(-INT)?(/INT)? doesn't work so allow just a single entry
            int s = Math.max( min, Math.min( max, getInt( ctx.INT() ) ) );
//            int s = Math.max( min, Math.min( max, getInt( ctx.INT( 0 ) )));
//            int e = Math.min( max, getInt( ctx.INT( 1 ), -1 ) ));
//            int j = Math.max( 1, Math.min( max, getInt( ctx.INT( 2 ), 1 ) ) );
//            if( e > s ) {
//                // range with optional step
//                for( int i = s; i <= e; i += j ) {
//                    each.accept( i );
//                }
//            }
//            else if( j > 1 ) {
//                // Step from stat up to max
//                for( int i = s; i <= max; i += j ) {
//                    each.accept( i );
//                }
//            }
//            else {
                // Just a single value
                each.accept( s );
//            }
        }
    }

    protected final void cronAttr( String name, int value )
    {
        if( value > -1 ) {
            attr( name, value );
        }
    }

}
