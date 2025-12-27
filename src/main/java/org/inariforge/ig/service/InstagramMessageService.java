package org.inariforge.ig.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 處理 Instagram webhook 傳來的事件。
 *
 * 此服務會解析 payload，將訊息事件抽出並將回覆工作交由 `InstagramSenderService` 非同步發送。
 */
@Service
public class InstagramMessageService {

    private static final Logger log = LoggerFactory.getLogger(InstagramMessageService.class);

    private final InstagramSenderService senderService;

    public InstagramMessageService(InstagramSenderService senderService) {
        this.senderService = senderService;
    }

    @SuppressWarnings("unchecked")
    public void processEvent(Map<String, Object> payload) {
        // 檢查 payload 是否為空
        if (payload == null) {
            log.warn("收到的 payload 為空。");
            return;
        }

        try {
            // 根據 Instagram 的 payload 結構，訊息位於 entry[0].messaging[0]
            List<Map<String, Object>> entries = (List<Map<String, Object>>) payload.get("entry");
            if (entries == null || entries.isEmpty()) {
                log.debug("Payload 中沒有 entry 陣列。");
                return;
            }

            for (Map<String, Object> entry : entries) {
                List<Map<String, Object>> messagingEvents = (List<Map<String, Object>>) entry.get("messaging");
                if (messagingEvents == null || messagingEvents.isEmpty()) {
                    log.debug("Entry 中沒有 messaging 陣列。");
                    continue;
                }

                for (Map<String, Object> event : messagingEvents) {
                    Map<String, Object> sender = (Map<String, Object>) event.get("sender");
                    Map<String, Object> message = (Map<String, Object>) event.get("message");

                    // 確保發送者和訊息物件存在
                    if (sender != null && message != null) {
                        // 忽略 Echo 訊息 (機器人自己發送的訊息)
                        if (Boolean.TRUE.equals(message.get("is_echo"))) {
                            log.info("忽略 Echo 訊息 (is_echo=true)");
                            continue;
                        }

                        String senderId = (String) sender.get("id");
                        String messageText = (String) message.get("text");

                        // 確保 sender ID 和 message text 存在
                        if (senderId != null && messageText != null) {
                            // 記錄收到的訊息
                            log.info("收到來自 {} 的訊息: {}", senderId, messageText);

                            // 呼叫 sender service 發送回覆訊息
                            senderService.sendTextMessage(senderId, "已收到您的訊息：" + messageText);
                        }
                    }
                }
            }
        } catch (ClassCastException | NullPointerException e) {
            log.error("解析 payload 時出錯，請檢查傳入的資料結構。", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleChange(String field, Object value) {
        if (value == null) {
            log.debug("Change with field {} has no value", field);
            return;
        }

        if (value instanceof Map) {
            Map<String, Object> v = (Map<String, Object>) value;
            Object messagesObj = v.get("messages");
            if (messagesObj instanceof List) {
                List<Object> messages = (List<Object>) messagesObj;
                for (Object mObj : messages) {
                    if (mObj instanceof Map) {
                        Map<String, Object> m = (Map<String, Object>) mObj;
                        String from = safeToString(m.get("from"));
                        String text = extractTextFromMessage(m);
                        log.info("IG message received (field={}): from={} text={}", field, from, text);

                        // 非同步回覆（簡單範例）
                        try {
                            String reply = generateAutoReply(text);
                            senderService.sendTextMessage(from, reply);
                        } catch (Exception e) {
                            log.error("Failed to queue reply for {}", from, e);
                        }
                    }
                }
                return;
            }
        }

        if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            for (Object item : list) {
                log.info("IG change item (field={}): {}", field, item);
            }
            return;
        }

        log.info("IG change (field={}): {}", field, value);
    }

    private String extractTextFromMessage(Map<String, Object> message) {
        Object textObj = message.get("text");
        if (textObj != null) return safeToString(textObj);

        Object attachments = message.get("attachments");
        if (attachments instanceof List) {
            return "[attachment]";
        }
        return "";
    }

    private String safeToString(Object o) {
        return o == null ? "" : o.toString();
    }

    // 簡單自動回覆策略（可擴充為 NLU 或其他業務邏輯）
    private String generateAutoReply(String incomingText) {
        if (incomingText == null || incomingText.isBlank()) {
            return "感謝你的訊息，我們會儘快回覆。";
        }
        return "收到：\"" + incomingText + "\"，我們稍後回覆你。";
    }

}
