package info.app.juleaften.service;

import info.app.juleaften.dto.request.RegisterVisitRequest;
import info.app.juleaften.dto.response.EventStatistics;
import info.app.juleaften.dto.response.VisitInfoResponse;
import info.app.juleaften.exception.exceptions.VisitNotFoundException;
import info.app.juleaften.model.Visit;
import info.app.juleaften.model.Event;
import info.app.juleaften.model.UserProfile;
import info.app.juleaften.model.dictionary.VisitStatus;
import info.app.juleaften.model.dictionary.Companionship;
import info.app.juleaften.repository.VisitRepository;
import info.app.juleaften.repository.EventRepository;
import info.app.juleaften.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VisitService {
    private final VisitRepository visitRepository;
    private final EventRepository eventRepository;
    private final UserProfileService userProfileService;
    private final SecurityService securityService;

    private final static Logger LOGGER = LoggerFactory.getLogger(VisitService.class);
    private final EventService eventService;

    public VisitService(VisitRepository visitRepository,
                        EventRepository eventRepository,
                        UserProfileService userProfileService, SecurityService securityService, EventService eventService) {
        this.visitRepository = visitRepository;
        this.eventRepository = eventRepository;
        this.userProfileService = userProfileService;
        this.securityService = securityService;
        this.eventService = eventService;
    }

    @Transactional
    public void registerVisit(RegisterVisitRequest request) {
        Optional<Event> event = eventRepository.findByName(request.eventName());
        if (event.isEmpty() || !isCompanionshipValid(request.companionship(), request.companionCount())) {
            LOGGER.debug("Event or companionship not found for event name \"{}\" or companionCount {} is wrong", request.companionCount(), request.eventName());
            return;
        }

        Optional<UserProfile> userProfile = Optional.empty();
        if (securityService.isAuthenticated()) {
            userProfile = securityService.getCurrentUserProfile();
        }

        if (userProfile.isEmpty()) {
            userProfile = Optional.of(userProfileService
                    .getOrSaveUserProfile(request.firstName(), request.lastName(), request.email()));
        }

        Optional<Visit> existingVisit = visitRepository
                .findByEventNameAndUserProfile_User_Email(request.eventName(), userProfile.get().getUser().getEmail());
        if (existingVisit.isPresent()) {
            LOGGER.error("User {} {} has already registered for event {}",
                    userProfile.get().getUser().getFirstName(), userProfile.get().getUser().getLastName(), request.eventName());
            return;
        }

        Visit visit = new Visit();
        visit.setCompanionship(request.companionship());
        visit.setCompanionsCount(request.companionCount());
        visit.setEvent(event.get());
        visit.setUserProfile(userProfile.get());
        visit.setNote(request.note());
        visit.setCreatedAt(LocalDateTime.now());
        visit.setStatus(resolveVisitStatus(userProfile.get(), request.eventName()));
        visitRepository.save(visit);
    }

    private VisitStatus resolveVisitStatus(UserProfile userProfile, String eventName) {
        EventStatistics eventStatistics = eventService.getEventStatistics(eventName);
        if (eventStatistics.isFull()) {
            return VisitStatus.WAITING;
        } else {
            return VisitStatus.REGISTERED;
        }
    }

    @Transactional
    public void updateVisitStatusByUserProfile(UserProfile userProfile) {
        List<Visit> visitList = visitRepository.findVisitByUserProfile_User_EmailAndStatusNotOrderByIdAscCreatedAtAsc(userProfile.getUsername(), VisitStatus.REGISTERED);
        visitList.forEach(visit -> {
            visit.setStatus(resolveVisitStatus(userProfile, visit.getEvent().getName()));
            visitRepository.save(visit);
        });
        LOGGER.info("Visits' statuses for user {} has been updated", userProfile.getUsername());
    }

    private boolean isCompanionshipValid(Companionship companionship, int numberOfCompanionship) {
        return switch (companionship) {
            case FAMILY, MIXED, FRIENDS -> numberOfCompanionship >= 1;
            case SINGLE, COUPLE -> numberOfCompanionship == 0;
        } && numberOfCompanionship <= 9;
    }

    public VisitInfoResponse findVisitByEventName(String eventName, UserProfile userProfile) {
        Visit visit = visitRepository.findByEventNameAndUserProfile_User_Email(eventName, userProfile.getUser().getEmail())
                .orElseThrow(() -> new VisitNotFoundException(userProfile.getUsername(), eventName));

        VisitInfoResponse visitInfoResponse = new VisitInfoResponse();
        visitInfoResponse.setEvent(visit.getEvent());
        visitInfoResponse.setCompanionship(visit.getCompanionship());
        visitInfoResponse.setCompanyCount(visit.getCompanionsCount());
        visitInfoResponse.setNote(visit.getNote());
        visitInfoResponse.setStatus(visit.getStatus());
        visitInfoResponse.setRegistrationDate(visit.getCreatedAt());

        return visitInfoResponse;
    }
}
