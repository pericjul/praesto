package ch.zhaw.praesto.model;

import lombok.Data;

@Data
public class UpdateAssignmentStatusRequest {
    private AssignmentStatus status;
}