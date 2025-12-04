package ch.thoenluk.solvers.ElfbertSemicolonApostropheDropTableNaughtyChildren;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.InputFormat;
import ch.thoenluk.InputParser;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Map.entry;

@Day(2)
@InputFormat(InputParser.COMMA_SEPARATED_STRINGS)
public class PostInputValidatinator implements ChristmasSaver<String[]> {
    @Override
    public String saveChristmas(String[] input) {
        return UtMath.restOfTheLongOwl(Arrays.stream(input)
                .map(Range::fromDescription)
                .filter(Objects::nonNull)
                .map(Range::sumContainedInvalidIds));
    }

    @Override
    public String saveChristmasAgain(String[] input) {
        return UtMath.restOfTheLongOwl(Arrays.stream(input)
                .flatMap(Range::fromDescriptionForSecondChallenge)
                .map(Range::sumContainedInvalidIds));
    }

    private record Range(long lowerBound, long upperBound, List<Long> distancesToSum, Long distanceToRemove) {
        private static final Map<Integer, List<Long>> DISTANCES_TO_SUM = Map.ofEntries(
            entry(1, List.of(12508845L)), // Starting this year strong with a mandatory input hack, are we.
            // Your input contains a range from a one-digit number to a two-digit one, like 3-20.
            // You may think not to treat one-digits. Since 1 < 2, last I checked, all one-digit numbers are valid IDs.
            // Still, gotta check them without errors some way even though a one-digit number is practically an invalid input.
            // I'd like to send this challenge back as "Not enough specification" as it would be great to mention one-digit
            // numbers being possible input in the example.
            entry(2, List.of(11L)),
            entry(3, List.of(111L)),
            entry(4, List.of(101L)),
            entry(5, List.of(11_111L)),
            entry(6, List.of(1_001L, 10_101L)),
            entry(7, List.of(1_111_111L)),
            entry(8, List.of(10_001L)),
            entry(9, List.of(1_001_001L)),
            entry(10, List.of(100_001L, 101_010_101L))
        ); // I'm sure it's possible to compute these. Sadly, engineers don't care what mathematicians think is possible.
        private static final Map<Integer, Long> DISTANCES_TO_REMOVE = Map.ofEntries(
            entry(6, 111_111L),
            entry(10, 1_111_111_111L)
        ); // haha maps go brr

        public static Range fromDescription(final String description) {
            final List<Long> bounds = Arrays.stream(description.split("-"))
                    .map(UtParsing::cachedParseLong)
                    .toList();
            final long lowerBound = raiseLowerBoundToEvenOOM(bounds.getFirst());
            final long upperBound = reduceUpperBoundToEvenOOM(bounds.getLast());
            final int digits = UtMath.numberOfDigits(lowerBound);
            final long distance = Math.powExact(10, digits / 2) + 1;
            final long lowestInvalidId = findLowestInvalidId(lowerBound, distance);
            final long highestInvalidId = findHighestInvalidId(upperBound, distance);
            if (lowestInvalidId > highestInvalidId) {
                return null;
            }
            return new Range(lowerBound, upperBound, List.of(distance), null);
        }

        public static Stream<Range> fromDescriptionForSecondChallenge(final String description) {
            final List<Long> bounds = Arrays.stream(description.split("-"))
                    .map(UtParsing::cachedParseLong)
                    .toList();
            final long lowerBound = bounds.getFirst();
            final long upperBound = bounds.getLast();
            final int lowerDigits = UtMath.numberOfDigits(lowerBound);
            final int upperDigits = UtMath.numberOfDigits(upperBound);
            if (lowerDigits == upperDigits) {
                return Stream.of(packageSecondChallengeRange(lowerBound, upperBound, lowerDigits));
            }
            final long borderpoint = Math.powExact(10, upperDigits - 1);
            return Stream.of(
                packageSecondChallengeRange(lowerBound, borderpoint - 1, lowerDigits),
                packageSecondChallengeRange(borderpoint, upperBound, upperDigits)
            );
        }

        private static Range packageSecondChallengeRange(final long lower, final long upper, final int digits) {
            return new Range(lower, upper, DISTANCES_TO_SUM.get(digits), DISTANCES_TO_REMOVE.get(digits));
        }

        private static long raiseLowerBoundToEvenOOM(final long lowerBound) {
            final int orderOfMagnitude = UtMath.numberOfDigits(lowerBound);
            if (UtMath.isOdd(orderOfMagnitude)) {
                return Math.powExact(10L, orderOfMagnitude);
            }
            return lowerBound;
        }

        private static long findLowestInvalidId(final long lowerBound, final long distance) {
            final long invalidId = transformToInvalidId(lowerBound, distance);
            if (invalidId >= lowerBound) {
                return invalidId;
            }
            return invalidId + distance;

        }

        private static long reduceUpperBoundToEvenOOM(final long upperBound) {
            final int orderOfMagnitude = UtMath.numberOfDigits(upperBound);
            if (UtMath.isOdd(orderOfMagnitude)) {
                return Math.powExact(10L, orderOfMagnitude - 1) - 1;
            }
            return upperBound;
        }

        private static long findHighestInvalidId(final long upperBound, final long distance) {
            final long invalidId = transformToInvalidId(upperBound, distance);
            if (invalidId <= upperBound) {
                return invalidId;
            }
            return invalidId - distance;
        }

        private static long transformToInvalidId(final long bound, final long distance) {
            final long remainder = bound % distance;
            return bound - remainder;
        }

        public long sumContainedInvalidIds() {
            final long invalidIds = distancesToSum().stream()
                    .map(this::sumContainedInvalidIdsForDistance)
                    .reduce(0L, UtMath::superOverflowSafeSum);
            final long doubleMatches = Optional.ofNullable(distanceToRemove())
                        .map(this::sumContainedInvalidIdsForDistance)
                        .orElse(0L);
            return invalidIds - doubleMatches;
        }

        private long sumContainedInvalidIdsForDistance(long distance) {
            final long lowestInvalidId = findLowestInvalidId(lowerBound(), distance);
            final long highestInvalidId = findHighestInvalidId(upperBound(), distance);
            final long numberOfInvalidIds = (highestInvalidId - lowestInvalidId) / distance;
            final long instancesOfLowestInvalidId = lowestInvalidId * (numberOfInvalidIds + 1);
            final long extraFromHigherIds = UtMath.triangularNumber(numberOfInvalidIds) * distance;
            return instancesOfLowestInvalidId + extraFromHigherIds;
        }
    }
}
