package org.qatlas.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * Minimal signed-token service for the single shared admin account.
 * Deliberately avoids pulling in a JWT library or Spring Security for what is,
 * for now, a single gated action (deleting/archiving executions): a HMAC-SHA256
 * signed token is enough, self-contained, and needs zero new Maven dependencies.
 *
 * Token format: base64url(username|expiryEpochMillis) + "." + base64url(HMAC-SHA256 signature)
 */
@Component
public class AdminTokenService {

    private final String secret;
    private final long expiryMinutes;

    public AdminTokenService(
            @Value("${app.security.token-secret}") final String secret,
            @Value("${app.security.token-expiry-minutes:480}") final long expiryMinutes) {
        this.secret = secret;
        this.expiryMinutes = expiryMinutes;
    }

    public String issueToken(final String username) {
        final long expiry = Instant.now().plusSeconds(expiryMinutes * 60).toEpochMilli();
        final String payload = username + "|" + expiry;
        final String encodedPayload = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));
        final String signature = sign(encodedPayload);
        return encodedPayload + "." + signature;
    }

    /** Returns the username if the token is well-formed, correctly signed, and not expired. */
    public Optional<String> validate(final String token) {
        if (token == null || !token.contains(".")) {
            return Optional.empty();
        }
        final String[] parts = token.split("\\.", 2);
        if (parts.length != 2) {
            return Optional.empty();
        }
        final String encodedPayload = parts[0];
        final String providedSignature = parts[1];
        final String expectedSignature = sign(encodedPayload);
        if (!constantTimeEquals(expectedSignature, providedSignature)) {
            return Optional.empty();
        }
        try {
            final String payload = new String(base64UrlDecode(encodedPayload), StandardCharsets.UTF_8);
            final String[] payloadParts = payload.split("\\|", 2);
            final String username = payloadParts[0];
            final long expiry = Long.parseLong(payloadParts[1]);
            if (Instant.now().toEpochMilli() > expiry) {
                return Optional.empty();
            }
            return Optional.of(username);
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    private String sign(final String data) {
        try {
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return base64UrlEncode(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (final Exception e) {
            throw new IllegalStateException("Unable to sign admin token", e);
        }
    }

    private static String base64UrlEncode(final byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] base64UrlDecode(final String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    private static boolean constantTimeEquals(final String a, final String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
