###############################################
# Build
###############################################
FROM clojure as builder
WORKDIR /usr/src/app
# Copy only project.clj to cache retrieved dependencies.
# `lein deps` will only be executed when `project.clj` is updated.
COPY project.clj /usr/src/app
RUN lein deps

COPY . /usr/src/app
RUN lein uberjar
# `lein uberjar` generates a Fat-Jar into
# `/usr/src/app/target/uberjar/emerald-server-${VERSION}-standalone.jar`

###############################################
# Create Image
###############################################
FROM java:8-alpine
WORKDIR /app
COPY --from=builder /usr/src/app/target/uberjar/*-standalone.jar .
EXPOSE 3000
