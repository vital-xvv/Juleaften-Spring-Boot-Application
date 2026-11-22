package info.app.juleaften.exception.exceptions;

public class VisitNotFoundException extends IllegalArgumentException {
    public VisitNotFoundException(String email, String eventName) {
        super("The registration for event \"" + eventName + "\" is not found for user " + email + ".");
    }
}
