package com.learn.bookService.configuration;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
    private String secret;
    

    // This method generates a JWT token for the given username and role.
    // The token includes the username as the subject and the role as a claim.
    // The token is signed using the HMAC SHA-256 algorithm with a secret key.
    // The token also has an expiration time of 10 minutes from the time it is generated.
    // This method is called in the AuthController after successful authentication to create
    // a token that will be returned to the client.
    public String generateToken(UserDetails userDetails) {

//        return Jwts.builder()
//                .setSubject(username)
//                .claim("role", role)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
//                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
//                .compact();


    	String token = Jwts.builder()
    		    .setSubject(userDetails.getUsername())
    		    .claim("roles", userDetails.getAuthorities().stream()
    		        .map(GrantedAuthority::getAuthority).toList())
    		    .setIssuedAt(new Date())
    		    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))  // 1 hour expiration
    		    .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
    		    .compact();
    	
    	return token;
    }

    // This method validates the JWT token and returns the claims if the token is valid.
    // If the token is invalid or expired, it will throw an exception.
    // The claims contain the information stored in the token, such as the username and role.
    // This method is used in the JwtFilter to validate the token from incoming requests.
    // The JwtFilter will call this method to check if the token is valid before allowing access to protected endpoints.
    // If the token is valid, the claims will be used to set the authentication in the security context.
    // If the token is invalid, the filter will reject the request and return an unauthorized response.
    // In summary, this method is crucial for ensuring that only requests with valid JWT tokens can access
    // protected resources in the application.
    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

	public String extractUsername(String token) {
		Claims claims = validateToken(token);
	     return claims.getSubject();
	}

	public boolean validateToken(String token, UserDetails userDetails) throws Exception {
		// TODO Auto-generated method stub
        Claims claims = validateToken(token);
        String username = claims.getSubject();
        return username.equals(userDetails.getUsername()) && claims.getExpiration().after(new Date());
	}
}
