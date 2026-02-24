# Use Java 17
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy all project files
COPY . .

# Build the jar
RUN ./mvnw clean package -DskipTests

# Expose port (important for Render)
EXPOSE 8080

# Start the app using Render's PORT
CMD ["sh", "-c", "java -jar target/*.jar --server.port=$PORT"]