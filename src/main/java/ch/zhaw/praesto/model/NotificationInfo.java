package ch.zhaw.praesto.model;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationInfo {
    private String type;
    private String icon;
    private String title;
    private String message;
    private String assignmentId;
    private Integer grade; 
    private Instant createdAt;
}