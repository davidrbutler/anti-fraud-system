package antifraud.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Represents the world regions allowed for transactions.
 * Codes are based on the Stage 5 requirements.
 */
public enum Region {
    EAP,  // East Asia and Pacific
    ECA,  // Europe and Central Asia
    HIC,  // High-Income countries
    LAC,  // Latin America and the Caribbean
    MENA, // The Middle East and North Africa
    SA,   // South Asia
    SSA;  // Sub-Saharan Africa

    /**
     * Checks if a given string code corresponds to a valid Region.
     * Case-sensitive.
     * @param code The code to check (e.g., "EAP").
     * @return true if valid, false otherwise.
     */
    // Optional: Add a helper method for validation if needed elsewhere,
    // otherwise direct Enum.valueOf() with try-catch can be used.
    public static boolean isValidCode(String code) {
        return Arrays.stream(Region.values())
                .anyMatch(region -> region.name().equals(code));
    }

    /**
     * Attempts to find a Region enum constant for a given code string.
     * Case-sensitive.
     * @param code The code string.
     * @return An Optional containing the Region if found, otherwise empty.
     */
    public static Optional<Region> fromCode(String code) {
        try {
            return Optional.of(Region.valueOf(code));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }
}