package container.desktop.containerdesktopbackend.service;

import container.desktop.containerdesktopbackend.Unit;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    @Value("${jwt.secret-key}")
    private String SECRET_KEY;
    @Value("${jwt.validity-period}")
    private Integer VALIDITY_PERIOD;
    @Value("${jwt.validity-unit}")
    private String VALIDITY_UNIT;

    public String generateToken(String username) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, Unit.valueOf(VALIDITY_UNIT).getValue() * VALIDITY_PERIOD);
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("created", new Date());
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .header().add("alg", Jwts.SIG.HS256.getId()).and()
                .claims(claims)
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), Jwts.SIG.HS256)
                .expiration(calendar.getTime())
                .compact();
    }

    public String extractUsername(String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("username", String.class);
    }


}
