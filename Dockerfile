FROM java:8-alpine
# RUN apk add --no-cache postgresql-client
WORKDIR /app
COPY target/uberjar/*-standalone.jar .
# COPY wait.sh .
# CMD java -jar *-standalone.jar
EXPOSE 3000
