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

/**
 *
 * @author peter
 */
public interface Constants
{

    /**
     * The system property defining the cluster name
     */
    static final String CLUSTER_NAME = "job.cluster.name";
    /**
     * The number of threads (concurrent jobs) to handle in this instance
     */
    static final String THREAD_COUNT = "job.cluster.threads";
    /**
     * The queue name prefix to receive jobs
     */
    static final String QUEUE_NAME = "job.exec.";
    /**
     * The routing key prefix used when submitting jobs
     */
    static final String ROUTING_KEY_PREFIX = "job.exec.";

    static final String SUBQUEUE_NAME = "job.sub.";
    static final String SUBROUTING_KEY_PREFIX = "job.sub.";

    static final String REPLY_TO = "replyTo";
    static final String CORR_ID = "corrId";

    static final String ARGS = "args";
    static final String RESPONSE = "response";
    static final String EXCEPTION = "exception";

    static final String CLUSTER = "cluster";
    static final String JOB = "job";
}
