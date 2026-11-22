package info.app.juleaften.security.oauth2;

import info.app.juleaften.config.HttpConfig;
import info.app.juleaften.security.SecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final SecurityService securityService;
    private final HttpConfig httpConfig;

    public OAuth2AuthenticationSuccessHandler(SecurityService securityService, HttpConfig httpConfig) {
        this.securityService = securityService;
        this.httpConfig = httpConfig;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        @NonNull Authentication authentication) throws IOException {
        String email = extractEmail(authentication);
        ResponseCookie cookie = securityService.provideAuthenticationCookie(email);

        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );

        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(httpConfig.getClientBaseUrl());
    }

    private static String extractEmail(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            return oidcUser.getEmail();
        }
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            return oauth2User.getAttribute("email");
        }
        throw new IllegalStateException("Unsupported OAuth2 principal type");
    }
}
