package cloud.chlora.management.security.util;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieAuth {

    public static String ACCESS_TOKEN_COOKIE = "access_token";

    public String accessTokenCookie(String token) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofHours(8))
                .build()
                .toString();
    }
}
