package ee.taltech.zoomalyzer.util;

import ee.taltech.zoomalyzer.entities.Recording;

import java.math.BigInteger;
import java.security.SecureRandom;

public class Utils {
    public static String getUniqueName(Recording recording) {
        return String.format("recording-%s", recording.getId());
    }

    public static String generateRandomToken(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[length / 2]; // We divide by 2 because we'll convert bytes to a hexadecimal string

        secureRandom.nextBytes(tokenBytes);
        BigInteger tokenInt = new BigInteger(1, tokenBytes);
        String randomToken = tokenInt.toString(16);

        // Ensure the token has the desired length by padding with zeros if necessary
        while (randomToken.length() < length) {
            randomToken = "0" + randomToken;
        }

        return randomToken;
    }

    public static void validateToken(String token, Recording recording) {
        // TODO add admin token
        if (token != null && !recording.getToken().equals(token)) {
            throw new RuntimeException("Invalid token for that recording");
        }
    }
}
