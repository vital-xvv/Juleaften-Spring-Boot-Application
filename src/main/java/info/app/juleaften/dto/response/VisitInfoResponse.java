package info.app.juleaften.dto.response;

import info.app.juleaften.model.Event;
import info.app.juleaften.model.dictionary.Companionship;
import info.app.juleaften.model.dictionary.VisitStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VisitInfoResponse {
    private String note;
    private Companionship companionship;
    private int companyCount;
    private VisitStatus status;
    private LocalDateTime registrationDate;
    private Event event;
}
