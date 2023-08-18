package example.security;

import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Represents the security context.
 * <p>
 * For testing purposes only, this is a cookie with the name user.
 * <p>
 * This can be set using the developer tools javascript console.
 * <p>
 * document.cookie="user=username"
 */
@RequestScoped
public class SecurityContext {
    @Inject
    HttpServletRequest request;

    public Principal getCallerPrincipal() {
        if (request.getCookies() != null) {
            Optional<Cookie> result = Arrays.stream(request.getCookies())
                    .filter(cookie -> "user".equals(cookie.getName()))
                    .findFirst();
            if (result.isPresent()) {
                return new User(result.get().getValue());
            }
        }
        return null;
    }
}
