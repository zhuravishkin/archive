FROM openjdk:11.0.2
MAINTAINER Zhuravishkin Alexey
WORKDIR /usr/src/myapp
COPY /target/*.jar /usr/src/myapp/app.jar
COPY id_rsa /usr/src/myapp
CMD ["java", "-jar", "/usr/src/myapp/app.jar"]
