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

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.IntSummaryStatistics;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import uk.trainwatch.rabbitmq.Rabbit;
import uk.trainwatch.scheduler.Cron;
import uk.trainwatch.util.JsonUtils;
import uk.trainwatch.util.TimeUtils;

/**
 *
 * @author peter
 */
@ApplicationScoped
public class ClusterMonitor
{

    private Map<String, IntSummaryStatistics> stats = new ConcurrentHashMap<>();

    @Inject
    private Rabbit rabbit;

    private Consumer<? super JsonStructure> publisher;
    private String hostName;

    @PostConstruct
    void start()
    {
        publisher = rabbit.publishJson( "statistics" );
        try {
            hostName = Inet6Address.getLocalHost().toString();
        }
        catch( UnknownHostException ex ) {
            hostName = "unknown";
        }
    }
    
    public void create(String stat) {
        stats.computeIfAbsent( stat, n -> new IntSummaryStatistics() );
    }

    public void increment( String stat )
    {
        accept( stat, 1 );
    }

    public void accept( String stat, int value )
    {
        stats.computeIfAbsent( stat, n -> new IntSummaryStatistics() )
                .accept( value );
        System.out.println(stats);
    }

    private static final String NAMES[] = {
        "alpha", "beta", "gamma", "delta", "eta", "zeta", "omega", "tau"
    };

    @Cron("0/5 * * * * ? *")
    public void submit()
    {
        stats.keySet()
                .forEach(
                        k -> stats.compute( k, ( k1, s ) -> {

                                        JsonObjectBuilder b = Json.createObjectBuilder()
                                                .add( "name", String.join( ".", "job", System.getenv( "CLUSTERNAME" ), "rate" ) )
                                                .add( "count", s.getCount() )
                                                .add( "min", s.getMin() )
                                                .add( "max", s.getMax() )
                                                .add( "max", s.getAverage() )
                                                .add( "max", s.getSum() )
                                                .add( "host", hostName );
                                        JsonUtils.add( b, "time", TimeUtils.getLondonDateTime() );

                                        publisher.accept( Json.createObjectBuilder()
                                                .add( "type", "statistic" )
                                                .add( "value", b
                                                )
                                                .build() );

                                        return new IntSummaryStatistics();
                                    } ) );
    }
}
