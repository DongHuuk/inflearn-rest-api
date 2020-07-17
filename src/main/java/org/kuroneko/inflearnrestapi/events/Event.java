package org.kuroneko.inflearnrestapi.events;

import lombok.*;
import org.kuroneko.inflearnrestapi.account.Account;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder @AllArgsConstructor
@NoArgsConstructor @Getter @Setter @EqualsAndHashCode(of = "id")
public class Event {

    @Id @GeneratedValue
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    private String location; // (optional)이게 없으면 온라인 모임
    private int basePrice; // (optional)
    private int maxPrice; // (optional)
    private int limitOfEnrollment;
    private boolean offline;
    private boolean free;

    @ManyToOne
    private Account manager;
    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus = EventStatus.DRAFT;

    public void freeUpdate() {
        if (this.basePrice == 0 && this.maxPrice == 0) {
            this.free = true;
        } else {
            this.free = false;
        }
    }

    public void offlineUpdate() {
        if (this.location == null || this.location.isBlank()) {
            this.offline = false;
        }else {
            this.offline = true;
        }
    }
}
