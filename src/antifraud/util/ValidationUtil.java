package antifraud.util;

import org.springframework.stereotype.Component; // Make it a component or keep static

import java.util.regex.Pattern;

@Component
public class ValidationUtil {

    // Simple regex for IPv4 format validation
    // (Note: This doesn't check for leading zeros or edge cases perfectly,
    // using a library like Apache Commons Validator might be more robust if needed)
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    public boolean isValidIpV4(String ip) {
        if (ip == null) {
            return false;
        }
        return IPV4_PATTERN.matcher(ip).matches();
    }

    // Luhn algorithm check implementation
    public boolean isValidLuhn(String number) {
        if (number == null || number.isEmpty() || !number.matches("\\d+")) {
            return false; // Must contain only digits
        }

        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
}