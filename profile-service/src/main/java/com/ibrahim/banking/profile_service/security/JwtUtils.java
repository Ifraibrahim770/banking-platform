package com.ibrahim.banking.profile_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwt.secret}") // In application.properties: app.jwt.secret=YourVeryLongAndSecureSecretKeyHere
    private String jwtSecretString;

    @Value("${app.jwt.expirationMs}") // In application.properties: app.jwt.expirationMs=86400000 (e.g., 24 hours)
    private int jwtExpirationMs;

    private SecretKey key; // Derived from jwtSecretString

    // Initialize the key after properties are set
    @jakarta.annotation.PostConstruct
    public void init() {
        // Ensure the secret key is strong enough for HS512
        if (jwtSecretString == null || jwtSecretString.length() < 64) { // HS512 needs 512 bits = 64 bytes
            logger.warn("JWT Secret is too short! Using a default generated key. PLEASE SET a strong 'app.jwt.secret' in properties.");
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        } else {
            byte[] keyBytes = jwtSecretString.getBytes();
            this.key = Keys.hmacShaKeyFor(keyBytes);
        }
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

     public String generateTokenFromUsername(String username) {
        return Jwts.builder()
               .setSubject(username)
               .setIssuedAt(new Date())
               .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
               .signWith(key, SignatureAlgorithm.HS512)
               .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
        return claims.getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
} 