package com.electricity.billing.util;

import java.util.List;
import java.util.function.Function;

/** Small, dependency-free helper to render tabular data as CSV for admin export features. */
public final class CsvExportUtil {

    private CsvExportUtil() {
    }

    public static <T> String toCsv(List<String> headers, List<T> rows, Function<T, List<String>> rowMapper) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", headers)).append("\n");
        for (T row : rows) {
            List<String> values = rowMapper.apply(row);
            sb.append(values.stream().map(CsvExportUtil::escape)
                    .reduce((a, b) -> a + "," + b).orElse("")).append("\n");
        }
        return sb.toString();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
