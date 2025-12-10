package ch.zhaw.praesto.model;

import lombok.Data;

import java.util.List;

@Data
public class SchoolClassDTO {
    private String name;
    private List<String> studentIds;
}