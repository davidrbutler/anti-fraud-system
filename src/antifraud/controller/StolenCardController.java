package antifraud.controller;

import antifraud.dto.CardRequest;
import antifraud.dto.CardResponse;
import antifraud.dto.DeleteStatusResponse;
import antifraud.model.StolenCard;
import antifraud.service.StolenCardService;
import antifraud.util.ValidationUtil; // For path variable validation
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/antifraud/stolencard")
public class StolenCardController {

    private final StolenCardService stolenCardService;
    private final ValidationUtil validationUtil; // Inject for path variable validation

    @Autowired
    public StolenCardController(StolenCardService stolenCardService, ValidationUtil validationUtil) {
        this.stolenCardService = stolenCardService;
        this.validationUtil = validationUtil;
    }

    // POST /api/antifraud/stolencard
    @PostMapping
    public ResponseEntity<CardResponse> addStolenCard(@Valid @RequestBody CardRequest request) {
        // Service layer handles validation (Luhn, conflict) and throws exceptions
        StolenCard savedCard = stolenCardService.addStolenCard(request.getNumber());
        // Convert entity to response DTO
        CardResponse response = new CardResponse(savedCard.getId(), savedCard.getNumber());
        // Spec example 6 shows 200 OK for success
        return ResponseEntity.ok(response);
    }

    // DELETE /api/antifraud/stolencard/{number}
    @DeleteMapping("/{number}")
    public ResponseEntity<DeleteStatusResponse> deleteStolenCard(@PathVariable String number) {
        // Explicitly validate path variable format *before* calling service
        if (!validationUtil.isValidLuhn(number)) {
            // Let RestExceptionHandler handle this via @ControllerAdvice
            throw new IllegalArgumentException("Invalid card number format (Luhn check failed) in path variable!");
        }
        // Service layer handles finding/deleting and throws exceptions (Not Found)
        Map<String, String> statusMap = stolenCardService.deleteStolenCard(number);
        // Convert map to response DTO
        DeleteStatusResponse response = new DeleteStatusResponse(statusMap.get("status"));
        return ResponseEntity.ok(response);
    }

    // GET /api/antifraud/stolencard
    @GetMapping
    public ResponseEntity<List<CardResponse>> listStolenCards() {
        List<StolenCard> cards = stolenCardService.listStolenCards();
        // Convert list of entities to list of response DTOs
        List<CardResponse> response = cards.stream()
                .map(cardEntity -> new CardResponse(cardEntity.getId(), cardEntity.getNumber()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}