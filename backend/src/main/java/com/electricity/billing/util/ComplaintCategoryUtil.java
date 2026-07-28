package com.electricity.billing.util;

import com.electricity.billing.entity.enums.ComplaintType;

import java.util.List;
import java.util.Map;

/** US008 - Category options shown in the complaint form, dependent on the selected complaint type. */
public final class ComplaintCategoryUtil {

    private static final Map<ComplaintType, List<String>> CATEGORIES = Map.of(
            ComplaintType.BILLING_ISSUE, List.of("Incorrect Bill Amount", "Duplicate Bill", "Bill Not Received", "Late Fee Dispute"),
            ComplaintType.POWER_OUTAGE, List.of("Full Outage", "Partial Outage", "Frequent Tripping", "Voltage Fluctuation"),
            ComplaintType.METER_ISSUE, List.of("Faulty Meter", "Incorrect Meter Reading", "New Meter Request", "Meter Tampering"),
            ComplaintType.OTHER, List.of("General Inquiry", "New Connection Request", "Other")
    );

    private ComplaintCategoryUtil() {
    }

    public static List<String> categoriesFor(ComplaintType type) {
        return CATEGORIES.getOrDefault(type, List.of());
    }

    public static Map<ComplaintType, List<String>> allCategories() {
        return CATEGORIES;
    }

    public static boolean isValidCategory(ComplaintType type, String category) {
        return categoriesFor(type).stream().anyMatch(c -> c.equalsIgnoreCase(category));
    }

    private static final Map<ComplaintType, Long> RESOLUTION_HOURS = Map.of(
            ComplaintType.BILLING_ISSUE, 48L,
            ComplaintType.POWER_OUTAGE, 12L,
            ComplaintType.METER_ISSUE, 72L,
            ComplaintType.OTHER, 48L
    );

    public static long resolutionHoursFor(ComplaintType type) {
        return RESOLUTION_HOURS.getOrDefault(type, 48L);
    }
}
