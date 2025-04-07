package antifraud.repository;

import antifraud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Find user by username, ignoring case (for registration check and login)
    Optional<User> findByUsernameIgnoreCase(String username);

    // Find all users ordered by ID (for list endpoint)
    // JpaRepository provides findAll(), sorting can be done in service or via Sort parameter
}