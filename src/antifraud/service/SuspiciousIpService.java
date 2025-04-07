package antifraud.service;

import antifraud.exception.IpAddressConflictException;
import antifraud.exception.IpAddressNotFoundException;
import antifraud.model.SuspiciousIp;
import antifraud.repository.SuspiciousIpRepository;
import antifraud.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class SuspiciousIpService {

    private final SuspiciousIpRepository ipRepository;
    private final ValidationUtil validationUtil;

    @Autowired
    public SuspiciousIpService(SuspiciousIpRepository ipRepository, ValidationUtil validationUtil) {
        this.ipRepository = ipRepository;
        this.validationUtil = validationUtil;
    }

    @Transactional
    public SuspiciousIp addSuspiciousIp(String ip) {
        if (!validationUtil.isValidIpV4(ip)) {
            throw new IllegalArgumentException("Invalid IPv4 format!");
        }
        if (ipRepository.existsByIp(ip)) {
            // Throw specific exception for conflict
            throw new IpAddressConflictException("IP address " + ip + " already exists!");
        }
        SuspiciousIp suspiciousIp = new SuspiciousIp(null, ip);
        return ipRepository.save(suspiciousIp);
    }

    @Transactional
    public Map<String, String> deleteSuspiciousIp(String ip) {
        if (!validationUtil.isValidIpV4(ip)) {
            throw new IllegalArgumentException("Invalid IPv4 format!");
        }
        SuspiciousIp suspiciousIp = ipRepository.findByIp(ip)
                // Throw specific exception for not found
                .orElseThrow(() -> new IpAddressNotFoundException("IP address " + ip + " not found!"));

        ipRepository.delete(suspiciousIp);
        return Map.of("status", "IP " + ip + " successfully removed!");
    }

    public List<SuspiciousIp> listSuspiciousIps() {
        return ipRepository.findAllByOrderByIdAsc();
    }

    public boolean isIpSuspicious(String ip) {
        // Format validation should happen before calling this in TransactionService
        return ipRepository.existsByIp(ip);
    }
}