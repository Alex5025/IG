# 專案需求（EARS）

專案名稱：Instagram 自動回覆機器人
目的：替店家提供自動回覆、問答式定位服務與定時推播功能，部署於 GCP Cloud Run，CI/CD 使用 GitHub Actions。

-- 要求清單（EARS 格式）

1) WHEN 收到 Instagram 私訊或留言事件, THE SYSTEM SHALL 使用 Webhook 被動接收事件並驗證事件來源。
   - 驗收條件：Webhook 驗證成功且回傳 200。

2) WHEN 收到含關鍵字的私訊或留言, THE SYSTEM SHALL 根據 keyword-mapping 回覆對應訊息。
   - 驗收條件：已定義的關鍵字會觸發正確回覆；未命中的情形回覆預設訊息。

3) WHEN 使用者以問答方式查詢店家位置或服務, THE SYSTEM SHALL 提供問答式定位服務（基於 keyword mapping 與資料庫內店家資料）。
   - 驗收條件：輸入包含地址或關鍵字時，回傳店家位置格式化結果。

4) WHEN 店家設定「預約提醒」或定時推播, THE SYSTEM SHALL 支援定時推播（Scheduled push），並可於預定時間推送私訊。
   - 驗收條件：預約時間到達時會觸發推播且記錄發送結果（成功/失敗）。

5) WHEN 系統需要存取外部機密（IG token 等）, THE SYSTEM SHALL 從 GCP Secret Manager 讀取敏感金鑰；CI 時由 GitHub Secrets 注入部署參數。
   - 驗收條件：程式不在版本庫內硬編碼任何敏感資訊。

6) WHEN 部署到 Cloud Run, THE SYSTEM SHALL 設定自動擴縮與 min-instances，並預設區域為 asia-southeast1。
   - 驗收條件：Cloud Run 服務可接受流量且可自動擴縮，最少維持指定 min-instances。

7) WHEN 建置與部署時, THE SYSTEM SHALL 在 GitHub Actions 做下列工作流程：執行測試、建置映像、推送映像、部署 Cloud Run、（必要時）執行 IaC。
   - 驗收條件：PR 合併後能自動完成上述流程或依環境變數決定執行階段。

8) WHEN 產生日誌與監控事件, THE SYSTEM SHALL 本地開發產生 log 檔；在 GCP 上整合 Cloud Logging / Logs Explorer 與 Cloud Monitoring。
   - 驗收條件：能在 GCP Logging 查看服務日誌與錯誤報告。

9) IF 接收到使用者個資（例如密碼） THEN THE SYSTEM SHALL 對顧客密碼進行安全雜湊處理 (bcrypt/argon2) 後儲存，並遵循最小保存策略。
   - 驗收條件：資料庫中不儲存明文密碼。

10) WHILE 系統運行, THE SYSTEM SHALL 支援多種使用者角色：管理員、店家主人、店家成員、註冊消費者、未註冊消費者，並以最小權限原則設計 API 權限。
    - 驗收條件：各角色存取受限，且管理介面只能由管理員或店家授權人員操作。

11) WHERE 店家管理需黑名單功能, THE SYSTEM SHALL 提供黑名單管理並於接收到黑名單使用者事件時拒絕回覆或標記不回覆。
    - 驗收條件：黑名單使用者訊息不會被自動回覆，且可以由店家管理介面新增/移除。

12) OPTIONAL: IF 未來需要升級回覆邏輯 THEN THE SYSTEM SHALL 保留擴充點以串接 Vertex AI 或 Dialogflow。

-- 非功能性需求與限制

- 成本限制：整體雲端費用上限為 50 USD/月（須透過資源限制、冷啟與最少實例調整達成）。
- 可用性：以 Cloud Run 及 Cloud Scheduler 組合確保高可用；支援自動重試與失敗記錄。
- 延遲：關鍵字回覆延遲應低於 3 秒（在正常情況下）。
- 日誌保留：依法律或店家需求設定，預設保留 30 天。
- 安全：所有外部通訊使用 HTTPS；避免 SSRF、Injection 等漏洞（採參數化 DB、輸入驗證）。

-- 接受標準

- 單元測試覆蓋主要關鍵字回覆邏輯。
- E2E 測試驗證 webhook 接收與 Cloud Run 部署流程。
- 部署文件與運維 runbook 完成。
