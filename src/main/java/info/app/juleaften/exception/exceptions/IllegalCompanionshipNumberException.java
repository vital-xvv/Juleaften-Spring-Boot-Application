package info.app.juleaften.exception.exceptions;

import info.app.juleaften.exception.ExceptionCode;
import info.app.juleaften.exception.ExceptionCodeAware;
import info.app.juleaften.model.dictionary.Companionship;

public class IllegalCompanionshipNumberException extends IllegalArgumentException implements ExceptionCodeAware {


    public IllegalCompanionshipNumberException(Companionship companionship, int number) {
        super("Illegal number of " + companionship.name() + " companions: " + number + ".");
    }

    @Override
    public ExceptionCode getExceptionCode() {
        return ExceptionCode.REGISTRATION_ILLEGAL_PARTY_COUNT;
    }
}
