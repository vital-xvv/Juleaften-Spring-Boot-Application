package info.app.juleaften.controller;

import info.app.juleaften.dto.request.ChangePasswordRequest;
import info.app.juleaften.dto.request.UserProfileDTO;
import info.app.juleaften.dto.response.ApiResponse;
import info.app.juleaften.model.UserProfile;
import info.app.juleaften.security.SecurityService;
import info.app.juleaften.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/profiles")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final SecurityService securityService;

    public UserProfileController(UserProfileService userProfileService, SecurityService securityService) {
        this.userProfileService = userProfileService;
        this.securityService = securityService;
    }

    @PostMapping()
    public ResponseEntity<ApiResponse<UserProfile>> register(@Valid @RequestBody UserProfileDTO userProfileDTO) {
        UserProfile userProfile = userProfileService
                .getOrSaveUserProfile(userProfileDTO.firstName(), userProfileDTO.lastName(), userProfileDTO.email());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("User has been created", userProfile));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest userProfileDTO) {
        userProfileService.changePassword(userProfileDTO);
        return ResponseEntity.ok(new ApiResponse<>("Password has been changed successfully"));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verify(@RequestBody Map<String, String> map) {
        String email = userProfileService.verifyEmail(map.get("token"));
        ResponseCookie cookie = securityService.provideAuthenticationCookie(email);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new ApiResponse<>("User has been verified successfully."));
    }


}
