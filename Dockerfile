FROM openjdk:17-jdk
VOLUME /tmp
WORKDIR /app
COPY target/YoutubeSearch-0.0.1-SNAPSHOT.jar youtubesearch.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "youtubesearch.jar"]