1. 目標與範圍：這個 IG 自動回覆機器人主要要做哪些事？
 - 關鍵字觸發
 - 自動回覆私
 - 問答式定位服務
 - 定時推播 (預約服務即將到來)
2. Instagram 帳號類型：朋友使用的是「個人帳號」還是「商業/創作者帳號（可用 Graph API/Webhooks）」？
 - 商業/創作者帳號
3. 互動方式：要用 Webhook（被動接收 IG 事件）還是定期 Polling？（建議 Webhook）
 - Webhook
 - Polling (如果需要付費則先不要)
4. 回覆邏輯：回覆要簡單 keyword mapping，還是要接外部 NLP（例如 Dialogflow / Vertex AI）？
 - 簡單 keyword mapping 
 - 先不要串接 AI
5. 語言與技術棧：偏好使用現有 Spring Boot（Java）專案，還是改用 Node.js / Python？（目前 repo 是 Spring Boot）
 - 使用 Spring Boot 
 - 前端可以考慮使用 Vue3
6. 認證與祕密管理：要用 GCP Secret Manager 儲存 IG token & service account，還是用 GitHub Secrets 直接在 CI 注入？
 - 用 GCP Secret Manager 處理通訓相關的 token
 - 服務相關的參數則利用 GitHub Secrets 直接在 CI 注入
7. 部署細節：Cloud Run 是否需使用自動擴縮與最小實例（例如 min-instances）？希望部署在哪個 GCP 區域？
 - 利用自動擴縮與最小實例
 - asia-southeast1
8. CI/CD 要求：GitHub Actions 要做到哪些步驟？
    1. run test
    2. build images
    3. push images
    4. deploy to Cloud Run
    5. run infrastructure IaC
9. 日誌與監控：需要整合哪種監控/告警（Stackdriver/Cloud Monitoring、Logging、Error Reporting）？
 - dev 要在本地 寫出 log file 
 - GCP 上要整合 google 的 logs explorer
10.  法律與隱私：是否有要遵守的隱私或訊息保留政策（例如保留私訊 X 天後刪除）？
 - 顧客的密碼要做 hash 處理
11.  權限與訪問：誰可以管理/更新機器人（單一朋友、或多人團隊）？
 - 管理員（服務建立者）
 - 店家主人
 - 團隊成員（店家成員）
 - 消費者（有註冊店家會員，有資料客戶）
 - 消費者（未註冊者，無資料客戶）
12.  其他特殊需求或約束（流量、成本上限、回覆頻率限制、黑名單/白名單）？
 - 需要成本上限 50 USD/月
 - 店家要有黑名單