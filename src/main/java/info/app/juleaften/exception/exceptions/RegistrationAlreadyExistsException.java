package info.app.juleaften.exception.exceptions;

import info.app.juleaften.exception.ExceptionCode;
import info.app.juleaften.exception.ExceptionCodeAware;

public class RegistrationAlreadyExistsException extends RuntimeException implements ExceptionCodeAware {
    public RegistrationAlreadyExistsException(String email) {
        super("Registration for email " + email + " already exists.");
    }

    @Override
    public ExceptionCode getExceptionCode() {
        return ExceptionCode.REGISTRATION_ALREADY_EXISTS;
    }
}
