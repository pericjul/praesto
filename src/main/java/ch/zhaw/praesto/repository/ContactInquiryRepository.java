package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.ContactInquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactInquiryRepository extends JpaRepository<ContactInquiry, String> {

    List<ContactInquiry> findAllByOrderByCreatedAtDesc();
}
