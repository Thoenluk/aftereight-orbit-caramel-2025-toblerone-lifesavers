package ch.thoenluk.solvers.TachyonPlease;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.InputFormat;
import ch.thoenluk.ut.Position;
import ch.thoenluk.ut.UtCollections;
import ch.thoenluk.ut.UtMath;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.thoenluk.InputParser.MULTILINE_POSITION_CHARACTER_MAP;

@Day(7)
@InputFormat(MULTILINE_POSITION_CHARACTER_MAP)
public class BeamAntiCrossinator implements ChristmasSaver<Map<Position, Character>> {
    private static final Character SPLITTER = '^';
    private static final Character START = 'S';

    @Override
    public String saveChristmas(Map<Position, Character> input) {
        return doTheThing(input);
    }

    private static String doTheThing(Map<Position, Character> input) {
        final Set<Position> splitters = UtCollections.findPositionsWithValueInMap(input, SPLITTER).collect(Collectors.toSet());
        final Position start = UtCollections.findSinglePositionWithValueInMap(input, START);
        final List<Position> positionsToExplore = new LinkedList<>(List.of(start));
        final Set<Position> tachyonBeamLocations = new HashSet<>();
        final Set<Position> usedSplitters = new HashSet<>();
        while (!positionsToExplore.isEmpty()) {
            final Position current = positionsToExplore.removeFirst();
            final Position down = current.offsetBy(Position.DOWN);
            final Stream<Position> newLocations;
            if (splitters.contains(down)) {
                newLocations = Stream.of(down.offsetBy(Position.LEFT), down.offsetBy(Position.RIGHT));
                usedSplitters.add(down);
            } else {
                newLocations = Stream.of(down);
            }
            newLocations.filter(input::containsKey)
                .filter(position -> !tachyonBeamLocations.contains(position))
                .forEach(position -> {
                    tachyonBeamLocations.add(position);
                    positionsToExplore.add(position);
                });
        }
        return Integer.toString(usedSplitters.size());
    }

    @Override
    public String saveChristmasAgain(Map<Position, Character> input) {
        final Set<Position> splitters = UtCollections.findPositionsWithValueInMap(input, SPLITTER).collect(Collectors.toSet());
        final Position start = UtCollections.findSinglePositionWithValueInMap(input, START);
        return Long.toString(splitTimelines(start, input, splitters, new HashMap<>()));
    }
    // I am a little sad that simply running the full solution is a heat death of the universe kinda deal. Curse you, exponential growth!
    // Then again, solving a challenge about parallel universes using recursion only feels right.
    // It's a bit of a cookie cutter solution, but only because we've been here before.
    // If nothing else, a lightning fast recursive exponential unwrapper is a neat party trick to know.

    private long splitTimelines(final Position start, final Map<Position, Character> input,
                               final Set<Position> splitters, final Map<Position, Long> timelines) {
        Position current = start;
        while (input.containsKey(current)) {
            current = current.offsetBy(Position.DOWN);
            if (splitters.contains(current)) {
                if (timelines.containsKey(current)) {
                    return timelines.get(current);
                }
                final long timelinesCreated = UtMath.superOverflowSafeSum(
                    splitTimelines(current.offsetBy(Position.LEFT), input, splitters, timelines),
                    splitTimelines(current.offsetBy(Position.RIGHT), input, splitters, timelines)
                );
                timelines.put(current, timelinesCreated);
                return timelinesCreated;
            }
        }
        return 1;
    }
}
