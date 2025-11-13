package com.example.taskmanager.util;

import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilTest {

    private static final ZoneId ATHENS = ZoneId.of("Europe/Athens");

    @Test
    void toLocalDateTime_convertsUtcToAthens_inWinter() {
        // 2025-01-15T12:00Z (UTC) -> Athens is UTC+2 in January => 14:00
        Instant instant = Instant.parse("2025-01-15T12:00:00Z");

        LocalDateTime local = DateTimeUtil.toLocalDateTime(instant);

        assertEquals(LocalDateTime.of(2025, 1, 15, 14, 0, 0), local);
    }

    @Test
    void toLocalDateTime_convertsUtcToAthens_inSummer_DST() {
        // 2025-07-15T12:00Z -> Athens is UTC+3 in July => 15:00
        Instant instant = Instant.parse("2025-07-15T12:00:00Z");

        LocalDateTime local = DateTimeUtil.toLocalDateTime(instant);

        assertEquals(LocalDateTime.of(2025, 7, 15, 15, 0, 0), local);
    }

    @Test
    void toInstant_convertsAthensLocalToUtc_inWinter() {
        // Athens local 2025-01-15 14:00 at UTC+2 -> 12:00Z
        LocalDateTime athensLocal = LocalDateTime.of(2025, 1, 15, 14, 0, 0);

        Instant utc = DateTimeUtil.toInstant(athensLocal);

        assertEquals(Instant.parse("2025-01-15T12:00:00Z"), utc);
    }

    @Test
    void toInstant_convertsAthensLocalToUtc_inSummer_DST() {
        // Athens local 2025-07-15 15:00 at UTC+3 -> 12:00Z
        LocalDateTime athensLocal = LocalDateTime.of(2025, 7, 15, 15, 0, 0);

        Instant utc = DateTimeUtil.toInstant(athensLocal);

        assertEquals(Instant.parse("2025-07-15T12:00:00Z"), utc);
    }

    @Test
    void converters_handleNulls() {
        assertNull(DateTimeUtil.toLocalDateTime(null));
        assertNull(DateTimeUtil.toInstant(null));
        assertNull(DateTimeUtil.formatLocal(null));
    }

    @Test
    void formatLocal_rendersExpectedPattern() {
        // Athens 2025-01-15 14:00 should format as "2025-01-15 14:00:00"
        Instant instant = Instant.parse("2025-01-15T12:00:00Z");

        String formatted = DateTimeUtil.formatLocal(instant);

        assertEquals("2025-01-15 14:00:00", formatted);
    }

    @Test
    void now_methods_are_time_consistent_within_one_minute() {
        // Sanity check (non-deterministic but tolerant)
        Instant nowUtc = DateTimeUtil.nowUtc();
        LocalDateTime expectedLocal = LocalDateTime.ofInstant(nowUtc, ATHENS);

        LocalDateTime nowLocal = DateTimeUtil.nowLocal();

        // Allow up to 60s difference to avoid flakiness
        long deltaSeconds = Math.abs(Duration.between(expectedLocal, nowLocal).getSeconds());
        assertTrue(deltaSeconds < 60, "nowLocal should be within 60s of converted nowUtc");
    }
}
