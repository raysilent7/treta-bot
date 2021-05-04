FROM openjdk:11

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    APPDOCKER_SLEEP=0 \
    JAVA_OPTS=""


ADD *.jar /treta-bot.jar

EXPOSE 8081
CMD java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /treta-bot.jar