package info.app.juleaften.security;

import info.app.juleaften.config.HttpConfig;
import info.app.juleaften.jwt.JWTService;
import info.app.juleaften.model.UserProfile;
import info.app.juleaften.service.UserProfileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@Service
public class SecurityService {
    private final UserProfileService userProfileService;
    private final JWTService jwtService;
    private final HttpConfig httpConfig;

    public final static String ACCESS_TOKEN_COOKIE_NAME = "ACCESS_TOKEN";

    public SecurityService(UserProfileService userProfileService, JWTService jwtService, HttpConfig httpConfig) {
        this.userProfileService = userProfileService;
        this.jwtService = jwtService;
        this.httpConfig = httpConfig;
    }

    @Value("${jwt.expiration.millis}")
    private int EXPIRATION_TIME;

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null || authentication instanceof AnonymousAuthenticationToken || !((UserDetails)authentication.getPrincipal()).isAccountNonLocked()) {
            return false;
        }
        return userProfileService.findByEmail(((UserDetails)authentication.getPrincipal()).getUsername()).isPresent();
    }

    public Optional<UserProfile> getCurrentUserProfile() {
        return Optional.of((UserProfile) Objects.requireNonNull(Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal()));
    }

    public ResponseCookie provideAuthenticationCookie(String email) {
        String token = jwtService.generateToken(email);
        return ResponseCookie
                .from(ACCESS_TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(httpConfig.isSecure()) // true in production with HTTPS
                .path("/")
                .sameSite(httpConfig.getSameSite())
                .maxAge(Duration.ofMillis(EXPIRATION_TIME).getSeconds())
                .build();
    }

    public ResponseCookie provideLogoutCookie() {
        return ResponseCookie
                .from(ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(httpConfig.isSecure())
                .sameSite(httpConfig.getSameSite())
                .path("/")
                .maxAge(0)
                .build();
    }
}
