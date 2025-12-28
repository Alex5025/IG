package org.inariforge.ig.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 用於呼叫 Facebook / Instagram Graph API 發送訊息的簡易 service。
 *
 * 注意：此實作為最小範例，實務上應加入更完整的錯誤處理、重試策略與監控。
 */
@Service
public class InstagramSenderService {

    private static final Logger log = LoggerFactory.getLogger(InstagramSenderService.class);

    @Value("${ig.page.access.token:${IG_PAGE_ACCESS_TOKEN:}}")
    private String pageAccessToken;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GRAPH_API_URL = "https://graph.facebook.com/v16.0/me/messages";

    /**
     * 非同步發送簡單文字訊息給使用者。
     * 使用簡單重試與指數退避（最多 3 次）。
     */
    @Async("taskExecutor")
    public void sendTextMessage(String recipientId, String text) {
        if (recipientId == null || recipientId.isBlank()) {
            log.warn("Recipient id is empty, skip sending");
            return;
        }
        if (pageAccessToken == null || pageAccessToken.isBlank()) {
            log.warn("IG page access token not configured, cannot send message");
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "instagram");
        Map<String, Object> recipient = new HashMap<>();
        recipient.put("id", recipientId);
        payload.put("recipient", recipient);
        Map<String, Object> message = new HashMap<>();
        message.put("text", text != null ? text : "");
        payload.put("message", message);

        String url = GRAPH_API_URL + "?access_token=" + pageAccessToken;

        int attempts = 0;
        int maxAttempts = 3;
        long backoff = 1000L;
        while (attempts < maxAttempts) {
            attempts++;
            try {
                ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<Map<String, Object>>() {};
                ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(payload), typeRef);
                Map<String, Object> response = responseEntity.getBody();
                log.info("Sent IG message to {} (attempt={}): response={}", recipientId, attempts, response);
                return;
            } catch (RestClientException e) {
                log.warn("Failed to send IG message (attempt={}): {}", attempts, e.getMessage());
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
                backoff *= 2;
            }
        }
        log.error("Giving up sending message to {} after {} attempts", recipientId, maxAttempts);
    }

}
