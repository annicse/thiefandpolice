# ---- ধাপ ১: build (JDK দিয়ে jar বানানো) ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# আগে শুধু pom কপি করলে dependency layer cache হয়, পরের build দ্রুত
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B -DskipTests package

# ---- ধাপ ২: run (ছোট JRE ইমেজ, শুধু jar) ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render PORT env দেয়; application.properties সেটা পড়ে
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
