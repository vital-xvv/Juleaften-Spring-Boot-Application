package info.app.juleaften.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(@Min(0) long userProfileId,
                                    @NotBlank String oldPassword,
                                    @NotBlank String newPassword) {
}
