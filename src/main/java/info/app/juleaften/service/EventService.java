package info.app.juleaften.service;

import info.app.juleaften.dto.response.EventStatistics;
import info.app.juleaften.exception.exceptions.EventNotFoundException;
import info.app.juleaften.model.Event;
import info.app.juleaften.model.Visit;
import info.app.juleaften.model.dictionary.VisitStatus;
import info.app.juleaften.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public EventStatistics getEventStatistics(String eventName) {
        Event event = eventRepository.findByName(eventName).orElseThrow(() -> new EventNotFoundException(eventName));

        List<Visit> registeredVisits = event.getVisit().stream().filter(v -> v.getStatus() == VisitStatus.REGISTERED).toList();

        EventStatistics eventStatistics = new EventStatistics();
        eventStatistics.setCapacity(event.getCapacity());
        eventStatistics.setRegisteredVisitsCount(registeredVisits.size());
        eventStatistics.setFilledCapacity(registeredVisits.size() + registeredVisits.stream().map(Visit::getCompanionsCount).reduce(0, Integer::sum));

        int filledPercent = (int) (eventStatistics.getFilledCapacity() / (double) eventStatistics.getCapacity() * 100);
        eventStatistics.setFilledPercentage(filledPercent);
        eventStatistics.setFull(filledPercent == 100);

        return eventStatistics;
    }
}
