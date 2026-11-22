package info.app.juleaften.dto.request;

import info.app.juleaften.model.dictionary.Companionship;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterVisitRequest(@NotBlank String firstName,
                                   @NotBlank String lastName,
                                   @NotBlank @Email String email,
                                   @NotNull Companionship companionship,
                                   int companionCount,
                                   String note,
                                   @NotBlank String eventName) {
}
