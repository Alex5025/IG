# 系統設計（概要）

目標：以 Spring Boot 應用部署於 Cloud Run 作為主 webhook 與業務邏輯執行環境，使用 GCP 原生服務管理安全、排程與監控。

1. 元件架構
- Cloud Run (Spring Boot service)：接收 Instagram Webhook、處理事件、執行 keyword-mapping、觸發推播。
- GCP Secret Manager：儲存 IG Access Token、App Secret 等敏感資訊。
- Cloud SQL (Postgres)：儲存店家資料、關鍵字映射、預約/排程、黑名單、使用者註冊資料（開發環境使用 docker-compose Postgres 模擬）。
- Cloud Scheduler + Pub/Sub 或 Cloud Tasks：用於定時推播（Scheduler → Pub/Sub → Cloud Run 觸發）。
- Artifact Registry (或 Container Registry)：儲存建置的容器映像。
- GitHub Actions：CI/CD（測試 → build image → push → deploy Cloud Run → 可選 IaC）。
- Cloud Logging / Monitoring：集中日誌與告警。

2. 資料流程（簡要）
- IG 事件流：Instagram -> Webhook -> Cloud Run /webhook -> 驗證事件 -> push to event-processor -> keyword matcher -> send reply via IG Graph API
- 預約推播流：店家建立預約 -> 存入 Cloud SQL (Postgres) -> Cloud Scheduler 觸發 Pub/Sub -> Cloud Run 訂閱並執行推播

3. 介面與 API
- /webhook : 接收 GET (驗證) 與 POST (事件)
- /admin/keywords : 管理關鍵字映射（需管理員或店家授權）
- /admin/blacklist : 管理黑名單
- /admin/schedule : 建立/查詢/刪除預約提醒
- /health : 健康檢查

4. 安全與權限
- Secrets: 由 Cloud Run 的 Service Account 具有讀取 Secret Manager 權限 (roles/secretmanager.secretAccessor)。
- IAM: 服務帳號最小權限原則；CI 使用專用 deploy 帳號或 GitHub OIDC。
- 資料保護: 密碼使用 bcrypt/argon2 雜湊後儲存；輸入驗證避免注入攻擊；外部 URL 嚴格 allow-list 避免 SSRF。

5. 可觀察性與日誌
- 本地開發：輸出 log 檔（可選檔案與 console），測試時可產生詳細 debug logs。
- GCP：使用 Cloud Logging，Errors 報送至 Error Reporting，監控以 Cloud Monitoring 設定重要指標（回覆成功率、錯誤率、延遲）。

6. 擴充點
- 未來可將 keyword-mapper 換成 ML 模型（Vertex AI）或串接 Dialogflow，設計時保留介面與 adapter。

7. 儲存方案選擇建議
- 建議使用：Cloud SQL (Postgres)（支援複雜交易、SQL 查詢與成熟備援機制）。
- 本地開發：使用 `docker-compose` 啟動 Postgres 容器以模擬 Cloud SQL 環境（方便 CI 與開發測試）。
- 若專案極簡且希望完全無伺服器化，可在未來評估 Firestore，但目前以關聯資料與交易需求優先採用 Postgres。

8. 成本控制建議
- 設定 Cloud Run 最小實例（min-instances）為 0 或低數量，並設定最大實例上限；使用冷啟與雲端排程分散流量。
- Firestore 使用按需或指定索引，避免不必要的大量讀寫。
