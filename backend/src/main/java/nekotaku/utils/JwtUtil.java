package nekotaku.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.NonNull;
import nekotaku.utils.model.TokenStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private final SecretKey jwtAccessSecret;
    private final SecretKey jwtRefreshSecret;
    private final String ROLE_LIST_NAME = "roles";

    public JwtUtil(
            @Value("${jwt.secret.access}") String jwtAccessSecret,
            @Value("${jwt.secret.refresh}")String jwtRefreshSecret
    ) {
        this.jwtAccessSecret = Keys.hmacShaKeyFor(jwtAccessSecret.getBytes());
        this.jwtRefreshSecret = Keys.hmacShaKeyFor(jwtRefreshSecret.getBytes());
    }

    public String generateAccessToken(@NonNull UserDetails userDetails) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant accessExpirationInstant = now.plusHours(1).atZone(ZoneId.systemDefault()).toInstant();
        final Date accessExpiration = Date.from(accessExpirationInstant);
        List<String> rolesList = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();
        return Jwts.builder()
                .claim(Claims.SUBJECT, userDetails.getUsername())
                .claim(Claims.EXPIRATION, accessExpiration)
                .claim(ROLE_LIST_NAME, rolesList)
                .signWith(jwtAccessSecret)
                .compact();
    }

    public String generateRefreshToken(@NonNull UserDetails userDetails) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant refreshExpirationInstant = now.plusDays(30).atZone(ZoneId.systemDefault()).toInstant();
        final Date refreshExpiration = Date.from(refreshExpirationInstant);
        return Jwts.builder()
                .claim(Claims.SUBJECT, userDetails.getUsername())
                .claim(Claims.EXPIRATION, refreshExpiration)
                .signWith(jwtRefreshSecret)
                .compact();
    }

    public TokenStatus validateRefreshToken(@NonNull String refreshToken) {
        return validateTokenDetailed(refreshToken, jwtRefreshSecret);
    }

    public TokenStatus validateAccessToken(@NonNull String accessToken) {
        return validateTokenDetailed(accessToken, jwtAccessSecret);
    }

    private TokenStatus validateTokenDetailed(@NonNull String token, @NonNull SecretKey secret) {
        try {
            createParser(secret).parseSignedClaims(token);
            return TokenStatus.VALID;
        } catch (ExpiredJwtException e) {
            log.warn("Токен истёк: {}", e.getMessage());
            return TokenStatus.EXPIRED;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT: {}", e.getMessage());
            return TokenStatus.UNSUPPORTED;
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT: {}", e.getMessage());
            return TokenStatus.MALFORMED;
        } catch (SignatureException e) {
            log.error("Invalid signature: {}", e.getMessage());
            return TokenStatus.INVALID_SIGNATURE;
        } catch (Exception e) {
            log.error("Invalid token: {}", e.getMessage());
            return TokenStatus.INVALID;
        }
    }

    private Claims getAccessClaims(@NonNull String token) {
        return getClaims(token, jwtAccessSecret);
    }

    public Claims getRefreshClaims(@NonNull String token) {
        return getClaims(token, jwtRefreshSecret);
    }

    private Claims getClaims(@NonNull String token, @NonNull SecretKey secret) {
        return createParser(secret).parseSignedClaims(token).getPayload();
    }

    public String extractUsername(String token) throws SignatureException {
        return extractClaim(token, Claims::getSubject);
    }

    public List<String> extractRoles(String token) {
        try {
            Claims claims = getAccessClaims(token);
            List<?> roles = claims.get(ROLE_LIST_NAME, List.class);
            if (roles == null) {
                throw new IllegalStateException("Роли отсутствуют в токене");
            }
            return roles.stream().map(Object::toString).toList();
        } catch (Exception e) {
            log.error("Не удалось извлечь роли: {}", e.getMessage());
            return List.of();
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAccessClaims(token);
        return claimsResolver.apply(claims);
    }

    private JwtParser createParser(SecretKey secret) {
        return Jwts.parser()
                .verifyWith(secret)
                .build();
    }

}