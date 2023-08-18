FROM maven:3.9.3-eclipse-temurin-17-alpine as build
WORKDIR /workspace/app
COPY pom.xml .
COPY src src
RUN mvn package
FROM tomcat:jre17-temurin
COPY --from=build /workspace/app/target/ROOT.war /usr/local/tomcat/webapps/
EXPOSE 8080
CMD ["catalina.sh", "run"]