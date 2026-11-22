package info.app.juleaften.security.oauth2;

import info.app.juleaften.service.UserProfileService;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class GoogleOidcUserService extends OidcUserService {
    private final UserProfileService userProfileService;

    public GoogleOidcUserService(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Override
    public OidcUser loadUser(@NonNull OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String email = oidcUser.getEmail();
        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_not_found"),
                    "Google did not provide an email address");
        }

        userProfileService.provisionOAuth2User(email, resolveFirstName(oidcUser, email), resolveLastName(oidcUser));
        return oidcUser;
    }

    private static String resolveFirstName(OidcUser oidcUser, String email) {
        if (StringUtils.hasText(oidcUser.getGivenName())) {
            return oidcUser.getGivenName();
        }
        if (StringUtils.hasText(oidcUser.getFullName())) {
            return oidcUser.getFullName().split(" ", 2)[0];
        }
        return email;
    }

    private static String resolveLastName(OidcUser oidcUser) {
        if (StringUtils.hasText(oidcUser.getFamilyName())) {
            return oidcUser.getFamilyName();
        }
        if (StringUtils.hasText(oidcUser.getFullName())) {
            String[] parts = oidcUser.getFullName().split(" ", 2);
            if (parts.length > 1 && StringUtils.hasText(parts[1])) {
                return parts[1];
            }
        }
        return "-";
    }
}
