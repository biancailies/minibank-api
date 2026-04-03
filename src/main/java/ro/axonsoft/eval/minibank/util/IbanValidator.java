package ro.axonsoft.eval.minibank.util;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class IbanValidator {

    private static final Pattern IBAN_PATTERN = Pattern.compile("^[A-Z0-9]+$");

    private static final Map<String, Integer> IBAN_LENGTHS = Map.ofEntries(
            Map.entry("AD", 24), Map.entry("AT", 20), Map.entry("BE", 16),
            Map.entry("BG", 22), Map.entry("CH", 21), Map.entry("CY", 28),
            Map.entry("CZ", 24), Map.entry("DE", 22), Map.entry("DK", 18),
            Map.entry("EE", 20), Map.entry("ES", 24), Map.entry("FI", 18),
            Map.entry("FR", 27), Map.entry("GB", 22), Map.entry("GI", 23),
            Map.entry("GR", 27), Map.entry("HR", 21), Map.entry("HU", 28),
            Map.entry("IE", 22), Map.entry("IS", 26), Map.entry("IT", 27),
            Map.entry("LI", 21), Map.entry("LT", 20), Map.entry("LU", 20),
            Map.entry("LV", 21), Map.entry("MC", 27), Map.entry("MT", 31),
            Map.entry("NL", 18), Map.entry("NO", 15), Map.entry("PL", 28),
            Map.entry("PT", 25), Map.entry("RO", 24), Map.entry("SE", 24),
            Map.entry("SI", 19), Map.entry("SK", 24), Map.entry("SM", 27),
            Map.entry("VA", 22)
    );

    private static final Set<String> SEPA_COUNTRIES = Set.of(
            "AT", "BE", "BG", "CH", "CY", "CZ", "DE", "DK", "EE", "ES",
            "FI", "FR", "GB", "GI", "GR", "HR", "HU", "IE", "IS", "IT",
            "LI", "LT", "LU", "LV", "MC", "MT", "NL", "NO", "PL", "PT",
            "RO", "SE", "SI", "SK", "SM", "AD", "VA"
    );

    private IbanValidator() {
    }

    public static String normalizeIban(String iban) {
        if (iban == null) {
            return null;
        }
        return iban.replace(" ", "").replace("-", "").toUpperCase();
    }

    public static boolean isValidIban(String iban) {
        String normalized = normalizeIban(iban);

        if (normalized == null || normalized.isBlank()) {
            return false;
        }

        if (!IBAN_PATTERN.matcher(normalized).matches()) {
            return false;
        }

        if (normalized.length() < 4) {
            return false;
        }

        String countryCode = normalized.substring(0, 2);
        Integer expectedLength = IBAN_LENGTHS.get(countryCode);

        if (expectedLength == null) {
            return false;
        }

        if (normalized.length() != expectedLength) {
            return false;
        }

        return mod97Check(normalized);
    }

    public static String getCountryCode(String iban) {
        String normalized = normalizeIban(iban);

        if (normalized == null || normalized.length() < 2) {
            return null;
        }

        return normalized.substring(0, 2);
    }

    public static boolean isSepaCountry(String countryCode) {
        return countryCode != null && SEPA_COUNTRIES.contains(countryCode);
    }

    private static boolean mod97Check(String iban) {
        String rearranged = iban.substring(4) + iban.substring(0, 4);

        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isDigit(c)) {
                numeric.append(c);
            } else {
                numeric.append(c - 'A' + 10);
            }
        }

        int remainder = 0;
        for (int i = 0; i < numeric.length(); i++) {
            int digit = numeric.charAt(i) - '0';
            remainder = (remainder * 10 + digit) % 97;
        }

        return remainder == 1;
    }
}