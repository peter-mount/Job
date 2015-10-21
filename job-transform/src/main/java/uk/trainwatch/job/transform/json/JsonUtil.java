/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.transform.json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import static uk.trainwatch.job.lang.expr.Arithmetic.decode;

/**
 *
 * @author peter
 */
public class JsonUtil
{

    /**
     * Add a value to a JsonObjectBuilder. This method will ensure that the correct method in the underlying builder is called for the supplied object. For
     * objects that are an array, collecton or map then those objects are transformed into an appropriate array or object and inserted as such, nesting as
     * necessary.
     * <p>
     * @param builder JsonObjectBuilder
     * @param name    value name
     * @param value   value to add
     */
    public static void add( JsonObjectBuilder builder, String name, Object value )
    {
        Object v = decode( value );
        if( v == null ) {
            builder.addNull( name );
        }
        else if( v instanceof String ) {
            builder.add( name, (String) v );
        }
        else if( v instanceof Number ) {
            Number n = (Number) v;
            if( n instanceof Integer ) {
                builder.add( name, n.intValue() );
            }
            else if( n instanceof Long ) {
                builder.add( name, n.longValue() );
            }
            else if( n instanceof Double || n instanceof Float ) {
                builder.add( name, n.doubleValue() );
            }
            else if( n instanceof Short ) {
                builder.add( name, n.shortValue() );
            }
            else if( n instanceof BigInteger ) {
                builder.add( name, (BigInteger) n );
            }
            else if( n instanceof BigDecimal ) {
                builder.add( name, (BigDecimal) n );
            }
            else {
                builder.add( name, n.toString() );
            }
        }
        else if( v instanceof Boolean ) {
            builder.add( name, (Boolean) v );
        }
        else if( v instanceof JsonObjectBuilderWrapper ) {
            builder.add( name, ((JsonObjectBuilderWrapper) v).getBuilder() );
        }
        else if( v instanceof JsonArrayBuilderWrapper ) {
            builder.add( name, ((JsonArrayBuilderWrapper) v).getBuilder() );
        }
        else if( v instanceof JsonObjectBuilder ) {
            builder.add( name, (JsonObjectBuilder) v );
        }
        else if( v instanceof JsonArrayBuilder ) {
            builder.add( name, (JsonArrayBuilder) v );
        }
        else if( v instanceof Map ) {
            builder.add( name, toObjectBuilder( (Map<String, Object>) v ) );
        }
        else if( v instanceof Iterable ) {
            builder.add( name, toArrayBuilder( (Iterable<Object>) v ) );
        }
        else if( v.getClass().isArray() ) {
            builder.add( name, toArrayBuilder( (Object[]) v ) );
        }
        else {
            builder.add( name, v.toString() );
        }
    }

    /**
     * Add a value to a JsonArrayBuilder. This method will ensure that the correct method in the underlying builder is called for the supplied object. For
     * objects that are an array, collecton or map then those objects are transformed into an appropriate array or object and inserted as such, nesting as
     * necessary.
     * <p>
     * @param builder JsonArrayBuilder
     * @param value   value to add
     */
    public static void add( JsonArrayBuilder builder, Object value )
    {
        Object v = decode( value );
        if( v == null ) {
            builder.addNull();
        }
        else if( v instanceof String ) {
            builder.add( (String) v );
        }
        else if( v instanceof Number ) {
            Number n = (Number) v;
            if( n instanceof Integer ) {
                builder.add( n.intValue() );
            }
            else if( n instanceof Long ) {
                builder.add( n.longValue() );
            }
            else if( n instanceof Double || n instanceof Float ) {
                builder.add( n.doubleValue() );
            }
            else if( n instanceof Short ) {
                builder.add( n.shortValue() );
            }
            else if( n instanceof BigInteger ) {
                builder.add( (BigInteger) n );
            }
            else if( n instanceof BigDecimal ) {
                builder.add( (BigDecimal) n );
            }
            else {
                builder.add( n.toString() );
            }
        }
        else if( v instanceof Boolean ) {
            builder.add( (Boolean) v );
        }
        else if( v instanceof JsonObjectBuilderWrapper ) {
            builder.add( ((JsonObjectBuilderWrapper) v).getBuilder() );
        }
        else if( v instanceof JsonArrayBuilderWrapper ) {
            builder.add( ((JsonArrayBuilderWrapper) v).getBuilder() );
        }
        else if( v instanceof JsonObjectBuilder ) {
            builder.add( (JsonObjectBuilder) v );
        }
        else if( v instanceof JsonArrayBuilder ) {
            builder.add( (JsonArrayBuilder) v );
        }
        else if( v instanceof Map ) {
            builder.add( toObjectBuilder( (Map<String, Object>) v ) );
        }
        else if( v instanceof Iterable ) {
            builder.add( toArrayBuilder( (Iterable<Object>) v ) );
        }
        else if( v.getClass().isArray() ) {
            builder.add( toArrayBuilder( (Object[]) v ) );
        }
        else {
            builder.add( v.toString() );
        }
    }

    /**
     * Convert a Map into a JsonObjectBuilder. All elements are parsed to ensure they are correctly converted into json.
     * <p>
     * @param map Map to convert
     * <p>
     * @return JsonObjectBuilder
     */
    public static JsonObjectBuilder toObjectBuilder( Map<String, Object> map )
    {
        JsonObjectBuilder b = Json.createObjectBuilder();
        map.forEach( ( k, v ) -> add( b, k, v ) );
        return b;
    }

    /**
     * Convert an array into a JsonArrayBuilder. All elements are parsed to ensure they are correctly converted into json.
     * <p>
     * @param a array to convert
     * <p>
     * @return JsonObjectBuilder
     */
    public static JsonArrayBuilder toArrayBuilder( Object... a )
    {
        JsonArrayBuilder b = Json.createArrayBuilder();
        for( Object v: a ) {
            add( b, v );
        }
        return b;
    }

    /**
     * Convert an Iterable (i.e. Collection) into a JsonArrayBuilder. All elements are parsed to ensure they are correctly converted into json.
     * <p>
     * @param it Iterable to convert
     * <p>
     * @return JsonObjectBuilder
     */
    public static JsonArrayBuilder toArrayBuilder( Iterable<Object> it )
    {
        JsonArrayBuilder b = Json.createArrayBuilder();
        it.forEach( v -> add( b, v ) );
        return b;
    }

    public static JsonStructure fromJson( Object o )
            throws IOException
    {
        if( o == null ) {
            return null;
        }

        if( o instanceof Reader ) {
            return parse( (Reader) o );
        }

        if( o instanceof InputStream ) {
            try( Reader r = new InputStreamReader( (InputStream) o ) ) {
                return parse( r );
            }
        }

        if( o instanceof File ) {
            try( Reader r = new FileReader( (File) o ) ) {
                return parse( r );
            }
        }

        if( o instanceof Path ) {
            try( Stream<String> s = Files.lines( (Path) o ) ) {
                return parse( new StringReader( s.collect( Collectors.joining( "\n" ) ) ) );
            }
        }

        String s = Objects.toString( o, "" );
        return s.isEmpty() ? null : parse( new StringReader( s ) );
    }

    private static JsonStructure parse( Reader r )
    {
        try( JsonReader jr = Json.createReader( r ) ) {
            return jr.read();
        }
    }
}
