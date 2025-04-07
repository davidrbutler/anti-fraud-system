package antifraud.repository;

import antifraud.model.SuspiciousIp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuspiciousIpRepository extends JpaRepository<SuspiciousIp, Long> {

    // Find an IP by its value (needed for checking existence and deletion)
    Optional<SuspiciousIp> findByIp(String ip);

    // Find all IPs sorted by ID ascending (for the GET list endpoint)
    List<SuspiciousIp> findAllByOrderByIdAsc();

    // Check if an IP exists (alternative to findByIp().isPresent())
    boolean existsByIp(String ip);
}