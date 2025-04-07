package antifraud.controller;

import antifraud.dto.DeleteStatusResponse; // DTO for delete response
import antifraud.dto.IpRequest;
import antifraud.dto.IpResponse;
import antifraud.model.SuspiciousIp;
import antifraud.service.SuspiciousIpService;
import antifraud.util.ValidationUtil; // For path variable validation
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // For CREATED status
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/antifraud/suspicious-ip")
public class SuspiciousIpController {

    private final SuspiciousIpService suspiciousIpService;
    private final ValidationUtil validationUtil; // Inject for path variable validation

    @Autowired
    public SuspiciousIpController(SuspiciousIpService suspiciousIpService, ValidationUtil validationUtil) {
        this.suspiciousIpService = suspiciousIpService;
        this.validationUtil = validationUtil;
    }

    // POST /api/antifraud/suspicious-ip
    @PostMapping
    public ResponseEntity<IpResponse> addSuspiciousIp(@Valid @RequestBody IpRequest request) {
        // Service layer handles validation (format, conflict) and throws exceptions
        SuspiciousIp savedIp = suspiciousIpService.addSuspiciousIp(request.getIp());
        // Convert entity to response DTO
        IpResponse response = new IpResponse(savedIp.getId(), savedIp.getIp());
        // Stage 4 spec example shows 200 OK, but 201 Created is also common for POST
        return ResponseEntity.ok(response); // Use 200 OK as per example 4 in spec
        // return ResponseEntity.status(HttpStatus.CREATED).body(response); // Alternative
    }

    // DELETE /api/antifraud/suspicious-ip/{ip}
    @DeleteMapping("/{ip}")
    public ResponseEntity<DeleteStatusResponse> deleteSuspiciousIp(@PathVariable String ip) {
        // Explicitly validate path variable format *before* calling service
        if (!validationUtil.isValidIpV4(ip)) {
            // Let RestExceptionHandler handle this via @ControllerAdvice
            throw new IllegalArgumentException("Invalid IPv4 format in path variable!");
        }
        // Service layer handles finding/deleting and throws exceptions (Not Found)
        Map<String, String> statusMap = suspiciousIpService.deleteSuspiciousIp(ip);
        // Convert map to response DTO
        DeleteStatusResponse response = new DeleteStatusResponse(statusMap.get("status"));
        return ResponseEntity.ok(response);
    }

    // GET /api/antifraud/suspicious-ip
    @GetMapping
    public ResponseEntity<List<IpResponse>> listSuspiciousIps() {
        List<SuspiciousIp> ips = suspiciousIpService.listSuspiciousIps();
        // Convert list of entities to list of response DTOs
        List<IpResponse> response = ips.stream()
                .map(ipEntity -> new IpResponse(ipEntity.getId(), ipEntity.getIp()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}