/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.transform.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Wrapper around {@link JsonObjectBuilder} to interface it to the language
 * <p>
 * @author peter
 */
public class JsonObjectBuilderWrapper
{

    private final JsonObjectBuilder builder;

    public JsonObjectBuilderWrapper( JsonObjectBuilder builder )
    {
        this.builder = builder;
    }

    JsonObjectBuilder getBuilder()
    {
        return builder;
    }

    public JsonObjectBuilderWrapper addNull( String name )
    {
        builder.addNull( name );
        return this;
    }

    public JsonObjectBuilderWrapper add( String name, Object value )
    {
        JsonUtil.add( builder, name, value );
        return this;
    }

    public JsonObject build()
    {
        return builder.build();
    }

}
