package info.app.juleaften.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventStatistics {
    private String name;
    private long capacity;
    private long filledCapacity;
    private long registeredVisitsCount;
    private int filledPercentage; //ceiling
    private boolean isFull;
}
