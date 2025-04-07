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
    private final ValidationUtil validationUtil; /

    @Autowired
    public SuspiciousIpController(SuspiciousIpService suspiciousIpService, ValidationUtil validationUtil) {
        this.suspiciousIpService = suspiciousIpService;
        this.validationUtil = validationUtil;
    }

    @PostMapping
    public ResponseEntity<IpResponse> addSuspiciousIp(@Valid @RequestBody IpRequest request) {
        SuspiciousIp savedIp = suspiciousIpService.addSuspiciousIp(request.getIp());
        IpResponse response = new IpResponse(savedIp.getId(), savedIp.getIp());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{ip}")
    public ResponseEntity<DeleteStatusResponse> deleteSuspiciousIp(@PathVariable String ip) {
        if (!validationUtil.isValidIpV4(ip)) {
            throw new IllegalArgumentException("Invalid IPv4 format in path variable!");
        }
        Map<String, String> statusMap = suspiciousIpService.deleteSuspiciousIp(ip);
        DeleteStatusResponse response = new DeleteStatusResponse(statusMap.get("status"));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<IpResponse>> listSuspiciousIps() {
        List<SuspiciousIp> ips = suspiciousIpService.listSuspiciousIps();
        List<IpResponse> response = ips.stream()
                .map(ipEntity -> new IpResponse(ipEntity.getId(), ipEntity.getIp()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}