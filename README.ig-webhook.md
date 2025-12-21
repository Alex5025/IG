README — IG Webhook 測試與設定

目的
- 示範如何將 Instagram 的聊天室訊息傳入本專案的 Spring Boot 應用（/webhook），並確認應用能處理與回覆訊息。

前置需求
- 已建立 Facebook App，並將 Instagram Business/Creator 帳號連結到 Page。
- App 已取得必要權限：`instagram_basic`、`instagram_manage_messages`、`pages_messaging`、`pages_manage_metadata`（依需求檢查）。
- 取得：`IG_APP_SECRET`、`IG_PAGE_ACCESS_TOKEN`（或發訊 token）、以及你自定的 `IG_VERIFY_TOKEN`。

快速步驟
1) 在本機設定環境變數（示範，切勿加入版本控制）

```bash
export IG_VERIFY_TOKEN="your_verify_token"
export IG_APP_SECRET="your_app_secret"
export IG_PAGE_ACCESS_TOKEN="your_page_access_token"
export SHORT_TOKEN="your_short_token"
export IG_RECIPIENT_ID="recipient_id_for_send_script"
```

或使用 env 檔（建議）：
- `./.env.development`：範本（可提交）
- `./.env.local`：本機私密設定（不要提交，已在 `.gitignore` 忽略）

2) 啟動應用

```bash
./gradlew bootRun
```

- 應用預設監聽 `http://localhost:8080`。

3) 暴露本機給外網（測試用）
- 使用 ngrok 或等效工具（必須支援 HTTPS）：

```bash
ngrok http 8080
```

- 記下 ngrok 的 HTTPS URL，如 `https://xxxxx.ngrok.io`。

4) 在 Facebook Developer Dashboard 設定 webhook
- Callback URL：`https://<your-ngrok>.ngrok.io/webhook`
- Verify token：與 `IG_VERIFY_TOKEN` 相同
- 選取訂閱欄位：messages / messaging 等

5) 驗證流程
- 當你在 Dashboard 加入 webhook 時，FB 會發 GET 驗證到 `/webhook`，若 `IG_VERIFY_TOKEN` 與 Controller 中設定的驗證值相符，會回傳 challenge。
- 實際收到用戶訊息時，FB/IG 會 POST payload 到 `/webhook`，應用會在日誌中記錄 `IG message received`。

檢查日誌
- 在啟動應用的終端查找關鍵字：`Webhook verified`、`IG message received`、`Sent IG message`。

發送測試訊息（伺服端回覆）
- 使用 `scripts/send-ig-message.sh` 可從伺服端發送簡單測試訊息（需 `IG_PAGE_ACCESS_TOKEN` 與 `IG_RECIPIENT_ID` 已設定）。

本機 webhook 快速測試
- 可用 `scripts/test-ig-webhook.sh` 驗證 `/webhook` 的 GET 驗證與 POST 接收流程（POST payload 為範例事件）。

安全建議
- 永遠不要把 `IG_APP_SECRET`、`IG_PAGE_ACCESS_TOKEN` 或 `SHORT_TOKEN` 提交到 Git。請使用環境變數或專用 secret manager（GCP Secret Manager、Vault、GitHub Secrets）。
- Webhook 必須使用 HTTPS（ngrok 僅供測試）。生產環境請使用受信任的 TLS endpoint（Cloud Run / Load Balancer / Ingress）。
- 在生產環境務必啟用 `IG_APP_SECRET` 的簽章驗證（X-Hub-Signature-256）。

除錯建議
- 若未收到 webhook：確認 webhook subscription 是否處於已訂閱狀態、ngrok session 是否仍有效、應用 endpoint 是否可在外網存取。
- 可用 cURL 模擬 GET 驗證：

```bash
curl -v -G "https://<your-ngrok>.ngrok.io/webhook" \
  --data-urlencode "hub.mode=subscribe" \
  --data-urlencode "hub.challenge=CHALLENGE_TOKEN" \
  --data-urlencode "hub.verify_token=$IG_VERIFY_TOKEN"
```

聯絡與下一步
- 若需要我代為啟動 `ngrok` 與本機應用並觀察日誌，請授權我執行 workspace task（`ngrok` 與 `./gradlew bootRun`）。
- 或者我可以協助將 `send-ig-message.sh` 調整為符合你實際使用的 IG Graph API 版本與 payload 格式。
