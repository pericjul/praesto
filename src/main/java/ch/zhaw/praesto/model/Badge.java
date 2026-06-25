package ch.zhaw.praesto.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "badges")
public class Badge {

    @Id
    private String id;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    private String icon;
    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private BadgeRuleType ruleType;

    private int threshold;
    private int sortOrder;
}
