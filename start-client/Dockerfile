FROM maven:3.9.2 AS build
WORKDIR /build
COPY . /build
RUN mvn clean package -DskipTests

FROM openjdk:8-jdk-alpine
WORKDIR /app
COPY --from=build /build/start-client/target/start-client-jar-with-dependencies.jar /app/client.jar

ENV NACOS_SERVER_ADDR=127.0.0.1:8848
ENV CLIENT_TYPE=netty

CMD java -DclientType=${CLIENT_TYPE} -jar /app/client.jar
