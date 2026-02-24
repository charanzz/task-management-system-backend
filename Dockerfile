FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

# Give permission to mvnw
RUN chmod +x mvnw

# Build jar
RUN ./mvnw clean package -DskipTests

# IMPORTANT: Do NOT hardcode port
CMD ["sh", "-c", "java -jar target/*.jar --server.port=$PORT"]