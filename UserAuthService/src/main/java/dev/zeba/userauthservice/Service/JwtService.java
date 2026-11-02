package dev.zeba.userauthservice.Service;

import dev.zeba.userauthservice.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    //2 variables from application properties
    private final SecretKey secretKey;
    private final long expirySeconds; //900ms = 15 mins

    //secret from env; key stored in encoded format: by default UTF-8 :
    //JWT_SECRET : should be min 32 chars, SHA256 requirement. Value to sign token: random text to sign token, refresh every now n then
    //banking system doesnt use refresh token
    public JwtService(@Value("${jwt.secret-key}") String secret, @Value("${jwt.expiration}") long expirySeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirySeconds = expirySeconds;
    }

    //JWT token : 3 parts: header, payload (JSON), signature: auth/user related info(username, roles): payload: every entry in payload is called claim
    //Some inbuilt claims in JWT: subject(email), expiration, issuer, issued at
    //Custom claims: role
    public String generateToken(User user){
    //Jwts: class used to build jwt; gives builder to set attributtes
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .issuer("userAuthService")
                .expiration(new Date(System.currentTimeMillis() + expirySeconds * 1000))
                .claim("role",user.getRole().name()) //name converts enum to string since JWT is string based token ; all business logics can be passed in claims
                .signWith(secretKey)
                .compact(); //actually generates string of JWT : final builder step
                //configuration steps + .compact() to generate JWT

            //when checked on jwt.io the JWT from postman, it said signature verification failed coz it doesn't have secret key in its server, it is
        // in local server
    }

    public Claims parseToken(String token){
        return Jwts.parser() //Building parser, telling parser key with which it should parse
                .verifyWith(secretKey) //Building parser, telling parser key with which it should parse
                .build() //Building parser, telling parser key with which it should parse
                .parseSignedClaims(token) //actually parsing token
                .getPayload(); //actually parsing token

//        JwtParserBuilder builder = Jwts.parser();
//        builder = builder.verifyWith(secretKey);
//        JwtParser parser = builder.build();
//        Jws<Claims> jws = parser.parseSignedClaims(token);
//        Claims claims = jws.getPayload();
//        return claims;
    }
}
