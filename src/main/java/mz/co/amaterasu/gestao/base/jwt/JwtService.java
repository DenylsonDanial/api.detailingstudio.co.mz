package mz.co.amaterasu.gestao.base.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.lang.reflect.InvocationTargetException;
import java.security.Key;
import java.util.*;
import java.util.function.Function;

import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import mz.co.amaterasu.gestao.user.dto.UserDTO;
import mz.co.amaterasu.gestao.user.dto.projection.PUserDTO;
import mz.co.amaterasu.gestao.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    public static final Logger LOG = LoggerFactory.getLogger(JwtService.class);

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    @Autowired
    private UserService userService;

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Log the exception message
            logger.error("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    public String extractTenantId(String token) {
        try {
            return extractClaim(token, claims -> claims.get("tenantId", String.class));
        } catch (Exception e) {
            return null;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails, String tenant) {
        return generateToken(new HashMap<>(), userDetails, tenant);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, String tenant) {
        return buildToken(extraClaims, userDetails, jwtExpiration, tenant);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    public String updateTokenTenant(String token, String tenantId, UserDetails userDetails) {
        // Extract the existing claims
        Claims claims = extractAllClaims(token);

        // Generate a new token with updated claims
        return buildToken(claims, userDetails, jwtExpiration, tenantId);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration,
            String tenant
    ) {
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        JwtBuilder jwtBuilder = Jwts
                .builder()
                .setClaims(extraClaims)
                .claim("authorities", authorities);

        try {
            PUserDTO userDTO = userService.findByUsername(userDetails.getUsername());
            if (Objects.nonNull(userDTO.getPasswordChangedAt()))
                jwtBuilder.claim("passwordChangedAt", userDTO.getPasswordChangedAt().toString());
        } catch (ApiRequestException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException e) {
            LOG.info("Error while generating token: {}", e.getMessage());
        }

        if (tenant != null) {
            jwtBuilder.claim("tenantId", tenant);
        }

        return jwtBuilder
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        logger.debug("Secret key: {}", secretKey);
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        Key key = Keys.hmacShaKeyFor(keyBytes);
        logger.debug("Decoded key: {}", key);
        return key;
    }
}
