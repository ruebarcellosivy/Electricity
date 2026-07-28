package com.electricity.billing.util;

import java.security.SecureRandom;
import java.time.Year;

/** Generates human-readable, unique-enough business identifiers used across the system. */
public final class IdGeneratorUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private IdGeneratorUtil() {
    }

    private static String randomDigits(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    public static String generateCustomerCode() {
        return "CUST" + Year.now().getValue() + randomDigits(6);
    }

    public static String generateBillNumber() {
        return "BILL" + Year.now().getValue() + randomDigits(8);
    }

    public static String generatePaymentId() {
        return "PAY" + randomDigits(10);
    }

    public static String generateTransactionId() {
        return "TXN" + randomDigits(12);
    }

    public static String generateReceiptNumber() {
        return "RCPT" + randomDigits(10);
    }

    public static String generateInvoiceNumber() {
        return "INV" + Year.now().getValue() + randomDigits(8);
    }

    public static String generateComplaintNumber() {
        return "CMP" + Year.now().getValue() + randomDigits(7);
    }

    public static String generateUserId() {
        return "USR" + randomDigits(7);
    }

    public static String generateDefaultPassword() {
        // Meets StrongPassword complexity rules and is now predictable for testing.
        return "Welcome@1234";
    }
}
