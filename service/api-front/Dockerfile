FROM openjdk:8-jre-alpine

ADD src/docker/start.sh /app/start.sh
ADD build/libs/solid-server-api-front.jar /app/app.jar

WORKDIR /app
CMD [ "/app/start.sh" ]
