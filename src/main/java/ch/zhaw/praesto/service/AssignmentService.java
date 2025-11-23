package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.Assignment;
import ch.zhaw.praesto.model.AssignmentStatus;
import ch.zhaw.praesto.repository.AssignmentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AssignmentService {

    private final AssignmentRepository repo;

    public AssignmentService(AssignmentRepository repo) {
        this.repo = repo;
    }

    public Assignment createAssignment(Assignment assignment, String teacherId) {
        assignment.setId(null);
        assignment.setCreatedByTeacherId(teacherId);
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        assignment.setCreatedAt(Instant.now());
        return repo.save(assignment);
    }

    public Assignment updateStatus(String assignmentId, AssignmentStatus status) {
        Assignment a = repo.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        a.setStatus(status);
        return repo.save(a);
    }

    public List<Assignment> getAssignmentsForClass(String classId) {
        return repo.findByClassId(classId);
    }
}