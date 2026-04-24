FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN javac -cp ".:lib/mysql-connector-j-9.7.0.jar" *.java

EXPOSE 8080

CMD ["sh", "-c", "java -cp .:lib/mysql-connector-j-9.7.0.jar WebServer"]
