package ch.thoenluk.solvers.WhyDontTheyUseTheHyperspaceRecursiveFlippingTiles;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.InputFormat;
import ch.thoenluk.ut.Position;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static ch.thoenluk.InputParser.MULTILINE_POSITION_LIST;
import static java.lang.Math.*;

@Day(9)
@InputFormat(MULTILINE_POSITION_LIST)
public class BasicGeometryCalculatinator implements ChristmasSaver<List<Position>> {
    @Override
    public String saveChristmas(List<Position> input) {
        final RectangleFinder finder = new RectangleFinder(input);
        return Long.toString(input.stream()
                .mapToLong(finder::findMaxRectangleArea)
                .max()
                .orElseThrow());
    }

    @Override
    public String saveChristmasAgain(List<Position> input) {
        final GreenTileFinder tileFinder = GreenTileFinder.fromPositions(input);
        final RectangleFinder finder = new RectangleFinder(input);
        return Long.toString(input.stream()
                .mapToLong(position -> finder.findMaxRectangleAreaInsideShape(position, tileFinder))
                .max()
                .orElseThrow());
    }

    private record RectangleFinder(List<Position> allPositions) {
        public long findMaxRectangleArea(final Position thisOne) {
            return allPositions.stream()
                    .mapToLong(thatOne -> findRectangleArea(thisOne, thatOne))
                    .max()
                    .orElseThrow();
        }

        public long findMaxRectangleAreaInsideShape(final Position thisOne, final GreenTileFinder tileFinder) {
            return allPositions.stream()
                    .filter(thatOne -> thisOne != thatOne)
                    .filter(thatOne -> tileFinder.rectangleIsInsideShapeGigaBrained(thisOne, thatOne))
                    .mapToLong(thatOne -> findRectangleArea(thisOne, thatOne))
                    .max()
                    .orElse(0L);
        }

        private long findRectangleArea(final Position thisOne, final Position thatOne) {
            return (1L + abs(thisOne.x() - thatOne.x())) * (1L + abs(thisOne.y() - thatOne.y()));
        }
    }

    private record GreenTileFinder(List<Edge> vertical, List<Edge> horizontal) {
        public static GreenTileFinder fromPositions(final List<Position> input) {
            final List<Edge> edges = createEdges(input);
            final List<Edge> vertical = edges.stream()
                    .filter(Edge::isVertical)
                    .toList();
            final List<Edge> horizontal = edges.stream()
                    .filter(Predicate.not(Edge::isVertical))
                    .toList();
            return new GreenTileFinder(vertical, horizontal);
        }

        public boolean rectangleIsInsideShapeGigaBrained(final Position thisOne, final Position thatOne) {
            final int topLeftX = min(thisOne.x(), thatOne.x());
            final int topLeftY = min(thisOne.y(), thatOne.y());
            final int bottomRightX = max(thisOne.x(), thatOne.x());
            final int bottomRightY = max(thisOne.y(), thatOne.y());
            final List<Position> corners = List.of(
                    new Position(topLeftY, topLeftX),
                    new Position(topLeftY, bottomRightX),
                    new Position(bottomRightY, bottomRightX),
                    new Position(bottomRightY, topLeftX)
            );
            return createEdges(corners).stream()
                    .flatMap(border -> border.findPossiblePositionsOutsideShape(border.isVertical() ? horizontal : vertical))
                    .allMatch(this::isGreenOrRed);
            // You could presumably speed this up by a factor of some 1000. Instead of checking the points on both sides
            // of the offending edge, determine which one point is potentially outside the shape.
            // That's a factor of 2. The remaining 500 are achieved by checking only the offending edge for whether the
            // potentially outside point is on the inside side of that edge.
            // In the example, this would be point 8,4 on rectangle border 2,3 -> 9, 3 which crosses the edge 7,3 -> 7,1,
            // but stays inside the shape.
            // This is left as an exercise to the reader because eeeeeerm 80:20 rule, value maximising, synergy growth. There.
        }

        // Preserved for posterity. It works! It just takes a very long time, despite reducing the amount of positions
        // checked quite drastically; For a square of side length a, this will only check 4a positions instead of a^2.
        public boolean rectangleIsInsideShapeNaive(final Position thisOne, final Position thatOne) {
            final int topLeftX = min(thisOne.x(), thatOne.x());
            final int topLeftY = min(thisOne.y(), thatOne.y());
            final int bottomRightX = max(thisOne.x(), thatOne.x());
            final int bottomRightY = max(thisOne.y(), thatOne.y());
            return IntStream.rangeClosed(topLeftX, bottomRightX).mapToObj(i -> new Position(topLeftY, i)).allMatch(this::isGreenOrRed)
            && IntStream.rangeClosed(topLeftX, bottomRightX).mapToObj(i -> new Position(bottomRightY, i)).allMatch(this::isGreenOrRed)
            && IntStream.rangeClosed(topLeftY, bottomRightY).mapToObj(i -> new Position(i, topLeftX)).allMatch(this::isGreenOrRed)
            && IntStream.rangeClosed(topLeftY, bottomRightY).mapToObj(i -> new Position(i, bottomRightX)).allMatch(this::isGreenOrRed);
        }

        private boolean isGreenOrRed(final Position position) {
            if (isOnAnyEdge(position)) {
                return true;
            }
            return isInsideShapeOnRightSide(position)
                    && isInsideShapeOnBottomSide(position)
                    && isInsideShapeOnLeftSide(position)
                    && isInsideShapeOnTopSide(position);
        }

        private boolean isOnAnyEdge(final Position position) {
            return vertical.stream().anyMatch(edge -> edge.contains(position.x(), position.y()))
                    || horizontal.stream().anyMatch(edge -> edge.contains(position.y(), position.x()));
        }

        private boolean isInsideShapeOnRightSide(final Position position) {
            // I tried to make a parameterised version of these functions. Its more hassle than worth.
            return findEdgesInRegion(vertical, position.y())
                    .filter(edge -> edge.isToTheRightOf(position))
                    .min(Comparator.comparing(Edge::location))
                    .map(edge -> edge.isOnInsideSide(position.x()))
                    .orElse(false);
        }

        private boolean isInsideShapeOnLeftSide(final Position position) {
            return findEdgesInRegion(vertical, position.y())
                    .filter(edge -> edge.isToTheLeftOf(position))
                    .max(Comparator.comparing(Edge::location))
                    .map(edge -> edge.isOnInsideSide(position.x()))
                    .orElse(false);
        }

        private boolean isInsideShapeOnBottomSide(final Position position) {
            return findEdgesInRegion(horizontal, position.x())
                    .filter(edge -> edge.isBelow(position))
                    .min(Comparator.comparing(Edge::location))
                    .map(edge -> edge.isOnInsideSide(position.y()))
                    .orElse(false);
        }

        private boolean isInsideShapeOnTopSide(final Position position) {
            return findEdgesInRegion(horizontal, position.x())
                    .filter(edge -> edge.isAbove(position))
                    .max(Comparator.comparing(Edge::location))
                    .map(edge -> edge.isOnInsideSide(position.y()))
                    .orElse(false);
        }

        private Stream<Edge> findEdgesInRegion(final List<Edge> edges, final int coordinate) {
            return edges.stream().filter(edge -> edge.isInRegion(coordinate));
        }

        private static List<Edge> createEdges(List<Position> input) {
            final List<Edge> edges = new LinkedList<>();
            final Iterator<Position> iterator = input.listIterator();
            Position first = iterator.next();
            while (iterator.hasNext()) {
                final Position second = iterator.next();
                edges.add(Edge.fromVertices(first, second));
                first = second; // You have to admit this is smooth AND performant.
                // I could also have transformed input into an array, but where's the fun in that.
                // Note: I am trying to avoid using input.get() unnecessarily for performance reasons.
            }
            edges.add(Edge.fromVertices(input.getLast(), input.getFirst()));
            return edges;
        }
    }

    private record Edge(int start, int end, int location, EdgeDirection direction) {
        public static Edge fromVertices(final Position first, final Position second) {
            final boolean isVertical = first.x() == second.x();
            final Function<Position, Integer> coordinateGetter = isVertical
                    ? Position::y
                    : Position::x;
            final int firstCoordinate = coordinateGetter.apply(first);
            final int secondCoordinate = coordinateGetter.apply(second);
            final int start = min(firstCoordinate, secondCoordinate);
            final int end = max(firstCoordinate, secondCoordinate);
            final int location = isVertical ? first.x() : first.y();
            final EdgeDirection direction = isVertical
                    ? first.y() < second.y() ? EdgeDirection.DOWN : EdgeDirection.UP
                    : first.x() < second.x() ? EdgeDirection.RIGHT : EdgeDirection.LEFT;
            return new Edge(start, end, location, direction);
        }

        public boolean isVertical() {
            return direction == EdgeDirection.DOWN || direction == EdgeDirection.UP;
        }

        public boolean isInRegion(final int coordinate) {
            return start <= coordinate && coordinate <= end;
        }

        public boolean contains(final int orthogonalCoordinate, final int alignedCoordinate) {
            return location == orthogonalCoordinate && isInRegion(alignedCoordinate);
        }

        public boolean isOnInsideSide(final int coordinate) {
            return isLesserCoordinateInside()
                    ? coordinate <= location
                    : coordinate >= location;
        }

        public boolean isBelow(final Position position) {
            return location >= position.y();
        }

        public boolean isToTheLeftOf(final Position position) {
            return location <= position.x();
        }

        public boolean isAbove(final Position position) {
            return location <= position.y();
        }

        public boolean isToTheRightOf(final Position position) {
            return location >= position.x();
        }

        public Stream<Position> findPossiblePositionsOutsideShape(final List<Edge> orthogonalShapeEdges) {
            return orthogonalShapeEdges.stream()
                    .filter(this::crosses)
                    .flatMap(this::createPossiblePositionsOutsideShape);
        }

        private boolean crosses(final Edge shapeEdge) {
            return isStrictlyInRegion(shapeEdge.location)
                    && shapeEdge.isInRegion(location);
        }

        private boolean isStrictlyInRegion(final int coordinate) {
            return (start < coordinate && coordinate <= end)
                    || (start <= coordinate && coordinate < end);
        }

        private Stream<Position> createPossiblePositionsOutsideShape(final Edge edge) {
            final IntStream alignedCoordinates = IntStream.of(edge.location - 1, edge.location + 1)
                    .filter(this::isInRegion);
            if (isVertical()) {
                    return alignedCoordinates.mapToObj(y -> new Position(y, location));
            }
            return alignedCoordinates.mapToObj(x -> new Position(location, x));
        }

        private boolean isLesserCoordinateInside() {
            return direction == EdgeDirection.DOWN || direction == EdgeDirection.LEFT;
        }
    }

    private enum EdgeDirection {
        DOWN,
        LEFT,
        UP,
        RIGHT
    }
}
