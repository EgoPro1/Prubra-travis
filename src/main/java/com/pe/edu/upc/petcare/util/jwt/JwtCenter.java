package com.pe.edu.upc.petcare.util.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtCenter implements Serializable {
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;

    // Secret Property
    @Value("${jwt.secret}")
    private String secret;

    public JwtCenter setToken(String token) {
        this.token = token;
        return this;
    }

    private String token;

    public String getUsername() {
        return getClaim(Claims::getSubject);
    }

    public Date getIssuedAtDate() {
        return getClaim(Claims::getIssuedAt);
    }

    public Date getExpirationDate() {
        return getClaim(Claims::getExpiration);
    }

    public <T> T getClaim(Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims();
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaims() {
        return Jwts.parser().setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isExpired() {
        final Date expiration = getExpirationDate();
        return expiration.before(new Date());
    }

    private Boolean ignoreExpiration() {
        return false;
    }

    public Boolean canBeRefreshed() {
        return (!isExpired() || ignoreExpiration());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername());
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() +
                        JWT_TOKEN_VALIDITY * 100))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Boolean validate(UserDetails userDetails) {
        final String username = getUsername();
        return (username.equals(userDetails.getUsername())) && !isExpired();
    }
}
