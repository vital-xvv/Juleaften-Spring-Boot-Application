package info.app.juleaften.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateEventRequest(@NotBlank String name, @NotBlank String description, @Min(2) int capacity) {
}
