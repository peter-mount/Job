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
package uk.trainwatch.job.web;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import onl.area51.job.cluster.ClusterContextListener;
import onl.area51.job.cluster.ClusterJobRetriever;
import onl.area51.job.scheduler.JobSubmissionService;
import onl.area51.job.scheduler.ScheduleExecutor;

/**
 *
 * @author peter
 */
@WebListener
public class JobListener
        extends ClusterContextListener
{

    @Inject
    private ClusterJobRetriever clusterJobRetriever;

    @Inject
    private JobSubmissionService jobSubmissionService;
    @Inject
    private ScheduleExecutor scheduleExecutor;

    @Override
    public void contextInitialized( ServletContextEvent sce )
    {
        super.contextInitialized( sce );
        clusterJobRetriever.toString();
        jobSubmissionService.toString();
        scheduleExecutor.toString();
    }

}
