package ch.thoenluk.solvers.TwitchPlaysAOCActuallySoundsMorbidlyEntertaining;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.InputFormat;
import ch.thoenluk.ut.ThreeDPosition;
import ch.thoenluk.ut.UtCollections;
import ch.thoenluk.ut.UtCollections.Pair;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtStrings;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ch.thoenluk.InputParser.MULTILINE_3D_POSITION_LIST;

@Day(8)
@InputFormat(MULTILINE_3D_POSITION_LIST)
public class AllMyCircuitsScreenwritinator implements ChristmasSaver<List<ThreeDPosition>> {
    @Override
    public String saveChristmas(List<ThreeDPosition> input) {
        final List<Connection> connections = buildConnections(input);
        final Map<ThreeDPosition, Set<ThreeDPosition>> circuits = buildCircuits(input);

        connections.stream()
                .limit(1000)
                .forEach(connection -> connection.combineCircuits(circuits));
        // And there's the obligatory challenge missing a real example.
        // This one's extra special because it barely explains the intended end state AND the description is unclear.
        // "The next two junction boxes are 431,825,988 and 425,690,689.
        // Because these two junction boxes were already in the same circuit, nothing happens!"
        // Pop quiz: Does that mean "We move on without connecting these boxes" or "We connect the boxes, but no new circuits are formed"?
        // Material difference!
        // You'll note the test output is wrong, and who cares, really. It would be trivial to give a full-size example
        // for 1000 iterations, if only input and output values. If they won't, I won't.

        return Integer.toString(circuits.values().stream()
                .distinct()
                .map(Set::size)
                .sorted(Comparator.reverseOrder())
                .limit(3)
                .reduce(UtMath::overflowSafeProduct)
                .orElseThrow());
    }

    private static Map<ThreeDPosition, Set<ThreeDPosition>> buildCircuits(List<ThreeDPosition> input) {
        return input.stream().collect(Collectors.toMap(Function.identity(), position -> {
            final Set<ThreeDPosition> value = new HashSet<>();
            value.add(position);
            return value;
        }));
    }

    private static List<Connection> buildConnections(List<ThreeDPosition> input) {
        return UtCollections.streamPairwise(input)
                .map(Connection::fromEdges)
                .sorted(Comparator.comparing(Connection::distance))
                .toList();
    }

    @Override
    public String saveChristmasAgain(List<ThreeDPosition> input) {
        final List<Connection> connections = buildConnections(input);
        final Map<ThreeDPosition, Set<ThreeDPosition>> circuits = buildCircuits(input);
        for (Connection connection : connections) {
            connection.combineCircuits(circuits);
            final Set<ThreeDPosition> circuit = circuits.get(connection.start());
            if (circuit.size() == circuits.size()) {
                return Long.toString(
                    ((long) connection.start().x()) * connection.end().x()
                ); // And we end with an overflow check that the example doesn't need, which I am convinced could be avoided.
                // My answer is 2_185_817_796, which you may notice is barely above INTEGER_MAX_VALUE.
                // I do not like this challenge. Thankfully it wasn't hard, it just isn't good.
            }
        }

        throw new IllegalStateException("Literally how did this happen?");
    }

    private record Connection(ThreeDPosition start, ThreeDPosition end, long distance) {
        public static Connection fromEdges(final Pair<ThreeDPosition> edges) {
            return new Connection(edges.first(), edges.second(), edges.first().getSquaredStraightLineDistanceFrom(edges.second()));
        }

        public void combineCircuits(final Map<ThreeDPosition, Set<ThreeDPosition>> circuits) {
            final Set<ThreeDPosition> result = circuits.get(start());
            if (result.contains(end())) {
                return;
            }
            result.addAll(circuits.get(end()));
            circuits.get(end()).forEach(position -> circuits.put(position, result));
        }
    }
}
