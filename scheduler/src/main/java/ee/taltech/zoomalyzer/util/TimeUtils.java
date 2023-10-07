package ee.taltech.zoomalyzer.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    private static final DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_INSTANT;

    public static String toIso8601(Instant time) {
        // iso8601DateTime "2023-10-04T14:30:00.000Z" in UTC
        return time.atZone(ZoneId.of("UTC")).format(isoFormatter);
    }

    public static String toIso8601(Instant time, String zoneId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return time.atZone(ZoneId.of(zoneId)).format(formatter);
    }

    public static Instant toInstant(String iso8601UTC) {
        // iso8601DateTime "2023-10-04T14:30:00.000Z" in UTC
        return Instant.parse(iso8601UTC);
    }
}
