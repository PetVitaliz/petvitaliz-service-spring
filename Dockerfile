FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home petvitaliz
COPY --from=build /app/target/*.jar app.jar
USER petvitaliz
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:+ExitOnOutOfMemoryError -Dspring.main.lazy-initialization=true"
EXPOSE 3000
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
