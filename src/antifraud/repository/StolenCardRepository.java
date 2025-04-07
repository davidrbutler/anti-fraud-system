package antifraud.repository;

import antifraud.model.StolenCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StolenCardRepository extends JpaRepository<StolenCard, Long> {

    // Find a card by its number (needed for checking existence and deletion)
    Optional<StolenCard> findByNumber(String number);

    // Find all cards sorted by ID ascending (for the GET list endpoint)
    List<StolenCard> findAllByOrderByIdAsc();

    // Check if a card number exists
    boolean existsByNumber(String number);
}