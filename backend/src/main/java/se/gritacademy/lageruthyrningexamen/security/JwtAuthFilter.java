package se.gritacademy.lageruthyrningexamen.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final Key key;

    public JwtAuthFilter(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Validera JWT-token från Authorization-header och sätt användarinformation i säkerhetskontexten
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String header = request.getHeader("Authorization");

        logger.debug("JWT-filtret: {} {}", request.getMethod(), path);
        logger.debug("Authorization-header tillstede: {}", header != null);

        // Kontrollera om Authorization-headern finns och börjar med "Bearer "
        if (header != null && header.startsWith("Bearer ")) {
            try {
                // Extrahera JWT-token från headern (ta bort "Bearer " prefixet)
                String token = header.substring(7).trim();
                logger.debug("Token längd: {}", token.length());

                // Parsa och validera JWT-tokenen med den kryptografiska nyckeln
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                // Extrahera användarens ID och roll från JWT-claims
                String userId = claims.getSubject();
                String role = claims.get("role", String.class);
                logger.debug("JWT validering lyckades, användar-ID: {}, roll: {}", userId, role);

                // Skapa behörigheter baserat på användarens roll
                List<GrantedAuthority> authorities = new ArrayList<>();
                
                // Kontrollera om rollen finns och är inte tom
                if (role != null && !role.isEmpty()) {
                    // Se till att rollen börjar med "ROLE_" för Spring Security-kompatibilitet
                    String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    authorities.add(new SimpleGrantedAuthority(roleWithPrefix));
                    logger.trace("Roll lagd till behörigheter: {}", roleWithPrefix);
                }

                // Skapa ett autentiseringsobjekt med användarens ID och behörigheter
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                authorities
                        );

                // Sätt autentiseringen i säkerhetskontexten för denna request
                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.debug("Autentisering satt i säkerhetskontexten för användar-ID: {}", userId);

            } catch (Exception e) {
                logger.warn("JWT validering misslyckades: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            }
        }

        // Fortsätt med resten av filterkejdan
        filterChain.doFilter(request, response);
    }
}
