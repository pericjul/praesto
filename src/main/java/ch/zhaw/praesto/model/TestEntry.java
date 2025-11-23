package ch.zhaw.praesto.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "testEntries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestEntry {
    @Id
    private String id;

    private String text;
}