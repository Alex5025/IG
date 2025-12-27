package org.inariforge.ig.controller;

import org.inariforge.ig.service.InstagramMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InstagramWebhookController.class)
@TestPropertySource(properties = {
    "ig.app.secret=test-secret",
    "ig.verify.token=test-token"
})
class InstagramWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstagramMessageService messageService;

    @Test
    void receiveEvent_shouldReturnOk_whenSignatureIsValid() throws Exception {
        String payload = "{\"object\":\"instagram\",\"entry\":[]}";
        String secret = "test-secret";
        String signature = "sha256=" + hmacSha256Hex(secret, payload);

        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .header("X-Hub-Signature-256", signature))
                .andExpect(status().isOk())
                .andExpect(content().string("EVENT_RECEIVED"));
    }

    @Test
    void receiveEvent_shouldReturn403_whenSignatureIsInvalid() throws Exception {
        String payload = "{\"object\":\"instagram\",\"entry\":[]}";
        String signature = "sha256=invalid_signature";

        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .header("X-Hub-Signature-256", signature))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Signature mismatch"));
    }

    @Test
    void receiveEvent_shouldReturn403_whenSignatureIsMissing() throws Exception {
        String payload = "{\"object\":\"instagram\",\"entry\":[]}";

        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Missing signature"));
    }

    private String hmacSha256Hex(String secret, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hmacBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
