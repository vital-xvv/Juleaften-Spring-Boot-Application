package info.app.juleaften.exception.exceptions;

public class EventNotFoundException extends IllegalArgumentException{
    public EventNotFoundException(String eventName) {
        super("Event \"" + eventName + "\" is not found.");
    }
}
