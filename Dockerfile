# 多階段 Dockerfile：第一階段在容器內用 Gradle 建置 jar，第二階段使用輕量 JRE 執行
FROM gradle:7.6-jdk17 AS builder

# 設定工作目錄
WORKDIR /home/gradle/project

# 使用 gradle 用戶將專案檔案複製到容器，避免權限問題
COPY --chown=gradle:gradle . .

# 在容器內執行 Gradle 建置可執行的 Spring Boot jar
# 這裡預設略過測試以加速建置；如需執行測試請移除 -x test
RUN gradle bootJar -x test --no-daemon

# 第二階段：使用較小的 JRE 映像來執行應用
FROM eclipse-temurin:17-jre

# 建立應用程式目錄
WORKDIR /app

# 從 builder 階段複製剛才建置出的 jar（匹配 build/libs 下的第一個 jar）
COPY --from=builder /home/gradle/project/build/libs/*.jar /app/app.jar

# 開放預設的 Spring Boot HTTP 連接埠
EXPOSE 8080

# 以可執行 jar 啟動應用
ENTRYPOINT ["java","-jar","/app/app.jar"]
