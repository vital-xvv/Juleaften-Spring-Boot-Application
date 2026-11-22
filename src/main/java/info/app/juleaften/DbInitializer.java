package info.app.juleaften;

import info.app.juleaften.model.Event;
import info.app.juleaften.repository.EventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DbInitializer implements CommandLineRunner {
    private final EventRepository eventRepository;

    public DbInitializer(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        //Create default event
        String defaultEventName = "Juleaften";
        if (eventRepository.findByName(defaultEventName).isEmpty()) {
            Event event = new Event();
            event.setCapacity(50);
            event.setName(defaultEventName);
            event.setDescription("Heart-warming gathering for ukraininas in FriKirken.");
            eventRepository.save(event);
        }

    }
}
