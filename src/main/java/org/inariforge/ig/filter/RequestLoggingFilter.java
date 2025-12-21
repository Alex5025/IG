package org.inariforge.ig.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * RequestLoggingFilter
 *
 * 用於列印所有進入的 HTTP request header 與 body，方便除錯與觀察。
 * 本過濾器會包裝 request 為 ContentCachingRequestWrapper，於 filter chain 執行後
 * 讀取已快取的 request body 並記錄。請注意：若 request body 非文字或非常大，
 * 日誌可能會顯示不可讀內容或過長的輸出。
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        try {
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            logRequest(wrappedRequest);
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("-- HTTP REQUEST ----------------------------\n");
            sb.append(request.getMethod()).append(" ").append(request.getRequestURI());
            if (request.getQueryString() != null) {
                sb.append('?').append(request.getQueryString());
            }
            sb.append('\n');

            sb.append("Headers:\n");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames != null && headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                sb.append(name).append(": ").append(request.getHeader(name)).append('\n');
            }

            sb.append("Body:\n");
            byte[] content = request.getContentAsByteArray();
            if (content != null && content.length > 0) {
                String charset = request.getCharacterEncoding() != null ? request.getCharacterEncoding() : StandardCharsets.UTF_8.name();
                String body = new String(content, charset);
                sb.append(body).append('\n');
            } else {
                sb.append("<empty>\n");
            }

            sb.append("-------------------------------------------");

            log.info(sb.toString());
        } catch (Exception ex) {
            log.warn("Failed to log request", ex);
        }
    }
}
