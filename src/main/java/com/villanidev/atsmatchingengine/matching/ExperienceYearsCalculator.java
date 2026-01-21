package com.villanidev.atsmatchingengine.matching;

import com.villanidev.atsmatchingengine.domain.CvMaster;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

public class ExperienceYearsCalculator {

    private final DateParser dateParser = new DateParser();

    public double calculateTotalYearsOfExperience(CvMaster cvMaster) {
        if (cvMaster.getExperiences() == null || cvMaster.getExperiences().isEmpty()) {
            return 0.0;
        }

        Optional<YearMonth> earliestStart = cvMaster.getExperiences().stream()
                .map(CvMaster.Experience::getStart)
                .filter(Objects::nonNull)
                .map(dateParser::parseYearMonth)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Comparator.naturalOrder());

        if (earliestStart.isEmpty()) {
            return 0.0;
        }

        YearMonth now = YearMonth.now();
        return earliestStart.get().until(now, ChronoUnit.MONTHS) / 12.0;
    }
}
