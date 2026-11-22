package info.app.juleaften.controller;

import info.app.juleaften.dto.request.RegisterVisitRequest;
import info.app.juleaften.dto.response.ApiResponse;
import info.app.juleaften.dto.response.VisitInfoResponse;
import info.app.juleaften.model.UserProfile;
import info.app.juleaften.service.VisitService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/visits")
public class VisitController {
    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> registerVisit(@Valid @RequestBody RegisterVisitRequest request) {
        visitService.registerVisit(request);
        return ResponseEntity.ok(new ApiResponse<>("Visit registered successfully."));
    }


    @GetMapping("/{eventName}")
    public ResponseEntity<ApiResponse<VisitInfoResponse>> getAvailableVisit(@NotBlank @PathVariable String eventName,
                                                                @AuthenticationPrincipal UserProfile userProfile) {
        VisitInfoResponse visitInfo = visitService.findVisitByEventName(eventName, userProfile);
        return ResponseEntity.ok(new ApiResponse<>("The registration has been retrieved successfully.", visitInfo));
    }
}
