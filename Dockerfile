FROM java:8

ADD server/build/libs/server-1.0.0.jar server.jar

ENTRYPOINT java -jar server.jar

EXPOSE 8080