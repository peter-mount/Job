/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.sql;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import uk.trainwatch.job.ext.ExtensionType;

/**
 *
 * @author peter
 */
public enum PostgreSQLType
{

    /**
     * Unsupported type
     */
    UNSUPPORTED( ExtensionType.UNSUPPORTED ),

    /**
     * Exists as a statement in the language
     */
    STATEMENT( ExtensionType.STATEMENT ),
    /**
     * Function returns a single result
     */
    SINGLE( ExtensionType.FUNCTION ),
    /**
     * Function returns a list of single results
     */
    LIST( ExtensionType.FUNCTION ),
    /**
     * Function returns a table
     */
    TABLE( ExtensionType.FUNCTION );
    private final ExtensionType type;

    private static final Map<String, PostgreSQLType> TYPES = new HashMap<>();
    private static final Map<ExtensionType, List<PostgreSQLType>> EXT_TYPES;

    static {
        for( PostgreSQLType t: values() ) {
            TYPES.put( t.toString(), t );
        }

        // Resolve the function types for each ExtensionType
        EXT_TYPES = Collections.unmodifiableMap(
                TYPES.values()
                .stream()
                .collect( Collectors.groupingBy( PostgreSQLType::getType ) )
        );
    }

    private PostgreSQLType( ExtensionType type )
    {
        this.type = type;
    }

    /**
     * Return the PostgreSQLType by string
     * <p>
     * @param s String
     * <p>
     * @return type, {@link #UNSUPPORTED} if not present
     */
    public static PostgreSQLType lookup( String s )
    {
        return s == null ? UNSUPPORTED : TYPES.getOrDefault( s.toUpperCase(), UNSUPPORTED );
    }

    /**
     * Returns an unmodifiable collection of all types that match to the extension type.
     * <p>
     * @param type type to look up
     * <p>
     * @return collection, never null
     */
    public static Collection<PostgreSQLType> getGetTypes( ExtensionType type )
    {
        return EXT_TYPES.getOrDefault( type, Collections.EMPTY_LIST );
    }

    public ExtensionType getType()
    {
        return type;
    }

}
