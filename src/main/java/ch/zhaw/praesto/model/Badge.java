package ch.zhaw.praesto.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "badges")
public class Badge {

    @Id
    private String id;

    private String icon;        // Emoji: 🎯, 🔥, etc.
    private String title;       // z.B. "Erste Session"
    private String description; // z.B. "Dein erstes KI-Training absolviert!"
    
    private BadgeRuleType ruleType;  // Enum für die Regel
    private int threshold;           // Schwellwert (z.B. 5 für "5 Sessions")
    
    private int sortOrder;           // Für Sortierung in der Anzeige
}