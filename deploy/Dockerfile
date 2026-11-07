# syntax=docker/dockerfile:1
FROM node:20-alpine AS web
WORKDIR /web
COPY mall-web/package*.json ./
RUN npm config set registry https://registry.npmmirror.com && npm install
COPY mall-web/ ./
RUN npm run build

FROM node:20-alpine AS admin
WORKDIR /admin
COPY mall-admin/package*.json ./
RUN npm config set registry https://registry.npmmirror.com && npm install
COPY mall-admin/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-17 AS build
ARG MODULE
WORKDIR /build
COPY . .
COPY --from=web /web/dist /build/mall-web/dist
COPY --from=admin /admin/dist /build/mall-admin/dist
RUN mvn -s maven-settings.xml -B -DskipTests -pl ${MODULE} -am package

FROM eclipse-temurin:17-jre
WORKDIR /app
ARG MODULE
COPY --from=build /build/${MODULE}/target/${MODULE}-1.0.0.jar /app/app.jar
ENV JAVA_OPTS="-Xms128m -Xmx256m -XX:MaxMetaspaceSize=128m -Duser.timezone=Asia/Shanghai"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
