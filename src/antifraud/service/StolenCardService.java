package antifraud.service;

// Import specific exceptions
import antifraud.exception.StolenCardConflictException;
import antifraud.exception.StolenCardNotFoundException;
import antifraud.model.StolenCard;
import antifraud.repository.StolenCardRepository;
import antifraud.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class StolenCardService {

    private final StolenCardRepository cardRepository;
    private final ValidationUtil validationUtil;

    @Autowired
    public StolenCardService(StolenCardRepository cardRepository, ValidationUtil validationUtil) {
        this.cardRepository = cardRepository;
        this.validationUtil = validationUtil;
    }

    @Transactional
    public StolenCard addStolenCard(String number) {
        if (!validationUtil.isValidLuhn(number)) {
            // Keep throwing IllegalArgumentException for bad format (maps to 400)
            throw new IllegalArgumentException("Invalid card number format (Luhn check failed)!");
        }
        if (cardRepository.existsByNumber(number)) {
            // Throw specific exception for conflict
            throw new StolenCardConflictException("Card number " + number + " already exists!");
        }
        StolenCard stolenCard = new StolenCard(null, number);
        return cardRepository.save(stolenCard);
    }

    @Transactional
    public Map<String, String> deleteStolenCard(String number) {
        if (!validationUtil.isValidLuhn(number)) {
            // Keep throwing IllegalArgumentException for bad format (maps to 400)
            throw new IllegalArgumentException("Invalid card number format (Luhn check failed)!");
        }
        StolenCard stolenCard = cardRepository.findByNumber(number)
                // Throw specific exception for not found
                .orElseThrow(() -> new StolenCardNotFoundException("Card number " + number + " not found!"));

        cardRepository.delete(stolenCard);
        return Map.of("status", "Card " + number + " successfully removed!");
    }

    public List<StolenCard> listStolenCards() {
        return cardRepository.findAllByOrderByIdAsc();
    }

    public boolean isCardStolen(String number) {
        // Format validation should happen before calling this in TransactionService
        return cardRepository.existsByNumber(number);
    }
}