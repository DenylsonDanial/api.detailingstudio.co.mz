package mz.co.amaterasu.gestao.util;

import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Verificacoes de permissao que dependem dos dados do pedido (ex.: ajuste de
 * preco so quando o valor difere do catalogo) e que por isso nao cabem num
 * {@code @PreAuthorize}.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "system";
        }
        return auth.getName();
    }

    public static boolean hasAuthority(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(granted -> authority.equals(granted.getAuthority()));
    }

    public static void require(String authority, String message) {
        if (!hasAuthority(authority)) {
            throw new ApiRequestException(message);
        }
    }

    public static String requireReason(String reason, String message) {
        if (reason == null || reason.isBlank()) {
            throw new ApiRequestException(message);
        }
        return reason.trim();
    }
}
