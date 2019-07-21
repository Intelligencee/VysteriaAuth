package org.vysteria.auth.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public class TOTP {

    public static final int DEFAULT_TIME_STEP_SECONDS = 30;
    private static int NUM_DIGITS_OUTPUT;
    private static final String blockOfZeros;

    public static String generateBase32Secret() {
        return generateBase32Secret(16);
    }

    public static String generateBase32Secret(final int length) {
        final StringBuilder sb = new StringBuilder(length);
        final Random random = new SecureRandom();
        for (int i = 0; i < length; ++i) {
            final int val = random.nextInt(32);
            if (val < 26) {
                sb.append((char)(65 + val));
            }
            else {
                sb.append((char)(50 + (val - 26)));
            }
        }
        return sb.toString();
    }

    public static boolean validateCurrentNumber(final String base32Secret, final int authNumber, final int windowMillis) throws GeneralSecurityException {
        return validateCurrentNumber(base32Secret, authNumber, windowMillis, System.currentTimeMillis(), 30);
    }

    public static boolean validateCurrentNumber(final String base32Secret, final int authNumber, final int windowMillis, final long timeMillis, final int timeStepSeconds) throws GeneralSecurityException {
        long from = timeMillis;
        long to = timeMillis;
        if (windowMillis > 0) {
            from -= windowMillis;
            to += windowMillis;
        }
        for (long timeStepMillis = timeStepSeconds * 1000, millis = from; millis <= to; millis += timeStepMillis) {
            final long compare = generateNumber(base32Secret, millis, timeStepSeconds);
            if (compare == authNumber) {
                return true;
            }
        }
        return false;
    }

    public static String generateCurrentNumberString(final String base32Secret) throws GeneralSecurityException {
        return generateNumberString(base32Secret, System.currentTimeMillis(), 30);
    }

    public static String generateNumberString(final String base32Secret, final long timeMillis, final int timeStepSeconds) throws GeneralSecurityException {
        final long number = generateNumber(base32Secret, timeMillis, timeStepSeconds);
        return zeroPrepend(number, TOTP.NUM_DIGITS_OUTPUT);
    }

    public static long generateCurrentNumber(final String base32Secret) throws GeneralSecurityException {
        return generateNumber(base32Secret, System.currentTimeMillis(), 30);
    }

    public static long generateNumber(final String base32Secret, final long timeMillis, final int timeStepSeconds) throws GeneralSecurityException {
        final byte[] key = decodeBase32(base32Secret);
        final byte[] data = new byte[8];
        long value = timeMillis / 1000L / timeStepSeconds;
        for (int i = 7; value > 0L; value >>= 8, --i) {
            data[i] = (byte)(value & 0xFFL);
        }
        final SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        final Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        final byte[] hash = mac.doFinal(data);
        final int offset = hash[hash.length - 1] & 0xF;
        long truncatedHash = 0L;
        for (int j = offset; j < offset + 4; ++j) {
            truncatedHash <<= 8;
            truncatedHash |= (hash[j] & 0xFF);
        }
        truncatedHash &= 0x7FFFFFFFL;
        truncatedHash %= 1000000L;
        return truncatedHash;
    }

    public static String qrImageUrl(final String keyId, final String secret) {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("https://chart.googleapis.com/chart?chs=128x128&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=");
        addOtpAuthPart(keyId, secret, sb);
        return sb.toString();
    }

    public static String generateOtpAuthUrl(final String keyId, final String secret) {
        final StringBuilder sb = new StringBuilder(64);
        addOtpAuthPart(keyId, secret, sb);
        return sb.toString();
    }

    private static void addOtpAuthPart(final String keyId, final String secret, final StringBuilder sb) {
        sb.append("otpauth://totp/").append(keyId).append("?secret=").append(secret);
    }

    static String zeroPrepend(final long num, final int digits) {
        final String numStr = Long.toString(num);
        if (numStr.length() >= digits) {
            return numStr;
        }
        final StringBuilder sb = new StringBuilder(digits);
        final int zeroCount = digits - numStr.length();
        sb.append(TOTP.blockOfZeros, 0, zeroCount);
        sb.append(numStr);
        return sb.toString();
    }

    static byte[] decodeBase32(final String str) {
        final int numBytes = (str.length() * 5 + 7) / 8;
        byte[] result = new byte[numBytes];
        int resultIndex = 0;
        int which = 0;
        int working = 0;
        for (int i = 0; i < str.length(); ++i) {
            final char ch = str.charAt(i);
            int val;
            if (ch >= 'a' && ch <= 'z') {
                val = ch - 'a';
            }
            else if (ch >= 'A' && ch <= 'Z') {
                val = ch - 'A';
            }
            else if (ch >= '2' && ch <= '7') {
                val = 26 + (ch - '2');
            }
            else {
                if (ch == '=') {
                    which = 0;
                    break;
                }
                throw new IllegalArgumentException("Invalid base-32 character: " + ch);
            }
            switch (which) {
                case 0: {
                    working = (val & 0x1F) << 3;
                    which = 1;
                    break;
                }
                case 1: {
                    working |= (val & 0x1C) >> 2;
                    result[resultIndex++] = (byte)working;
                    working = (val & 0x3) << 6;
                    which = 2;
                    break;
                }
                case 2: {
                    working |= (val & 0x1F) << 1;
                    which = 3;
                    break;
                }
                case 3: {
                    working |= (val & 0x10) >> 4;
                    result[resultIndex++] = (byte)working;
                    working = (val & 0xF) << 4;
                    which = 4;
                    break;
                }
                case 4: {
                    working |= (val & 0x1E) >> 1;
                    result[resultIndex++] = (byte)working;
                    working = (val & 0x1) << 7;
                    which = 5;
                    break;
                }
                case 5: {
                    working |= (val & 0x1F) << 2;
                    which = 6;
                    break;
                }
                case 6: {
                    working |= (val & 0x18) >> 3;
                    result[resultIndex++] = (byte)working;
                    working = (val & 0x7) << 5;
                    which = 7;
                    break;
                }
                case 7: {
                    working |= (val & 0x1F);
                    result[resultIndex++] = (byte)working;
                    which = 0;
                    break;
                }
            }
        }
        if (which != 0) {
            result[resultIndex++] = (byte)working;
        }
        if (resultIndex != result.length) {
            result = Arrays.copyOf(result, resultIndex);
        }
        return result;
    }

    static {
        TOTP.NUM_DIGITS_OUTPUT = 6;
        final char[] chars = new char[TOTP.NUM_DIGITS_OUTPUT];
        for (int i = 0; i < chars.length; ++i) {
            chars[i] = '0';
        }
        blockOfZeros = new String(chars);
    }
}
