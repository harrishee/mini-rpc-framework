FROM maven:3.9.2 AS build
WORKDIR /build
COPY . /build
RUN mvn clean package -DskipTests

FROM openjdk:8-jdk-alpine
WORKDIR /app
COPY --from=build /build/start-server/target/start-server-jar-with-dependencies.jar /app/server.jar

ENV NACOS_SERVER_ADDR=127.0.0.1:8848
ENV SERVER_HOST=127.0.0.1
ENV SERVER_PORT=9999
ENV SERVER_TYPE=netty
EXPOSE 9999
CMD java -DserverType=${SERVER_TYPE} -DSERVER_HOST=${SERVER_HOST} -DSERVER_PORT=${SERVER_PORT} -jar /app/server.jar
