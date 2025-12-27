package org.inariforge.ig.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.inariforge.ig.service.InstagramMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Instagram Webhook 控制器
 *
 * 處理 Instagram Webhook 的驗證與事件接收。
 */
@RestController
public class InstagramWebhookController {

    private static final Logger log = LoggerFactory.getLogger(InstagramWebhookController.class);

    // 驗證用 token，建議在雲端環境透過 Secret Manager / 環境變數注入
    @Value("${ig.verify.token:${IG_VERIFY_TOKEN:}}")
    private String verifyToken;

    // 用於驗證簽章（App Secret）
    @Value("${ig.app.secret:${IG_APP_SECRET:}}")
    private String appSecret;

    private final InstagramMessageService messageService;

    private final ObjectMapper mapper = new ObjectMapper();

    public InstagramWebhookController(InstagramMessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 用於 Instagram Webhook 的驗證機制。
     * Instagram 會以 GET 要求驗證 endpoint，帶入 hub.mode、hub.challenge、hub.verify_token。
     */
    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.challenge", required = false) String challenge,
            @RequestParam(name = "hub.verify_token", required = false) String token
    ) {
        if (mode != null && "subscribe".equals(mode) && token != null && token.equals(verifyToken)) {
            log.info("Webhook verified successfully");
            return ResponseEntity.ok(challenge != null ? challenge : "");
        }
        log.warn("Webhook verification failed: mode={} tokenMatches={}", mode, token != null && token.equals(verifyToken));
        return ResponseEntity.status(403).body("Verification token mismatch");
    }

    /**
     * 接收 Instagram 的事件通知。
     * 1) 驗證 X-Hub-Signature-256（若設定了 App Secret）
     * 2) 將事件委派給 InstagramMessageService 處理
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> receiveEvent(
            @RequestBody byte[] bodyBytes,
            @RequestHeader(name = "X-Hub-Signature-256", required = false) String signatureHeader
    ) {
        try {
            // 如果有設定 App Secret，嘗試驗證簽章
            if (appSecret != null && !appSecret.isBlank()) {
                if (signatureHeader == null || signatureHeader.isBlank()) {
                    log.warn("Missing X-Hub-Signature-256 header");
                    return ResponseEntity.status(403).body("Missing signature");
                }
                String expected = "sha256=" + hmacSha256Hex(appSecret, bodyBytes);
                if (!constantTimeEquals(expected, signatureHeader)) {
                    log.warn("Signature mismatch");
                    return ResponseEntity.status(403).body("Signature mismatch");
                }
            }

            Map<String, Object> payload = mapper.readValue(bodyBytes, new TypeReference<Map<String, Object>>() {});
            messageService.processEvent(payload);
            return ResponseEntity.ok("EVENT_RECEIVED");
        } catch (Exception e) {
            log.error("Failed to process webhook event", e);
            return ResponseEntity.status(500).body("Error");
        }
    }

    private static String hmacSha256Hex(String secret, byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] raw = mac.doFinal(data);
        StringBuilder sb = new StringBuilder(raw.length * 2);
        for (byte b : raw) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // 防止時間差攻擊的字串比較
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
