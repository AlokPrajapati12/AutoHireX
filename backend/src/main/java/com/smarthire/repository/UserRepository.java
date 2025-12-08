package com.smarthire.repository;

import com.smarthire.model.User;
import com.smarthire.model.User.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // ✔ Spring Data will auto-implement this method
    Optional<User> findByEmail(String email);

    // ✔ This is fine
    List<User> findByRole(UserRole role);

    // ✔ This is fine
    boolean existsByEmail(String email);
}
