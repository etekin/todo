FROM maven:3.8.3-openjdk-17
COPY target/*.jar /usr/local/lib/todo.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/local/lib/todo.jar"]