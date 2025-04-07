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
    private final ValidationUtil validationUtil;

    @Autowired
    public StolenCardController(StolenCardService stolenCardService, ValidationUtil validationUtil) {
        this.stolenCardService = stolenCardService;
        this.validationUtil = validationUtil;
    }

    @PostMapping
    public ResponseEntity<CardResponse> addStolenCard(@Valid @RequestBody CardRequest request) {
        StolenCard savedCard = stolenCardService.addStolenCard(request.getNumber());
        CardResponse response = new CardResponse(savedCard.getId(), savedCard.getNumber());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{number}")
    public ResponseEntity<DeleteStatusResponse> deleteStolenCard(@PathVariable String number) {
        if (!validationUtil.isValidLuhn(number)) {
            throw new IllegalArgumentException("Invalid card number format (Luhn check failed) in path variable!");
        }
        Map<String, String> statusMap = stolenCardService.deleteStolenCard(number);
        DeleteStatusResponse response = new DeleteStatusResponse(statusMap.get("status"));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CardResponse>> listStolenCards() {
        List<StolenCard> cards = stolenCardService.listStolenCards();
        List<CardResponse> response = cards.stream()
                .map(cardEntity -> new CardResponse(cardEntity.getId(), cardEntity.getNumber()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}