# ====== Railway 入口 Dockerfile ======
# Railway 默认在仓库根目录找 Dockerfile，本项目真正的 Dockerfile 在 backend/
# 这里把 backend 目录的内容复制到根目录的构建上下文，再复用 backend/Dockerfile 构建

FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build
# 复制 backend 内的 pom.xml
COPY backend/pom.xml .
# 离线下载依赖
RUN mvn dependency:go-offline -B

# 复制 backend 源码
COPY backend/src ./src
RUN mvn package -DskipTests -B

# ====== 运行阶段 ======
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S acgspace && adduser -S acgspace -G acgspace

COPY --from=builder /build/target/acg-space-backend-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 18083

USER acgspace

# Railway 注入的 PORT 环境变量会覆盖 18083
# 数据库/Redis/MQ 等连接信息由 Railway Variables 注入
ENV PORT=18083
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
