# Ig

簡單的 Spring Boot 專案範例 (Gradle)

## 專案簡介

這是名為 `Ig` 的 Spring Boot 專案。包含 OpenAPI (springdoc) 設定，並在本地環境預設關閉 Google Cloud Platform 自動配置以避免憑證相關錯誤。

## 主要檔案

- `src/main/java/org/inariforge/ig/IgApplication.java` - 應用程式進入點
- `src/main/java/org/inariforge/ig/config/OpenApiConfig.java` - OpenAPI / Swagger 設定
- `src/main/resources/application.properties` - 應用程式設定

## 前置需求

- JDK 11 或更新版本（專案的 Gradle / build.gradle 會指定精確版本）
- Gradle Wrapper（專案已包含 `gradlew`）

## 常用指令

在 macOS / Linux 下使用 Gradle Wrapper：

```bash
# 建置專案（包含測試）
./gradlew build

# 只跑測試
./gradlew test

# 以開發模式啟動應用（在 localhost:8080）
./gradlew bootRun
```

## 設定重點（`application.properties`）

專案預設在 `src/main/resources/application.properties` 中包含：

- `springdoc.api-docs.path=/v3/api-docs`：OpenAPI JSON 的路徑
- `springdoc.swagger-ui.path=/swagger-ui.html`：對外暴露一個較短的 Swagger UI 路徑
- `spring.cloud.gcp.enabled=false`：在本地/開發環境預設關閉 GCP 自動配置，以避免沒有 ADC 憑證時的錯誤
- `spring.autoconfigure.exclude`：排除特定 GCP auto-configuration（例如 Pub/Sub、Storage）

在部署到 GCP 或需要啟用 GCP 功能時，可透過 Profile 或環境變數覆寫這些設定。

## OpenAPI / Swagger

專案使用 springdoc-openapi 提供自動化的 OpenAPI 文件。建置並啟動應用後可在以下位置查看：

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html` 或 `http://localhost:8080/swagger-ui.html`（本專案也將短路徑 `/swagger-ui.html` 指向 UI）

## 測試

- 單元測試位於 `src/test/java`，可使用 `./gradlew test` 執行。

## Docker

這個專案提供一個簡單的 `Dockerfile`，是假設你已在本機使用 Gradle Wrapper 建置好可執行 jar（更快速且常見的工作流程）。步驟如下：

1. 在專案根目錄建置 jar（會產生於 `build/libs/`）：

```bash
# 產生可執行的 Spring Boot jar
./gradlew bootJar
# 或完整 build（含測試）
./gradlew build
```

2. 建置 Docker 映像：

```bash
# 從專案根目錄執行
docker build -t ig-app:latest .
```

3. 執行容器（將本機 8080 對應到容器的 8080）：

```bash
docker run --rm -p 8080:8080 ig-app:latest
```

小提示：如果你想在 Docker 裡面執行完整的 build（CI 風格），我也可以還原 multi-stage 的 `Dockerfile`，讓容器內執行 `./gradlew bootJar`。

## Docker（進階）

本專案提供兩個 Dockerfile，分別適用於開發與正式環境：

- `Dockerfile.dev`：開發/測試用，multi-stage，在容器內使用 Gradle 建置 jar，並預設啟用 `dev` profile（OpenAPI/Swagger 啟用）。
- `Dockerfile.prod`：正式用，假設 jar 已由 CI 或本機建置好（`./gradlew bootJar`），此檔僅複製 artifact 並以 `prod` profile 執行（OpenAPI/Swagger 停用）。

使用範例：

1) 使用 `Dockerfile.dev`（在容器內完成 build 並以 dev profile 啟動）：

```bash
# 建置映像（指定 Dockerfile.dev）
docker build -f Dockerfile.dev -t ig-app:dev .
# 運行容器（dev profile，會啟用 OpenAPI）
docker run --rm -p 8080:8080 ig-app:dev
```

2) 使用 `Dockerfile.prod`（先在主機建置 jar，再建置映像）：

```bash
# 在主機上先建置 jar
./gradlew bootJar
# 建置映像（使用預期已存在的 build/libs/*.jar）
docker build -f Dockerfile.prod -t ig-app:prod .
# 運行容器（prod profile，OpenAPI 停用）
docker run --rm -p 8080:8080 ig-app:prod
```

註：如果你使用 CI 系統（例如 GitHub Actions、GitLab CI、Jenkins），建議在 CI 裡面先執行 `./gradlew bootJar` 或 `./gradlew build`，然後在構建 Docker 映像時使用 `Dockerfile.prod`，這樣可減少映像大小與建置時間。

## 其他 / 貢獻

歡迎提出 issue 或 pull request。若要在本機快速開始，請確保已安裝合適的 JDK，然後執行 `./gradlew bootRun`。

---

如果你想要我把 README 調整為英文、或加入範例 API 的文件與使用方式（例如 curl 範例），告訴我我會幫你補上。
