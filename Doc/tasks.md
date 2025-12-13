# 實作任務與里程碑

本清單為可追蹤的實作步驟，建議以小步快跑方式完成每項任務。

短期 (MVP) 任務：
1. 設定專案與本地開發環境
   - 新增 `application-{profile}.yml` 範本及本地測試說明。
   - 建立 Cloud SQL (Postgres) 本地開發環境說明（使用 `docker-compose` 啟動 Postgres 容器），並提供資料庫初始化腳本。

2. Webhook 與基本回覆邏輯
   - 實作 `/webhook` endpoint（GET 驗證、POST 事件接收）。
   - 實作 keyword-mapper service（從資料庫讀取映射）。
   - 加入黑名單檢查邏輯。
   - 單元測試：關鍵字命中、未命中、黑名單。

3. 定時推播 (MVP)
   - 設計預約資料模型（儲存在 Cloud SQL / Postgres）。
   - 使用 Cloud Scheduler + Pub/Sub 設計推播流程（本地可模擬，Scheduler 可用 cronjob 或手動觸發測試流程；Pub/Sub 可使用 emulator 或以直接呼叫模擬訊息）。
   - E2E 測試：建立預約並觸發推播，驗證雲端與本地模擬流程。

4. 安全與密鑰管理
   - 整合 GCP Secret Manager 取得 IG token 與 App Secret。
   - 對密碼或敏感欄位使用 bcrypt/argon2 雜湊。

5. CI/CD 設定（GitHub Actions）
   - Job: `test`（執行單元測試）
   - Job: `build`（建置 jar + 建容器映像）
   - Job: `push`（推送至 Artifact Registry）
   - Job: `deploy`（部署至 Cloud Run，支援環境分支）
   - 可選: `iac`（執行 Terraform / gcloud infra）

6. 監控與日誌
   - 本地產生 log 檔（dev 設定），並在 Cloud Run 上啟用 Cloud Logging。
   - 設定基本的 Cloud Monitoring 指標與 Alert Policy。

7. 文件與交付
   - 撰寫 `Doc/runbook.md`（運維手冊）
   - 撰寫 `README.md`（如何在本地跑、如何部署）

長期 / 未來工作：
- 加入 UI（Vue3）供店家管理關鍵字、黑名單與排程。
- 串接 Vertex AI 作更進階的 NLP 回覆。
- 成本分析與優化（監控實際費用並自動調整資源）。

每項任務建議 owner 與估時後續細化於 `tasks` 管理系統。
