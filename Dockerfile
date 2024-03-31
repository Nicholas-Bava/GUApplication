FROM alpine:latest
RUN apk --no-cache add openjdk21
EXPOSE 8091
ADD target/devops_proj.jar devops_proj.jar
ENTRYPOINT["java", "-jar", "/devops_proj.jar"]