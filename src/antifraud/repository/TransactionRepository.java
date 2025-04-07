package antifraud.repository;

import antifraud.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // Keep Optional if findById is used

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds all transactions for a given card number that occurred between
     * the specified start and end times (exclusive of end time, inclusive of start time).
     * Used for the correlation checks in Stage 5.
     */
    List<Transaction> findByNumberAndDateBetween(String number, LocalDateTime startDateTime, LocalDateTime endDateTime);


    /**
     * Finds all transaction history for a given card number, ordered by transaction ID ascending.
     * Used for the GET /api/antifraud/history/{number} endpoint.
     *
     * @param number The card number.
     * @return A list of transactions for the given card number, sorted by ID.
     */
    List<Transaction> findByNumberOrderByTransactionIdAsc(String number);

    /**
     * Finds all transaction history, ordered by transaction ID ascending.
     * Used for the GET /api/antifraud/history endpoint.
     * Note: JpaRepository.findAll(Sort.by("transactionId")) could also be used in the service.
     *
     * @return A list of all transactions, sorted by ID.
     */
    List<Transaction> findAllByOrderByTransactionIdAsc();

    // JpaRepository already provides findById(Long id) which returns Optional<Transaction>
}