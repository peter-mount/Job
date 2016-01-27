/*
 * Copyright 2016 peter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package onl.area51.job.cluster;

import com.rabbitmq.client.impl.MethodArgumentWriter;
import com.rabbitmq.client.impl.ValueWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import static onl.area51.job.cluster.Constants.*;
import uk.trainwatch.rabbitmq.Rabbit;
import uk.trainwatch.rabbitmq.RabbitMQ;

/**
 *
 * @author peter
 */
public class ClusterPublisher
        implements Consumer<Map<String, Object>>
{

    private final Rabbit rabbit;

    public ClusterPublisher( Rabbit rabbit )
    {
        this.rabbit = rabbit;
    }

    @Override
    public void accept( Map<String, Object> message )
    {
        Objects.requireNonNull( message );

        String cluster = (String) message.get( CLUSTER );
        if( cluster == null ) {
            throw new NullPointerException( "No cluster name defined" );
        }

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            MethodArgumentWriter writer = new MethodArgumentWriter( new ValueWriter( new DataOutputStream( buffer ) ) );
            writer.writeTable( message );
            writer.flush();

            rabbit.getConnection()
                    .getChannel( this )
                    .basicPublish( RabbitMQ.DEFAULT_TOPIC, ROUTING_KEY_PREFIX + cluster, null, buffer.toByteArray() );
        }
        catch( IOException ex ) {
            throw new UncheckedIOException( ex );
        }
    }
}
