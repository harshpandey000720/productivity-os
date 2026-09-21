package com.Harsh.Productivity.Os.service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
@Service
public class JWTService {
    private final SecretKey key;
//    public JWTService(){
//        try {
//            KeyGenerator keyGen=KeyGenerator.getInstance("HmacSHA256");
//            SecretKey sk=keyGen.generateKey();
//            secretKey= Base64.getEncoder().encodeToString(sk.getEncoded());
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//    }
    public String generateToken(String username) {
        Map<String,Object> claims=new HashMap<>();
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+1000L*60*30))
                .and()
                .signWith(getKey())
                .compact();
    }
    public JWTService(@Value("${jwt.secret}") String jwtSecret) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getKey() {
        return key;
    }
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    private<T> T extractClaim(String token, Function<Claims ,T> claimResolver){
        final Claims claims=extractAllClaims(token);
        return claimResolver.apply(claims);
    }
    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getKey())
                .build().
                parseSignedClaims(token)
                .getPayload();
    }
    public boolean validateToken(String token, UserDetails userDetais) {
        final String userName=extractUserName(token);
        return (userName.equals(userDetais.getUsername())&&!isTokenExpired(token));
    }
    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }
    private Date extractExpiration(String token){
        return extractClaim(token,Claims::getExpiration);
    }
}
