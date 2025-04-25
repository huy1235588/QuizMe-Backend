FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Sao chép POM và tải các dependency
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B

# Sao chép mã nguồn và xây dựng ứng dụng
COPY src src
RUN ./mvnw clean package -DskipTests

# Giai đoạn 2: Tạo image chính
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Sao chép file JAR từ giai đoạn xây dựng
COPY --from=build /app/target/*.jar app.jar

# Expose cổng
EXPOSE 8080

# Tham số khởi động ứng dụng
ENTRYPOINT ["java", "-jar", "/app/app.jar"]