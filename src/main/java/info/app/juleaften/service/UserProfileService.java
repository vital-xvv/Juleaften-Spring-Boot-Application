package info.app.juleaften.service;

import info.app.juleaften.config.HttpConfig;
import info.app.juleaften.dict.AuthenticationMethod;
import info.app.juleaften.dto.request.ChangePasswordRequest;
import info.app.juleaften.model.User;
import info.app.juleaften.model.UserProfile;
import info.app.juleaften.notification.EmailNotificationService;
import info.app.juleaften.repository.UserProfileRepository;
import info.app.juleaften.repository.UserRepository;
import info.app.juleaften.util.PasswordGenerator;
import info.app.juleaften.util.VerificationTokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailNotificationService emailNotificationService;
    private final TemplateEngine templateEngine;
    private final VerificationTokenGenerator tokenGenerator;
    private final HttpConfig httpConfig;

    @Value("${password.auto.size}")
    private int DEFAULT_PASSWORD_SIZE;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileService.class);

    public UserProfileService(UserProfileRepository userProfileRepository,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              EmailNotificationService emailNotificationService,
                              TemplateEngine templateEngine,
                              VerificationTokenGenerator tokenGenerator, HttpConfig httpConfig) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailNotificationService = emailNotificationService;
        this.templateEngine = templateEngine;
        this.tokenGenerator = tokenGenerator;
        this.httpConfig = httpConfig;
    }

    @Transactional
    public UserProfile getOrSaveUserProfile(String firstName, String lastName, String email) {
        return userProfileRepository.findByUser_Email(email).orElseGet(() -> {
            User newUser = new User(firstName,lastName, email);
            UserProfile newUserProfile = new UserProfile();
            newUserProfile.setUser(newUser);
            String generatedPassword = PasswordGenerator.generate(DEFAULT_PASSWORD_SIZE);
            LOGGER.debug("Generated password for user {}: {}", newUser.getEmail(), generatedPassword);

            newUserProfile.setPassword(passwordEncoder.encode(generatedPassword));
            newUserProfile.setSignUpMethod(AuthenticationMethod.LOCAL);
            newUserProfile.setVerified(false);
            newUserProfile.setActive(false);

            LOGGER.info("User {} {} has been created", firstName, lastName);
            sendAccountVerificationEmail(newUserProfile, generatedPassword);
            return newUserProfile;
        });
    }

    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        Optional<UserProfile> userProfile = userProfileRepository.findById(changePasswordRequest.userProfileId());
        if (userProfile.isPresent() && userProfile.get().isUserProfileValid() && passwordEncoder.matches(changePasswordRequest.oldPassword(), userProfile.get().getPassword())) {
            userProfile.get().setPassword(passwordEncoder.encode(changePasswordRequest.newPassword()));
            userProfile.get().setUpdatedAt(LocalDateTime.now());
            userProfileRepository.save(userProfile.get());
            LOGGER.info("Password for {} {} has been changed successfully", userProfile.get().getUser().getFirstName(), userProfile.get().getUser().getLastName());
        } else {
            throw new IllegalArgumentException("Password change failed. Invalid credentials.");
        }
    }

    @Transactional
    public void provisionOAuth2User(String email, String firstName, String lastName) {
        userProfileRepository.findByUser_Email(email).orElseGet(() -> {
            User newUser = new User(firstName, lastName, email);
            userRepository.save(newUser);

            UserProfile newUserProfile = new UserProfile();
            newUserProfile.setUser(newUser);
            newUserProfile.setPassword(passwordEncoder.encode(PasswordGenerator.generate(DEFAULT_PASSWORD_SIZE)));
            newUserProfile.setSignUpMethod(AuthenticationMethod.GOOGLE);
            newUserProfile.setVerified(true);
            newUserProfile.setActive(true);
            userProfileRepository.save(newUserProfile);
            LOGGER.info("Google user {} {} has been created", firstName, lastName);
            return newUserProfile;
        });
    }


    @Transactional
    public String verifyEmail(String token) {
        String tokenHash = tokenGenerator.hashToken(token);
        UserProfile userProfile = userProfileRepository.findByVerificationTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token."));

        if (userProfile.getVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification token has expired. Please request a new verification email.");
        }

        userProfile.setVerified(true);
        userProfile.setActive(true);

        // Invalidate the token
        userProfile.setVerificationTokenHash(null);
        userProfile.setVerificationTokenExpiresAt(null);

        userProfileRepository.save(userProfile);

        return userProfile.getUser().getEmail();
    }

    @Transactional
    public void sendAccountVerificationEmail(UserProfile userProfile, String generatedPassword) {
        try {
            String token = tokenGenerator.generateToken();
            userProfile.setVerificationTokenHash(tokenGenerator.hashToken(token));
            userProfile.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(24));
            userProfileRepository.save(userProfile);

            Context context = new Context();
            context.setVariable("recipientName", "%s %s".formatted(userProfile.getUser().getFirstName(), userProfile.getUser().getLastName()));
            context.setVariable("verificationUrl", httpConfig.getClientBaseUrl() + "/verify-email?token=" + token);
            context.setVariable("expiryHours", 24);
            context.setVariable("password", generatedPassword);

            String content = templateEngine.process("/email_templates/email_verify.html", context);

            emailNotificationService.sendMessage(userProfile.getUser().getEmail(), "Email Verification", content, true);
        } catch (Exception e) {
            LOGGER.error("Error sending email verification to {}: {}", userProfile.getUser().getEmail(), e.getMessage());
        }
    }

    public Optional<UserProfile> findByEmail(String email) {
        return userProfileRepository.findByUser_Email(email);
    }
}
