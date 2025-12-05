package ch.thoenluk.solvers.AndHereIWasJokingAboutOverelaborateIdSystems;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.InputFormat;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;
import ch.thoenluk.ut.UtStrings;

import java.util.*;

import static ch.thoenluk.InputParser.EMPTY_LINE_SEPARATED_STRINGS;

@Day(5)
@InputFormat(EMPTY_LINE_SEPARATED_STRINGS)
public class Ramseyinator implements ChristmasSaver<String[]> {
    @Override
    public String saveChristmas(String[] input) {
        final List<RangeButNotTheDay2Ones> ranges = UtStrings.streamInputAsLines(input[0])
                .map(RangeButNotTheDay2Ones::fromDescription)
                .toList();
        return Long.toString(UtStrings.streamInputAsLines(input[1])
                .map(UtParsing::cachedParseLong)
                .filter(id -> ranges.stream()
                        .anyMatch(range -> range.contains(id))
                )
                .count()
        );
    }

    @Override
    public String saveChristmasAgain(String[] input) {
        final List<RangeButNotTheDay2Ones> ranges = new ArrayList<>(UtStrings.streamInputAsLines(input[0])
                .map(RangeButNotTheDay2Ones::fromDescription)
                .toList());
        mergeRanges(ranges);
        return UtMath.restOfTheLongOwl(ranges.stream().mapToLong(RangeButNotTheDay2Ones::length));
    }

    private void mergeRanges(final List<RangeButNotTheDay2Ones> ranges) {
        boolean mergedRanges = true;
        while (mergedRanges) {
            mergedRanges = false;
            for (int i = 0; i < ranges.size(); i++) {
                final RangeButNotTheDay2Ones range = ranges.get(i);
                final List<RangeButNotTheDay2Ones> overlappingRanges = ranges.stream()
                        .filter(r -> r != range && range.overlaps(r))
                        .toList();
                if (!overlappingRanges.isEmpty()) {
                    ranges.remove(range);
                    ranges.removeAll(overlappingRanges);
                    ranges.add(range.merge(overlappingRanges));
                    mergedRanges = true;
                }
            }
        }
    }

    private record RangeButNotTheDay2Ones(long start, long end) {
        public static RangeButNotTheDay2Ones fromDescription(final String description) {
            final List<Long> bounds = Arrays.stream(description.split("-"))
                    .map(UtParsing::cachedParseLong)
                    .toList();
            return new RangeButNotTheDay2Ones(bounds.getFirst(), bounds.getLast());
        }

        public boolean contains(final long id) {
            return start() <= id && id <= end();
        }

        public long length() {
            return end() - start() + 1L;
            // This is why we iterate with end exclusive
        }

        public boolean overlaps(final RangeButNotTheDay2Ones other) {
            return start() <= other.end() && other.start() <= end();
        }

        public RangeButNotTheDay2Ones merge(final Collection<RangeButNotTheDay2Ones> others) {
            final long othersStart = others.stream()
                    .mapToLong(RangeButNotTheDay2Ones::start)
                    .min()
                    .orElseThrow();
            final long othersEnd = others.stream()
                    .mapToLong(RangeButNotTheDay2Ones::end)
                    .max()
                    .orElseThrow();
            return new RangeButNotTheDay2Ones(Math.min(start(), othersStart), Math.max(end(), othersEnd));
        }
    }
}
