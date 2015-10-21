/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.transform;

import javax.json.Json;
import org.kohsuke.MetaInfServices;
import uk.trainwatch.job.ext.Extension;
import uk.trainwatch.job.lang.expr.ExpressionOperation;
import uk.trainwatch.job.transform.json.JsonArrayBuilderWrapper;
import uk.trainwatch.job.transform.json.JsonObjectBuilderWrapper;

/**
 *
 * @author peter
 */
@MetaInfServices(Extension.class)
public class TransformExtension
        implements Extension
{

    @Override
    public String getName()
    {
        return "Transform";
    }

    @Override
    public String getVersion()
    {
        return "1.0";
    }

    @Override
    public ExpressionOperation construct( String type, ExpressionOperation... exp )
    {
        switch( type ) {
            case "JsonArrayBuilder":
                return ( s, a ) -> new JsonArrayBuilderWrapper( Json.createArrayBuilder() );

            case "JsonObjectBuilder":
                return ( s, a ) -> new JsonObjectBuilderWrapper( Json.createObjectBuilder() );

            default:
                return null;
        }
    }

}
