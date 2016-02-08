package onl.area51.job.jcl;

/**
 *
 * @author peter
 */
public interface Jcl
{

    /**
     * The job node
     *
     * @return
     */
    String getNode();

    /**
     * The Job name
     *
     * @return
     */
    String getName();

    /**
     * Is this job valid. The bare minimum for validity is that node and name are not null nor blank.
     *
     * @return
     */
    default boolean isValid()
    {
        String a = getNode(), b = getName();
        return a != null && !a.isEmpty() && b != null && !b.isEmpty();
    }

    /**
     * The job type
     *
     * @return
     */
    JclType getType();

    /**
     * The Job schedule.
     * <p>
     * This will be XML
     *
     * @return
     */
    String getSchedule();

    /**
     * Creates a basic Jcl
     *
     * @param node
     * @param name
     *
     * @return
     */
    static Jcl create( String node, String name )
    {
        return create( node, name, JclType.UNKNOWN );
    }

    static Jcl create( String node, String name, JclType type )
    {
        return create( node, name, type, "<schedule/>" );
    }

    static Jcl create( String node, String name, JclType type, String schedule )
    {
        String nd = node == null ? null : node.toLowerCase();
        String nm = name == null ? null : name.toLowerCase();
        return new Jcl()
        {

            @Override
            public String getName()
            {
                return nm;
            }

            @Override
            public String getNode()
            {
                return nd;
            }

            @Override
            public JclType getType()
            {
                return type;
            }

            @Override
            public String getSchedule()
            {
                return schedule;
            }

        };
    }
}
