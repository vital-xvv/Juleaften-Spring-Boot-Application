package info.app.juleaften.controller;

import info.app.juleaften.dto.request.AuthRequest;
import info.app.juleaften.dto.response.ApiResponse;
import info.app.juleaften.dto.response.UserProfileResponse;
import info.app.juleaften.model.User;
import info.app.juleaften.model.UserProfile;
import info.app.juleaften.security.SecurityService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final SecurityService securityService;

    public AuthenticationController(AuthenticationManager authenticationManager,
                                    SecurityService securityService) {
        this.authenticationManager = authenticationManager;
        this.securityService = securityService;
    }

    @PostMapping("")
    public ResponseEntity<?> generateToken(@Valid @RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password()));
        if (!authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
        ResponseCookie cookie = securityService.provideAuthenticationCookie(authRequest.email());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Authentication successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, UserProfileResponse>>> getCurrentUser(@AuthenticationPrincipal UserProfile userProfile) {
        if (userProfile == null || !userProfile.isAccountNonLocked()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>("User can not be authenticated."));
        }
        User user = userProfile.getUser();
        UserProfileResponse userProfileResponse = new UserProfileResponse(user.getFirstName(), user.getLastName(), user.getEmail(), userProfile.getRoles().getLast());
        return ResponseEntity.ok(new ApiResponse<>("User has been successfully authenticated.", Map.of("userProfile", userProfileResponse)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        session.invalidate();
        ResponseCookie cookie = securityService.provideLogoutCookie();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
