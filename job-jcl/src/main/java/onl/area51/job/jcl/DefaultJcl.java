package onl.area51.job.jcl;

/**
 *
 * @author peter
 */
class DefaultJcl
        implements Jcl
{

    private final String node, name;

    DefaultJcl( String node, String name )
    {
        this.node = node;
        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getNode()
    {
        return node;
    }

}
