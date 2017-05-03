FROM ubuntu:latest

MAINTAINER shaoxiong "emdeboasvan@gmail.com"

USER root

EXPOSE 80 8080 22000

ADD jdk-8u73-linux-x64.tar.gz ~/depend/
ADD hadoop-2.7.3.tar.gz ~/depend/
ADD zookeeper-3.4.9.tar.gz ~/depend/

ENV JAVA_HOME=~/depend/jdk1.8.0_73
ENV WEBVEINS=~/depend/webveins

RUN echo $JAVA_HOME
CMD ["ls", "-a", "-l", "~/depend"]
