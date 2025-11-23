package ch.zhaw.praesto.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private String id;

    private String auth0Id;
    private String email;
    private String name;
    private String role;   // STUDENT oder TEACHER
    private String classId; // optional bei TEACHER leer
}