# ====== 后端运行镜像 ======
# 说明：原多阶段构建在 Docker 内执行 `mvn dependency:go-offline`，
# 因沙箱 Docker 构建网络解析 Maven Central 偶发 DNS 失败（Unknown host）而中断。
# 改为「宿主机已构建好 JAR → 仅 COPY 运行」，复用宿主机稳定网络与本地 Maven 缓存。
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 创建非 root 用户运行应用（安全最佳实践）
RUN addgroup -S acgspace && adduser -S acgspace -G acgspace

# 复制宿主机构建产物（backend/target/*.jar）
COPY target/*.jar app.jar

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
