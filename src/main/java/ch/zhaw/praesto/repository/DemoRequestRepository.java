package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.DemoRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DemoRequestRepository extends JpaRepository<DemoRequest, String> {

    List<DemoRequest> findAllByOrderByCreatedAtDesc();

    Optional<DemoRequest> findByApprovedSchoolId(String approvedSchoolId);
}
