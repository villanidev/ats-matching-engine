package com.villanidev.atsmatchingengine.matching;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DateParser {

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public Optional<YearMonth> parseYearMonth(String dateStr) {
        try {
            return Optional.of(YearMonth.parse(dateStr, YEAR_MONTH_FORMATTER));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
