package mz.co.amaterasu.gestao.base.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.amaterasu.gestao.base.dto.AuthRequests;
import mz.co.amaterasu.gestao.base.dto.AuthResponse;
import mz.co.amaterasu.gestao.base.entity.PasswordResetToken;
import mz.co.amaterasu.gestao.base.entity.RefreshToken;
import mz.co.amaterasu.gestao.base.entity.UserTenant;
import mz.co.amaterasu.gestao.base.jwt.JwtService;
import mz.co.amaterasu.gestao.base.repository.PasswordResetTokenRepository;
import mz.co.amaterasu.gestao.base.repository.RefreshTokenRepository;
import mz.co.amaterasu.gestao.base.service.EmailService;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import mz.co.amaterasu.gestao.user.annotaion.MasterDB;
import mz.co.amaterasu.gestao.user.entity.User;
import mz.co.amaterasu.gestao.user.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;
    private static final int RESET_TOKEN_MINUTES = 30;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${security.jwt.refresh-expiration-time}")
    private long refreshExpirationMs;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    // -------------------------------------------------------------- login ---

    @MasterDB
    @Transactional
    public AuthResponse login(AuthRequests.Login request, HttpServletRequest httpRequest) {
        User user = findByUsernameOrEmail(request.getUsername())
                .orElseThrow(() -> new ApiRequestException("Credenciais invalidas"));

        if (!user.isAccountNonLocked()) {
            throw new ApiRequestException(
                    "Conta bloqueada por tentativas falhadas. Tente novamente dentro de alguns minutos.");
        }

        try {
            // O UserDetailsService carrega por username; se o pedido veio com email,
            // autenticamos com o username real da conta.
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword()));
        } catch (BadCredentialsException e) {
            registerFailedAttempt(user);
            throw new ApiRequestException("Credenciais invalidas");
        } catch (DisabledException e) {
            throw new ApiRequestException("Utilizador desactivado. Contacte o administrador.");
        } catch (LockedException e) {
            throw new ApiRequestException("Conta bloqueada. Contacte o administrador.");
        }

        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String tenantId = resolveTenant(user, request.getTenantId());
        return buildAuthResponse(user, tenantId, httpRequest);
    }

    private Optional<User> findByUsernameOrEmail(String login) {
        if (StringUtils.isBlank(login)) {
            return Optional.empty();
        }
        Optional<User> byUsername = userRepository.findByUsername(login.trim());
        if (byUsername.isPresent()) {
            return byUsername;
        }
        return userRepository.findByEmailIgnoreCase(login.trim());
    }

    private void registerFailedAttempt(User user) {
        int attempts = user.getFailedLoginCount() + 1;
        user.setFailedLoginCount(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(Instant.now().plus(LOCK_MINUTES, ChronoUnit.MINUTES));
            log.warn("Utilizador {} bloqueado apos {} tentativas falhadas", user.getUsername(), attempts);
        }
        userRepository.save(user);
    }

    private String resolveTenant(User user, String requestedTenant) {
        List<UserTenant> tenants = userRepository.findAllUserTenantsByUsername(user.getUsername());

        if (StringUtils.isNotBlank(requestedTenant)) {
            boolean allowed = tenants.stream().anyMatch(t -> requestedTenant.equals(t.getTenant()));
            if (!allowed) {
                throw new ApiRequestException("Nao tem acesso a esta empresa");
            }
            return requestedTenant;
        }

        return tenants.isEmpty() ? null : tenants.get(0).getTenant();
    }

    // ------------------------------------------------------------ refresh ---

    /**
     * Rotacao de refresh token: o token apresentado e revogado e emitido um novo.
     * Se for apresentado um token ja revogado, revogam-se todas as sessoes do
     * utilizador, por se assumir roubo de token.
     */
    @MasterDB
    @Transactional
    public AuthResponse refresh(AuthRequests.Refresh request, HttpServletRequest httpRequest) {
        String presentedToken = request.getRefreshToken();
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(presentedToken))
                .orElseThrow(() -> new ApiRequestException("Sessao invalida. Volte a autenticar-se."));

        if (stored.getRevokedAt() != null) {
            refreshTokenRepository.revokeAllForUser(stored.getUserId(), Instant.now());
            log.warn("Reutilizacao de refresh token detectada para o utilizador {}", stored.getUserId());
            throw new ApiRequestException("Sessao invalida. Volte a autenticar-se.");
        }

        if (!stored.isUsable()) {
            throw new ApiRequestException("Sessao expirada. Volte a autenticar-se.");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new ApiRequestException("Utilizador nao encontrado"));

        if (!user.isEnabled()) {
            throw new ApiRequestException("Utilizador desactivado. Contacte o administrador.");
        }

        stored.setRevokedAt(Instant.now());
        refreshTokenRepository.save(stored);

        String requestedTenant = StringUtils.isNotBlank(request.getTenantId())
                ? request.getTenantId()
                : stored.getActiveTenant();
        String tenantId = resolveTenant(user, requestedTenant);
        return buildAuthResponse(user, tenantId, httpRequest);
    }

    /**
     * Troca a empresa activa desta sessao: valida o acesso, grava o tenant no
     * refresh token e reemite o access token. O refresh token nao e rotacionado.
     */
    @MasterDB
    @Transactional
    public String switchCompany(String username, AuthRequests.SwitchCompany request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiRequestException("Utilizador nao encontrado"));

        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(request.getRefreshToken()))
                .orElseThrow(() -> new ApiRequestException("Sessao invalida. Volte a autenticar-se."));

        if (!user.getId().equals(stored.getUserId()) || !stored.isUsable()) {
            throw new ApiRequestException("Sessao invalida. Volte a autenticar-se.");
        }

        String tenantId = resolveTenant(user, request.getTenantId());
        stored.setActiveTenant(tenantId);
        refreshTokenRepository.save(stored);

        return jwtService.generateToken(user, tenantId);
    }

    @MasterDB
    @Transactional
    public void logout(String presentedToken) {
        refreshTokenRepository.findByTokenHash(hash(presentedToken)).ifPresent(token -> {
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
        });
    }

    // --------------------------------------------------- palavra-passe ------

    /**
     * Nao revela se o email existe: responde sempre com sucesso, para nao permitir
     * enumeracao de contas.
     */
    @MasterDB
    @Transactional
    public void forgotPassword(String email) {
        Optional<User> maybeUser = userRepository.findByEmailIgnoreCase(email);
        if (maybeUser.isEmpty()) {
            log.info("Pedido de recuperacao para email desconhecido: {}", email);
            return;
        }

        User user = maybeUser.get();
        passwordResetTokenRepository.invalidateAllForUser(user.getId(), Instant.now());

        String rawToken = generateToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(user.getId());
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(Instant.now().plus(RESET_TOKEN_MINUTES, ChronoUnit.MINUTES));
        token.setCreatedAt(Instant.now());
        passwordResetTokenRepository.save(token);

        String resetUrl = frontendBaseUrl + "/auth/reset-password?token=" + rawToken;
        emailService.send(user.getEmail(), "Recuperacao de palavra-passe", "reset-password", Map.of(
                "name", StringUtils.defaultIfBlank(user.getFirstName(), user.getUsername()),
                "companyName", "Detailing Studio",
                "resetUrl", resetUrl,
                "expiryMinutes", RESET_TOKEN_MINUTES
        ));
    }

    @MasterDB
    @Transactional
    public void resetPassword(AuthRequests.ResetPassword request) {
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new ApiRequestException("As palavras-passe nao coincidem");
        }

        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hash(request.getToken()))
                .orElseThrow(() -> new ApiRequestException("Link invalido ou ja utilizado"));

        if (!token.isUsable()) {
            throw new ApiRequestException("Link expirado. Peca uma nova recuperacao.");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ApiRequestException("Utilizador nao encontrado"));

        applyNewPassword(user, request.getPassword());

        token.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(token);
    }

    @MasterDB
    @Transactional
    public void changePassword(String username, AuthRequests.ChangePassword request) {
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new ApiRequestException("As palavras-passe nao coincidem");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiRequestException("Utilizador nao encontrado"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ApiRequestException("A palavra-passe actual esta incorrecta");
        }

        applyNewPassword(user, request.getPassword());
    }

    /**
     * Primeiro acesso: so e permitido enquanto passwordChangedAt for null.
     * Revoga as sessoes anteriores e devolve um par de tokens novo para o
     * cliente nao ser expulso imediatamente.
     */
    @MasterDB
    @Transactional
    public AuthResponse setInitialPassword(String username, AuthRequests.SetPassword request,
                                           String tenantId, HttpServletRequest httpRequest) {
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new ApiRequestException("As palavras-passe nao coincidem");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiRequestException("Utilizador nao encontrado"));

        if (user.getPasswordChangedAt() != null) {
            throw new ApiRequestException("A palavra-passe definitiva ja foi definida.");
        }

        applyNewPassword(user, request.getPassword());
        return buildAuthResponse(user, tenantId, httpRequest);
    }

    private void applyNewPassword(User user, String rawPassword) {
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPasswordChangedAt(Instant.now());
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        // Toda a sessao anterior deixa de ser valida apos mudanca de palavra-passe.
        refreshTokenRepository.revokeAllForUser(user.getId(), Instant.now());
    }

    // ------------------------------------------------------------ helpers ---

    private AuthResponse buildAuthResponse(User user, String tenantId, HttpServletRequest httpRequest) {
        String accessToken = jwtService.generateToken(user, tenantId);
        String rawRefreshToken = generateToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenHash(hash(rawRefreshToken));
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setActiveTenant(tenantId);
        if (httpRequest != null) {
            refreshToken.setUserAgent(StringUtils.abbreviate(httpRequest.getHeader("User-Agent"), 255));
            refreshToken.setIpAddress(clientIp(httpRequest));
        }
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .expiresIn(jwtService.getExpirationTime())
                .username(user.getUsername())
                .name(user.getName())
                .tenantId(tenantId)
                .authorities(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .mustChangePassword(user.getPasswordChangedAt() == null)
                .build();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(forwarded)) {
            return StringUtils.abbreviate(forwarded.split(",")[0].trim(), 64);
        }
        return StringUtils.abbreviate(request.getRemoteAddr(), 64);
    }

    private String generateToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }
}
