# syntax=docker/dockerfile:experimental
FROM openjdk:8-jdk-alpine as build
WORKDIR /workspace/app

COPY target target

RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM openjdk:8-jdk-alpine
RUN addgroup -S demo && adduser -S demo -G demo
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
RUN chown -R demo:demo /app
ENV SPRING_PROFILES_ACTIVE=container
USER demo
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -noverify -cp /app:/app/lib/* com.example.demo.DemoApplication ${@}"]