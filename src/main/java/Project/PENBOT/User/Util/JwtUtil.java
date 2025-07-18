package Project.PENBOT.User.Util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    public static final long ACCESS_TOKEN_EXPIRED = 60 * 60 * 1000L;

    private final SecretKey accesskey;

    public JwtUtil(@Value("${spring.jwt.access-secret}") String accessSecret) {
        this.accesskey = new SecretKeySpec(accessSecret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String createAccessToken(String userId, String role) {
        return Jwts.builder()
                .claim("category", "access")
                .claim("userId", userId)
                .claim("role",role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRED))
                .signWith(accesskey)
                .compact();
    }

    public Claims getClaims(String token) {
        SecretKey key = accesskey;
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
