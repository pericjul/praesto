package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.ContactInquiry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ContactInquiryRepository extends MongoRepository<ContactInquiry, String> {

    List<ContactInquiry> findAllByOrderByCreatedAtDesc();
}
