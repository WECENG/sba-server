FROM registry.tsintergy.com/tsintergy-public/openjdk:8
#FROM openjdk:8-jre
WORKDIR /app/
##申明入参

ARG PROJECT_JAR_PATH
ARG PROJECT_JAR

EXPOSE 8080
ADD ${PROJECT_JAR_PATH}/${PROJECT_JAR} /app/lib/app.war
ENV JAVA_OPTS="-Xmx1024m -Xms1024m -Xss512k -XX:MaxDirectMemorySize=256M -Dfile.encoding=UTF-8"

CMD java $JAVA_OPTS -jar /app/lib/app.war