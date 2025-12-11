package ch.zhaw.praesto.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_badges")
@CompoundIndex(name = "student_badge_idx", def = "{'studentId': 1, 'badgeId': 1}", unique = true)
public class UserBadge {

    @Id
    private String id;

    private String studentId;   // User ID
    private String badgeId;     // Referenz zum Badge
    
    private Instant earnedAt;   // Wann verdient
}