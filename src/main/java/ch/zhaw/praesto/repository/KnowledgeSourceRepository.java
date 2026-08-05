package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.KnowledgeSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeSourceRepository extends JpaRepository<KnowledgeSource, String> {

    List<KnowledgeSource> findAllByOrderByCreatedAtDesc();

    List<KnowledgeSource> findByEnabledTrue();
}
