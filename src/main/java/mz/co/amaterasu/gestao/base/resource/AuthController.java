package mz.co.amaterasu.gestao.base.resource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.dto.AuthRequests;
import mz.co.amaterasu.gestao.base.dto.AuthResponse;
import mz.co.amaterasu.gestao.base.jwt.JwtService;
import mz.co.amaterasu.gestao.base.service.impl.AuthService;
import mz.co.amaterasu.gestao.user.dto.RequestSuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequests.Login request,
                                              HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody @Valid AuthRequests.Refresh request,
                                                HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.refresh(request, httpRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<RequestSuccessResponse> logout(@RequestBody @Valid AuthRequests.Refresh request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(new RequestSuccessResponse("Sessao terminada", HttpStatus.OK));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<RequestSuccessResponse> forgotPassword(
            @RequestBody @Valid AuthRequests.ForgotPassword request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(new RequestSuccessResponse("Se o endereco estiver registado, recebera um email com instrucoes.", HttpStatus.OK));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<RequestSuccessResponse> resetPassword(
            @RequestBody @Valid AuthRequests.ResetPassword request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new RequestSuccessResponse("Palavra-passe redefinida com sucesso", HttpStatus.OK));
    }

    @PostMapping("/change-password")
    public ResponseEntity<RequestSuccessResponse> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid AuthRequests.ChangePassword request) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(new RequestSuccessResponse("Palavra-passe alterada com sucesso", HttpStatus.OK));
    }

    /**
     * Primeiro acesso com palavra-passe temporaria. Exige JWT; nao pede a senha
     * actual; devolve um AuthResponse com tokens novos.
     */
    @PostMapping("/set-password")
    public ResponseEntity<AuthResponse> setPassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid AuthRequests.SetPassword request,
            HttpServletRequest httpRequest) {
        String token = authorization.replace("Bearer ", "");
        String tenantId = jwtService.extractTenantId(token);
        return ResponseEntity.ok(
                authService.setInitialPassword(userDetails.getUsername(), request, tenantId, httpRequest));
    }

    /** Troca a empresa activa desta sessao e reemite o access token com o novo tenantId. */
    @PutMapping("/switch-company")
    public ResponseEntity<String> switchCompany(@AuthenticationPrincipal UserDetails userDetails,
                                                @RequestBody @Valid AuthRequests.SwitchCompany request) {
        return ResponseEntity.ok(authService.switchCompany(userDetails.getUsername(), request));
    }
}
