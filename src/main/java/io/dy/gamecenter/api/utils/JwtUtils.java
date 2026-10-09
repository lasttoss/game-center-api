package io.dy.gamecenter.api.utils;

import io.dy.gamecenter.api.constants.JwtConstants;
import io.dy.gamecenter.api.models.UserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    public String jwtSecret;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractJwtId(String token) {
        return extractClaim(token, Claims::getId);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        if (claims != null) {
            return claimsResolver.apply(claims);
        }
        return null;
    }

    public Claims extractAllClaims(String token) {
        try {
            // Decodes the token and returns the claims it carries.
            Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
            return claims;
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserModel user, long issuedAt, long expired) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, user.getUserId(), issuedAt * 1000, expired * 1000);
    }

    private String createToken(Map<String, Object> claims, String subject, long issuedAt, long expiredTime) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date(issuedAt))
                .setExpiration(new Date(expiredTime))
                .signWith(SignatureAlgorithm.HS256, jwtSecret).compact();
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    private String[] getAuthorities(Collection<? extends GrantedAuthority> authorities) {
        String[] roles = new String[authorities.size()];
        int i = 0;
        for (GrantedAuthority authority : authorities) {
            roles[i++] = authority.getAuthority();
        }
        return roles;
    }
}


