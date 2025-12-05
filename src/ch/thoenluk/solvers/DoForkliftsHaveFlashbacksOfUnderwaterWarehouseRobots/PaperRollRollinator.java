package ch.thoenluk.solvers.DoForkliftsHaveFlashbacksOfUnderwaterWarehouseRobots;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.InputFormat;
import ch.thoenluk.ut.Position;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.thoenluk.InputParser.MULTILINE_POSITION_CHARACTER_MAP;

@Day(4)
@InputFormat(MULTILINE_POSITION_CHARACTER_MAP)
public class PaperRollRollinator implements ChristmasSaver<Map<Position, Character>> {
    private static final Character PAPER = '@';

    @Override
    public String saveChristmas(Map<Position, Character> input) {
        final Map<Position, Character> paperLocations = filterToPaperLocations(input);
        return Long.toString(findRemovablePaperLocations(paperLocations).count());
    }

    private Stream<Position> findRemovablePaperLocations(Map<Position, Character> paperLocations) {
        return paperLocations.keySet().stream()
                .filter(position -> isAccessible(position, paperLocations));
    }

    private static Map<Position, Character> filterToPaperLocations(Map<Position, Character> input) {
        return input.entrySet().stream()
                .filter(entry -> entry.getValue().equals(PAPER))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public String saveChristmasAgain(Map<Position, Character> input) {
        final Map<Position, Character> paperLocations = filterToPaperLocations(input);
        int result = 0;
        List<Position> removablePapers;
        do {
            removablePapers = findRemovablePaperLocations(paperLocations).toList();
            removablePapers.forEach(paperLocations::remove);
            result += removablePapers.size();
        } while (!removablePapers.isEmpty());
        return Integer.toString(result);
    }

    private boolean isAccessible(final Position position, final Map<Position, Character> map) {
        return position.getOmnidirectionalNeighbours().stream()
                .filter(map::containsKey)
                .count() < 4;
    }
}
