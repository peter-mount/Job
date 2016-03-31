FROM area51/kernel:latest-opendata
MAINTAINER Peter Mount <peter@retep.org>

# You must define this for your application
#ENV CLUSTERNAME     undefinedClusterName

# Location of filesystem within this container. Usually fine to leave as is
ENV FILESYSTEM_BASE                 /var/lib/jobcache

ADD ${project.build.finalName}-assembly.tar /opt
