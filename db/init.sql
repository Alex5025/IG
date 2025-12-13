-- 初始化資料表 (Postgres)
--
-- 說明：
-- 此檔案包含專案初次啟動所需的基本資料表結構，用於儲存使用者、商店、關鍵字回覆、黑名單與預約排程。
-- 請注意：實際專案上線時可能需要新增索引、約束、或調整欄位型別以符合效能與安全需求。

-- users：儲存使用者（管理員、店家成員、消費者等）
CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY, -- 使用者內部 id
  ig_id VARCHAR(128) UNIQUE, -- Instagram 使用者 ID（若有）
  username VARCHAR(128), -- 使用者顯示名稱
  hashed_password VARCHAR(256), -- 密碼雜湊（請使用 bcrypt/argon2 存放雜湊值）
  role VARCHAR(32), -- 角色（例如 ADMIN / OWNER / MEMBER / CUSTOMER）
  created_at TIMESTAMP DEFAULT now() -- 建立時間
);

-- stores：儲存店家資料
CREATE TABLE IF NOT EXISTS stores (
  id SERIAL PRIMARY KEY, -- 商店內部 id
  name VARCHAR(255), -- 商店名稱
  address TEXT, -- 商店地址（可為文字或 JSON 格式視需求擴充）
  owner_user_id INTEGER REFERENCES users(id), -- 店家擁有者對應 users.id
  created_at TIMESTAMP DEFAULT now() -- 建立時間
);

-- keywords：關鍵字對應回覆
-- store_id 與 keyword 組合須唯一，避免同一商店有重複關鍵字
CREATE TABLE IF NOT EXISTS keywords (
  id SERIAL PRIMARY KEY,
  store_id INTEGER REFERENCES stores(id), -- 所屬商店
  keyword VARCHAR(128) NOT NULL, -- 觸發關鍵字
  reply TEXT NOT NULL, -- 回覆內容
  created_at TIMESTAMP DEFAULT now(), -- 建立時間
  UNIQUE(store_id, keyword)
);

-- blacklist：商店的黑名單，用來標記不應回覆或封鎖的 IG 使用者
CREATE TABLE IF NOT EXISTS blacklist (
  id SERIAL PRIMARY KEY,
  store_id INTEGER REFERENCES stores(id), -- 所屬商店
  ig_id VARCHAR(128) NOT NULL, -- 被列入黑名單的 IG 使用者 ID
  reason TEXT, -- 加入黑名單理由（選填）
  created_at TIMESTAMP DEFAULT now(), -- 建立時間
  UNIQUE(store_id, ig_id)
);

-- schedules：預約或定時推播紀錄
-- status 可為 PENDING / SENT / FAILED 等
CREATE TABLE IF NOT EXISTS schedules (
  id SERIAL PRIMARY KEY,
  store_id INTEGER REFERENCES stores(id), -- 所屬商店
  user_id INTEGER REFERENCES users(id), -- 要接收推播的使用者（若有）
  message TEXT, -- 要推送的訊息內容
  scheduled_at TIMESTAMP NOT NULL, -- 預定推播時間
  status VARCHAR(32) DEFAULT 'PENDING', -- 狀態
  created_at TIMESTAMP DEFAULT now() -- 建立時間
);
