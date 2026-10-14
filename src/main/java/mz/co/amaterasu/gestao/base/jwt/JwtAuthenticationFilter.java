package mz.co.amaterasu.gestao.base.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.co.amaterasu.gestao.util.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    /**
     * Prefixos servidos pela base de dados master. Pedidos a qualquer outro
     * prefixo sao encaminhados para a base de dados do tenant indicado no JWT.
     */
    private static final List<String> SYSTEM_BASE_PATHS = List.of(
            "auth", "users", "roles", "permissions", "countries", "provinces", "districts", "companySetup", "companies"
    );
    private final HandlerExceptionResolver handlerExceptionResolver;

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            HandlerExceptionResolver handlerExceptionResolver
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            try {
                filterChain.doFilter(request, response);
            } finally {
                TenantContext.clear();
            }
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);

            TenantContext.setMasterTenant();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (username != null && authentication == null) {

                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            persistTenantIdInContext(jwt, request);
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            handlerExceptionResolver.resolveException(request, response, null, exception);
        } finally {
            TenantContext.clear();
        }
    }

    private void persistTenantIdInContext(String accessToken, HttpServletRequest request) {

        String requestMapping = extractRequestMapping(request.getRequestURI());

        if (!SYSTEM_BASE_PATHS.contains(requestMapping)) {
            TenantContext.clear();
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(accessToken)
                    .getBody();

            String tenantId = claims.get("tenantId", String.class); // Extract tenantId
            if (tenantId == null || tenantId.isBlank()) {
                String headerTenant = request.getHeader("X-Tenant-ID");
                if (headerTenant == null || headerTenant.isBlank()) {
                    headerTenant = request.getHeader("X-Tenant-Id");
                }
                tenantId = headerTenant;
            }

            // Set the tenant in context (if applicable)
            if (tenantId != null && !tenantId.isBlank()) {
                TenantContext.setCurrentTenant(tenantId);
            }
        }
    }

    private String extractRequestMapping(String uri) {
        if (uri == null || uri.isEmpty()) {
            return "";
        }
        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }
        int nextSlashIndex = uri.indexOf("/");
        if (nextSlashIndex != -1) {
            return uri.substring(0, nextSlashIndex);  // Extract the first part
        } else {
            return uri;  // If there is no additional "/", return the whole string
        }
    }
}
