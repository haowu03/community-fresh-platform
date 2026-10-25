# ================= 构建阶段 =================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# 先拷贝 pom 拉取依赖，充分利用 Docker 层缓存
COPY pom.xml .
RUN mvn -B dependency:go-offline

# 拷贝源码并打包（默认执行单元测试，全绿才产出镜像）
COPY src ./src
RUN mvn -B clean package

# ================= 运行阶段 =================
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app
COPY --from=build /build/target/fresh-server-1.0.0.jar app.jar

# 商品图片上传目录（compose 中挂载宿主机 ./upload）
VOLUME /app/upload
EXPOSE 8080

# 数据库/Redis 地址由 compose 环境变量覆盖（application.yml 默认连接本机）
ENTRYPOINT ["java", "-jar", "app.jar"]