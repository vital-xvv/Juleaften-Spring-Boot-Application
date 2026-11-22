package info.app.juleaften.dto.response;

import info.app.juleaften.dict.UserRole;

public record UserProfileResponse(String firstName, String lastName, String email, UserRole role) {
}
