package ch.thoenluk.solvers.DoForkliftsHaveFlashbacksOfUnderwaterWarehouseRobots;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.InputFormat;
import ch.thoenluk.ut.Position;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.thoenluk.InputParser.MULTILINE_POSITION_CHARACTER_MAP;

@Day(4)
@InputFormat(MULTILINE_POSITION_CHARACTER_MAP)
public class PaperRollRollinator implements ChristmasSaver<Map<Position, Character>> {
    private static final Character PAPER = '@';

    @Override
    public String saveChristmas(final Map<Position, Character> input) {
        final Set<Position> paperLocations = filterToPaperLocations(input);
        return Long.toString(findRemovablePaperLocations(paperLocations).count());
    }

    private Stream<Position> findRemovablePaperLocations(final Set<Position> paperLocations) {
        return paperLocations.stream().filter(position -> isAccessible(position, paperLocations));
    }

    private static Set<Position> filterToPaperLocations(final Map<Position, Character> input) {
        return input.entrySet().stream()
                .filter(entry -> entry.getValue().equals(PAPER))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public String saveChristmasAgain(final Map<Position, Character> input) {
        final Set<Position> paperLocations = filterToPaperLocations(input);
        int result = 0;
        List<Position> removablePapers;
        do {
            removablePapers = findRemovablePaperLocations(paperLocations).toList();
            removablePapers.forEach(paperLocations::remove);
            result += removablePapers.size();
        } while (!removablePapers.isEmpty());
        return Integer.toString(result);
    }

    private boolean isAccessible(final Position position, final Set<Position> map) {
        return position.getOmnidirectionalNeighbours().stream()
                .filter(map::contains)
                .count() < 4;
    }
}
