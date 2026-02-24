# 使用 Eclipse Temurin JDK 17 (Spring Boot 3.2.4 推荐 JDK 17，微信云托管更稳定)
FROM eclipse-temurin:17-jre-alpine

# 设置维护者标签
LABEL maintainer="yannqing <yannqing.com>"
LABEL version="1.0"
LABEL description="cni-alumni"

# 设置时区为上海 (Alpine 需要安装 tzdata)
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata ca-certificates && \
    update-ca-certificates && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# 设置工作目录
WORKDIR /app

# 创建日志目录
RUN mkdir -p /app/logs

# 创建一个挂载点
VOLUME /app/logs

# 复制应用程序
COPY ./app.jar /app/app.jar

# 暴露端口（微信云托管会自动映射）
EXPOSE 8080

# 设置 Java 启动参数（优化内存和性能）
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom"

# 健康检查（微信云托管可以利用这个）
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 启动命令（使用非 root 用户运行更安全）
RUN addgroup -g 1000 appuser && adduser -D -u 1000 -G appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

# 启动应用
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=prod"]