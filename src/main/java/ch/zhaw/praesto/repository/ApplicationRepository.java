package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.Application;
import ch.zhaw.praesto.model.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, String> {

    List<Application> findByStudentId(String studentId);

    List<Application> findByStudentIdOrderByCreatedAtDesc(String studentId);

    List<Application> findByStudentIdAndStatus(String studentId, ApplicationStatus status);

    long countByStudentIdAndStatus(String studentId, ApplicationStatus status);

    long countByStudentId(String studentId);

    boolean existsByStudentIdAndStatus(String studentId, ApplicationStatus status);

    void deleteByStudentIdIn(java.util.Collection<String> studentIds);

}