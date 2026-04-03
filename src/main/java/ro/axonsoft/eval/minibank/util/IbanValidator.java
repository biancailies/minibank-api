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
}
