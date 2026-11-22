package info.app.juleaften.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JWTService {

    private final Integer EXPIRATION_TIME;
    private final SecretKey secretKey;

    public JWTService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration.millis}") Integer expirationTime) {
        EXPIRATION_TIME = expirationTime;
        secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .signWith(secretKey)
                .issuedAt(new Date())
                .expiration(new Date(Instant.now().plusMillis(EXPIRATION_TIME).toEpochMilli()))
                .subject(username)
                .compact();

    }

    public Claims extractClaims(String jwtToken) {
        JwtParser parser = Jwts.parser().verifyWith(secretKey).build();
        Jws<Claims> claims = parser.parseSignedClaims(jwtToken);
        return claims.getPayload();
    }

    public boolean validateToken(Claims claims, UserDetails userDetails) {
        return userDetails.getUsername().equals(claims.getSubject()) && !isTokenExpired(claims);
    }

    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
