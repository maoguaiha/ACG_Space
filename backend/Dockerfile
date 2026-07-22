# ====== 多阶段构建 Dockerfile ======
# 阶段1: Maven 构建
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build
# 先复制 pom.xml 利用 Docker 缓存层加速依赖下载
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并构建
COPY src ./src
RUN mvn package -DskipTests -B

# 阶段2: 运行镜像（只包含 JRE，体积更小）
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 创建非 root 用户运行应用（安全最佳实践）
RUN addgroup -S acgspace && adduser -S acgspace -G acgspace

# 复制构建产物
COPY --from=builder /build/target/acg-space-backend-1.0.0-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 18083

# 非 root 用户运行
USER acgspace

# JVM 参数优化
# -Xms256m -Xmx512m: 堆内存
# -XX:+UseG1GC: G1 垃圾回收器(适合大堆)
# -Djava.security.egd=file:/dev/./urandom: 加快随机数生成（避免阻塞）
ENTRYPOINT ["java", \
    "-Xms256m", "-Xmx512m", \
    "-XX:+UseG1GC", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-DDB_HOST=mysql", \
    "-DREDIS_HOST=redis", \
    "-DROCKETMQ_NAMESRV=rmqnamesrv:9876", \
    "-jar", "app.jar"]
