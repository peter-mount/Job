/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.transform.json;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

/**
 * Wrapper around {@link JsonArrayBuilder} to interface it to the language
 * <p>
 * @author peter
 */
public class JsonArrayBuilderWrapper
{

    private final JsonArrayBuilder builder;

    public JsonArrayBuilderWrapper( JsonArrayBuilder b )
    {
        this.builder = b;
    }

    JsonArrayBuilder getBuilder()
    {
        return builder;
    }

    public JsonArrayBuilderWrapper addNull()
    {
        builder.addNull();
        return this;
    }

    public JsonArrayBuilderWrapper add( Object value )
    {
        JsonUtil.add( builder, value );
        return this;
    }

    public JsonArray build()
    {
        return builder.build();
    }
}
