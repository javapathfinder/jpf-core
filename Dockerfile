FROM openjdk:11-jdk-slim

RUN apt-get update && apt-get install -y \
    git \
    unzip \
    bash \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /home/jpf-core

WORKDIR /home/jpf-core

RUN git config --global --add safe.directory /home/jpf-core
