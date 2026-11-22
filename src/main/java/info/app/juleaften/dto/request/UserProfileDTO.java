package info.app.juleaften.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserProfileDTO(@NotBlank String firstName,
                             @NotBlank String lastName,
                             @NotBlank @Email String email) {
}
