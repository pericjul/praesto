package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public Optional<User> findByAuth0Id(String auth0Id) {
        return repo.findByAuth0Id(auth0Id);
    }
}