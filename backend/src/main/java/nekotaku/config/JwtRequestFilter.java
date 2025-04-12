package nekotaku.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nekotaku.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER_STR = "Bearer ";
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION);
        String username = null;
        String jwt = null;
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_STR)) {
            try {
                jwt = authHeader.substring(BEARER_STR.length());
                if (!jwt.equalsIgnoreCase("null")) {
                    username = jwtUtil.extractUsername(jwt);
                }
            }
            catch (ExpiredJwtException e) {
                logger.warn("Время жизни токена истекло.");
            }
            catch (SignatureException e) {
                logger.warn("Подпись JWT не совпадает с локально вычисленной подписью. Действительность JWT не может быть подтверждена и не должна вызывать доверия.");
            }
            catch (MalformedJwtException exception) {
                logger.error("MalformedJwtException: ");
            }
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    username, null, jwtUtil.extractRoles(jwt).stream().map(SimpleGrantedAuthority::new).toList()
            );
            SecurityContextHolder.getContext().setAuthentication(token);
        }

        filterChain.doFilter(request, response);
    }

}
