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

import java.io.IOException;
import javax.enterprise.inject.Typed;
import onl.area51.job.jcl.Jcl;

/**
 * An implementation that will retrieve a Job from some central location, a FileSystem or a Database
 *
 * @author peter
 */
@Typed(Object.class)
public interface ClusterJobRetriever
{

    /**
     * Retrieve a stored Job
     *
     * @param jcl Jcl to retrieve
     *
     * @return
     *
     * @throws IOException
     */
    String retrieveJob( Jcl jcl )
            throws IOException;

    /**
     * Stores a job
     *
     * @param jcl Jcl to store
     * @param job Full text of the job
     *
     * @throws IOException
     */
    void storeJob( Jcl jcl, String job )
            throws IOException;
}
