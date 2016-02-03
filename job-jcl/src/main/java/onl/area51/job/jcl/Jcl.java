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
}
