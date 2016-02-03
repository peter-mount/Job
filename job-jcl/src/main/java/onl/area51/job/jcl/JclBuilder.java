package onl.area51.job.jcl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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

}
