package mz.co.amaterasu.gestao.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class Utils {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generatePassword(int length) {
        StringBuilder password = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(randomIndex));
        }

        return password.toString();
    }

    public static double roundNumber(double number, int places) {
        if (places < 0) return number;
        BigDecimal bd = new BigDecimal(number).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
