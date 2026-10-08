FROM eclipse-temurin:17-jre-alpine

EXPOSE 8080

ENV APP_HOME=/usr/src/app

WORKDIR $APP_HOME

COPY target/*.jar $APP_HOME/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
