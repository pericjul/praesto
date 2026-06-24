package ch.zhaw.praesto.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Eine Schule = ein Mandant. Vollständige Datenisolation erfolgt über die
 * {@code schoolId}, die als Pflichtfeld in allen mandantenbezogenen Collections steht.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "schools")
public class School {

    @Id
    private String id;

    private String name;          // "Kantonsschule Aarau"
    private String canton;        // "AG"
    private String city;          // "Aarau"

    @Builder.Default
    private boolean isActive = true;   // deaktivierbar durch SuperAdmin

    private Instant createdAt;
}
