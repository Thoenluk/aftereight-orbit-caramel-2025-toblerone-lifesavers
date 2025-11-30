package ch.thoenluk.ut;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class UtCollections {
    public static <T> Stream<Position> findPositionsWithValueInMap(final Map<Position, T> map, final T value) {
        return findKeysWithValueInMap(map, value);
    }

    public static <T> Position findSinglePositionWithValueInMap(final Map<Position, T> map, final T value) {
        return findPositionsWithValueInMap(map, value).findAny().orElseThrow();
    }

    public static <K, V> Stream<K> findKeysWithValueInMap(final Map<K, V> map, final V value) {
        return map.entrySet().stream()
                .filter(entry -> value.equals(entry.getValue()))
                .map(Map.Entry::getKey);
    }

    public static <T> Stream<List<T>> streamPairwise(final List<T> list) {
        return IntStream.range(0, list.size())
                .boxed()
                .flatMap(index -> toPairs(list, index));
    }

    private static <T> Stream<List<T>> toPairs(final List<T> list, final Integer index) {
        return list.subList(index + 1, list.size()).stream()
                .map(secondElement -> makePair(list, index, secondElement));
    }

    private static <T> List<T> makePair(final List<T> list, final Integer index, final T secondElement) {
        return List.of(list.get(index), secondElement);
    }

    public static <T> boolean anyOverlap(final Collection<T> first, final Collection<T> second) {
        final List<T> overlap = new LinkedList<>(first);
        overlap.retainAll(second);
        return !overlap.isEmpty();
    }
}
