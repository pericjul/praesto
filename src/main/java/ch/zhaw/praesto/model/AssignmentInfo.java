package ch.zhaw.praesto.model;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentInfo {
    private String id;
    private String title;
    private String type;
    private Instant dueDate;
    private Integer durationMin;
}