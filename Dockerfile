# 多阶段构建 - 秒杀系统后端服务
FROM maven:3.8-eclipse-temurin-11 AS builder

WORKDIR /app

# 复制 pom 文件
COPY pom.xml .
COPY seckill-common/pom.xml seckill-common/
COPY seckill-user-service/pom.xml seckill-user-service/
COPY seckill-product-service/pom.xml seckill-product-service/

# 下载依赖（利用 Docker 缓存）
RUN mvn dependency:go-offline -B

# 复制源码并构建
COPY seckill-common seckill-common/
COPY seckill-user-service seckill-user-service/
COPY seckill-product-service seckill-product-service/

# 构建指定模块（可通过 ARG 指定）
ARG BUILD_MODULE=seckill-user-service
RUN mvn clean package -pl ${BUILD_MODULE} -am -DskipTests -B

# 运行阶段
FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

# 创建非 root 用户
RUN adduser -D -s /bin/sh appuser

# 从构建阶段复制 jar
ARG BUILD_MODULE=seckill-user-service
COPY --from=builder /app/${BUILD_MODULE}/target/*.jar app.jar

USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
