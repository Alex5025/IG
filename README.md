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

## 其他 / 貢獻

歡迎提出 issue 或 pull request。若要在本機快速開始，請確保已安裝合適的 JDK，然後執行 `./gradlew bootRun`。

---

如果你想要我把 README 調整為英文、或加入範例 API 的文件與使用方式（例如 curl 範例），告訴我我會幫你補上。
