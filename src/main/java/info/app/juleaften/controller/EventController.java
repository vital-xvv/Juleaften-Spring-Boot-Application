package info.app.juleaften.controller;

import info.app.juleaften.dto.request.CreateEventRequest;
import info.app.juleaften.dto.response.ApiResponse;
import info.app.juleaften.dto.response.EventStatistics;
import info.app.juleaften.dto.response.UserProfileResponse;
import info.app.juleaften.dto.response.VisitInfoResponse;
import info.app.juleaften.model.Event;
import info.app.juleaften.model.User;
import info.app.juleaften.model.UserProfile;
import info.app.juleaften.repository.EventRepository;
import info.app.juleaften.service.EventService;
import info.app.juleaften.service.VisitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventRepository eventRepository;
    private final EventService eventService;
    private final VisitService visitService;

    public EventController(EventRepository eventRepository, EventService eventService, VisitService visitService) {
        this.eventRepository = eventRepository;
        this.eventService = eventService;
        this.visitService = visitService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Event>> createEvent(@Valid @RequestBody CreateEventRequest requestBody) {
        Event event = new Event();
        event.setName(requestBody.name());
        event.setDescription(requestBody.description());
        event.setCapacity(requestBody.capacity());
        eventRepository.save(event);
        return ResponseEntity.ok(new ApiResponse<>("Event created successfully", event));
    }

    @GetMapping("/statistics/{eventName}")
    public ResponseEntity<ApiResponse<EventStatistics>> getEventStatistics(@PathVariable String eventName) {
        EventStatistics eventStatistics = eventService.getEventStatistics(eventName);
        return ResponseEntity.ok(new ApiResponse<>("Event capacity retrieved successfully.", eventStatistics));
    }

    @GetMapping("/user/detailed")
    public ResponseEntity<ApiResponse<Map<String, ?>>> getDetailedInfo(@AuthenticationPrincipal UserProfile userProfile, @RequestParam String eventName) {
        if (userProfile == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>("User can not be authenticated."));
        }
        User user = userProfile.getUser();
        UserProfileResponse userProfileResponse = new UserProfileResponse(user.getFirstName(), user.getLastName(), user.getEmail(), userProfile.getRoles().getLast());

        EventStatistics eventStatistics = eventService.getEventStatistics(eventName);
        VisitInfoResponse visitInfoResponse = visitService.findVisitByEventName(eventName, userProfile);
        return ResponseEntity.
                ok(new ApiResponse<>("User has been successfully authenticated.",
                        Map.of("userProfile", userProfileResponse,
                                "eventStatistics" , eventStatistics,
                                "visitInfo" , visitInfoResponse)));
    }

}
