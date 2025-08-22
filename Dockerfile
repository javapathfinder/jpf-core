FROM openjdk:11-jdk-slim

RUN apt-get update && apt-get install -y \
    git \
    unzip \
    bash \
    && rm -rf /var/lib/apt/lists/*

# Set environment variables
ENV JPF_HOME=/opt/jpf/jpf-core
ENV JAVA_OPTS="--add-exports java.base/jdk.internal.misc=ALL-UNNAMED --add-opens java.base/jdk.internal.misc=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED"


RUN mkdir -p /opt/jpf/jpf-core /opt/projects

# set working dir (default)
WORKDIR /opt/jpf/jpf-core


RUN echo "echo '>> Container ready. Mount your code via docker-compose volumes.'"

# default entrypoint: bash shell
ENTRYPOINT ["/bin/bash"]
