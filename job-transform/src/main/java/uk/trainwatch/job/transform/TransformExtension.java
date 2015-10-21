/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.transform;

import java.util.Objects;
import javax.json.Json;
import org.kohsuke.MetaInfServices;
import uk.trainwatch.job.ext.Extension;
import static uk.trainwatch.job.lang.expr.Arithmetic.decode;
import uk.trainwatch.job.lang.expr.ExpressionOperation;
import uk.trainwatch.job.transform.json.JsonArrayBuilderWrapper;
import uk.trainwatch.job.transform.json.JsonObjectBuilderWrapper;
import uk.trainwatch.job.transform.json.JsonUtil;

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
        if( exp == null || exp.length == 0 ) {
            switch( type ) {
                case "JsonArrayBuilder":
                    return ( s, a ) -> new JsonArrayBuilderWrapper( Json.createArrayBuilder() );

                case "JsonObjectBuilder":
                    return ( s, a ) -> new JsonObjectBuilderWrapper( Json.createObjectBuilder() );

                default:
                    return null;
            }
        }
        return null;
    }

    @Override
    public ExpressionOperation getExpression( String name, ExpressionOperation... argv )
    {
        int argc = argv == null ? 0 : argv.length;

        switch( argc ) {
            case 1: {
                ExpressionOperation arg0 = argv[0];
                switch( name ) {
                    case "fromJson":
                        return ( s, a ) -> JsonUtil.fromJson( decode( arg0.invoke( s, a ) ) );
                    default:
                        return null;
                }
            }
            default:
                return null;
        }
    }

}
