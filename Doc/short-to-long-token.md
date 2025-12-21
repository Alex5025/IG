# 短期 (short‑lived) User Token 換成 長期 (long‑lived) User Token — 操作手冊

**目的**
- 說明如何把 Facebook / Instagram 的 short‑lived user access token 換成 long‑lived user token（可使用在拿 Page token、長時間測試）。

**適用對象**
- 需要用 long token 取得 Page access token，或在伺服器端保存較長有效期憑證的開發者。

**風險與安全提醒**
- Token 與 App Secret 都是敏感憑證，**切勿**在公開場合貼出完整 token 或 `APP_SECRET`。
- 若 token 被洩露，請在 Facebook App 或 Page 設定撤銷並重新產生。

**前置條件**
- 你有一個 Facebook App（App ID, App Secret）。
- 你能取得 short‑lived user token（來自 Graph API Explorer 或 OAuth 授權流程）。
- 若要拿 Page token，該 user 必須為 Page 管理員。

**步驟概覽**
1. 以 Graph API Explorer 或 OAuth 流程取得 short‑lived user token（測試用）。
2. 用 `APP_ID`、`APP_SECRET` 與 `SHORT_TOKEN` 呼叫 OAuth 換取 long‑lived user token（60 天）。
3. 用 long‑lived user token 呼叫 `/me/accounts` 取得 Page access token（若你為 Page 管理員）。
4. （選用）使用 `debug_token` 檢查 token 屬性。

**詳細步驟與範例命令**

**1) 產生 short‑lived user token（測試）**
- 開啟 Graph API Explorer：https://developers.facebook.com/tools/explorer
- 在右上角選擇你的 App（例如：`test2`），按「Get Token」→「Get User Access Token」，勾選需要的權限 scope（例如 `pages_show_list`, `pages_messaging`, `instagram_basic`, `instagram_manage_messages` 等），完成授權後會獲得 short token（通常有效 1–2 小時）。

**2) 把 short‑lived token 換成 long‑lived user token（60 天）**
- 將以下命令中的 `APP_ID`、`APP_SECRET`、`SHORT_TOKEN` 替換為你的值：

```bash
curl -s "https://graph.facebook.com/v19.0/oauth/access_token?grant_type=fb_exchange_token&client_id=APP_ID&client_secret=APP_SECRET&fb_exchange_token=SHORT_TOKEN" | jq .
```

- 回傳範例：
```json
{
  "access_token": "LONG_USER_TOKEN",
  "token_type": "bearer",
  "expires_in": 5184000
}
```
- 記下 `access_token`（這是 long‑lived user token）與 `expires_in`。

**3) 用 long‑lived user token 取得 Page access token（若你為 Page 管理員）**
- 列出你管理的 Page（回傳中每個 page 含 `access_token`）：

```bash
curl -s "https://graph.facebook.com/v19.0/me/accounts?access_token=LONG_USER_TOKEN" | jq .
```

- 範例回傳（每個 page object 會包含 `access_token`，即 PAGE_ACCESS_TOKEN）：
```json
{
  "data": [
    {
      "access_token": "PAGE_ACCESS_TOKEN",
      "category": "Product/Service",
      "name": "My Page",
      "id": "1234567890"
    }
  ]
}
```

- 若 `data` 為空陣列，表示該 user 不是任何 Page 的管理員。

**4) 檢查 token 詳細資訊（可選，但建議）**
- 使用 `debug_token` 來檢視 token 的 scopes、到期時間、是否有效等（需要 `APP_ID|APP_SECRET` 當作 app access token）：

```bash
curl -s "https://graph.facebook.com/debug_token?input_token=TOKEN_TO_DEBUG&access_token=APP_ID|APP_SECRET" | jq .
```

- 回傳範例會包含 `is_valid`, `scopes`, `issued_at`, `expires_at` 等欄位。

**一鍵測試區塊（可複製執行）**

把 `SHORT_TOKEN`、`APP_ID`、`APP_SECRET` 變成你的值：

```bash
# 1) 換成 long token
SHORT_TOKEN='PASTE_SHORT_TOKEN'
APP_ID='PASTE_APP_ID'
APP_SECRET='PASTE_APP_SECRET'
curl -s "https://graph.facebook.com/v19.0/oauth/access_token?grant_type=fb_exchange_token&client_id=${APP_ID}&client_secret=${APP_SECRET}&fb_exchange_token=${SHORT_TOKEN}" | jq .

# 2) 假設取得 long token，export 後列出 pages
export LONG_TOKEN='PASTE_LONG_TOKEN'
curl -s "https://graph.facebook.com/v19.0/me/accounts?access_token=${LONG_TOKEN}" | jq .
```

**常見問題與對應解法**
- `Invalid OAuth access token - Cannot parse access token`：通常是 token 有誤（換行、空格、被截斷）或你把 short token 當成 page token。請把 token 存入變數再使用，或使用單引號包住。示例：`export TOKEN='PASTE_TOKEN'`。
- `data: []`（`/me/accounts` 回空）：表示該 user 不是 Page 管理員，需以 Page 管理員授權或把該 user 加為管理員。
- 權限不足（scopes 缺少）：用 Graph API Explorer 再次授權並勾選所需 scope；若 App 在開發模式下，僅對 App Roles 有效，非 roles 的帳號無法獲得某些權限。

**安全建議（必讀）**
- 長期運行時請勿把 token 硬編在原始碼，改用環境變數或 Secret Manager（如 GCP Secret Manager）。
- 在測試期間若 token 不慎洩露，立刻在 Facebook App 或 Page 管理界面撤銷並重新產生。

---
// 檔案建立於專案路徑：`Doc/short-to-long-token.md`，如需我把文件放到其他位置或補充內容（例如 OAuth redirect 範例、圖示流程），告訴我要加入哪些段落。
