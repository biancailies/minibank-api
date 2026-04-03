package ro.axonsoft.eval.minibank.util;

import java.math.BigInteger;

import static java.util.regex.Pattern.matches;

public class IbanValidator {

    public IbanValidator() {}

    public static String normalizeIban(String iban) {
        iban = iban.replace("-", "");
        iban = iban.replace(" ", "");
        return iban;
    }

    public static boolean isValidIban(String iban) {

        if (iban == null)
            return false;

        iban = normalizeIban(iban).toUpperCase();

        if (iban.isBlank())
            return false;

        if(!(matches("[A-Z0-9]+", iban)))
            return false;

        if(iban.length() < 4)
            return false;

        String newIban = iban.substring(4) + iban.substring(0,4);

        StringBuilder sb = new StringBuilder();

        for (char c : newIban.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
            } else {
                sb.append(c - 'A' + 10);
            }
        }

        BigInteger newIb = new BigInteger(sb.toString());

        if(newIb.mod(BigInteger.valueOf(97)).equals(BigInteger.ONE))
            return true;

        return false;
    }

    public static String getCountryCode(String iban) {
        if (iban == null)
            return null;

        iban = normalizeIban(iban).toUpperCase();

        if (iban.length() < 2)
            return null;

        return iban.substring(0, 2);
    }

    public static boolean isSepaCountry(String countryCode) {
        if (countryCode == null)
            return false;

        switch (countryCode) {
            case "AT": case "BE": case "BG": case "CH": case "CY":
            case "CZ": case "DE": case "DK": case "EE": case "ES":
            case "FI": case "FR": case "GB": case "GI": case "GR":
            case "HR": case "HU": case "IE": case "IS": case "IT":
            case "LI": case "LT": case "LU": case "LV": case "MC":
            case "MT": case "NL": case "NO": case "PL": case "PT":
            case "RO": case "SE": case "SI": case "SK": case "SM":
            case "AD": case "VA":
                return true;
            default:
                return false;
        }
    }

}
