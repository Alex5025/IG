package org.inariforge.ig.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Instagram Webhook Controller
 *
 * 處理 Instagram Webhook 的驗證與事件接收。
 */
@RestController
public class InstagramWebhookController {

    // 驗證 token，建議在 Cloud Run 上透過 Secret Manager 注入為環境變數
    @Value("${ig.verify.token:${IG_VERIFY_TOKEN:}}")
    private String verifyToken;

    /**
     * 用於 Instagram Webhook 的驗證機制。
     * Instagram 會以 GET 要求來驗證 endpoint，帶入 hub.mode, hub.challenge, hub.verify_token
     */
    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.challenge", required = false) String challenge,
            @RequestParam(name = "hub.verify_token", required = false) String token
    ) {
        if (mode != null && "subscribe".equals(mode) && token != null && token.equals(verifyToken)) {
            return ResponseEntity.ok(challenge != null ? challenge : "");
        }
        return ResponseEntity.status(403).body("Verification token mismatch");
    }

    /**
     * 接收 Instagram 的事件通知。
     * 實作中只回傳 200，實際應交由服務層處理事件解析與回覆邏輯。
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> receiveEvent(@RequestBody Map<String, Object> payload) {
        // TODO: 驗證 X-Hub-Signature 或其他簽章，並將事件推入 event processor
        // 暫時回傳 200
        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}
