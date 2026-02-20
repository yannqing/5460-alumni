# 使用 Eclipse Temurin JDK 21 (支持 ARM64 架构)
FROM eclipse-temurin:21-jdk-alpine

# 设置维护者标签
LABEL maintainer="yannqing <yannqing.com>"
LABEL version="1.0"
LABEL description="cni-alumni"

# 设置时区为上海 (Alpine 需要安装 tzdata)
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# 设置工作目录
WORKDIR /yannqing/cni-alumni/java

# 创建一个挂载点
VOLUME /yannqing/cni-alumni/logs

# 复制应用程序
COPY ./app.jar /tmp/app.jar

# 暴露端口
EXPOSE 8080 9100

# 启动命令
CMD ["java", "-jar", "/tmp/app.jar", "--spring.profiles.active=prod"]