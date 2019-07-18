FROM hseeberger/scala-sbt:8u212_1.2.8_2.12.8 as builder
WORKDIR /tmp
COPY . /tmp
RUN ["sbt", "assembly"]

FROM openjdk:8-jre as runner
COPY --from=builder /tmp/target/scala-2.12/life-tracker-backend-temp-assembly-0.1.jar /tmp/app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "/tmp/app.jar"]