package ch.thoenluk.ut;

import java.util.*;

public class UtParsing {
    private static final Map<String, Integer> STRING_INTEGER_CACHE = new HashMap<>();
    private static final Map<String, Long> STRING_LONG_CACHE = new HashMap<>();
    private static final Map<Character, Integer> CHAR_INTEGER_CACHE = new HashMap<>();

    public static synchronized int cachedParseInt(final String stringRepresentation) {
        return STRING_INTEGER_CACHE.computeIfAbsent(stringRepresentation, Integer::parseInt);
    }

    public static synchronized int cachedParseInt(final String stringRepresentation, final int radix) {
        if (!STRING_INTEGER_CACHE.containsKey(stringRepresentation)) {
            STRING_INTEGER_CACHE.put(stringRepresentation, Integer.parseInt(stringRepresentation, radix));
        }

        return STRING_INTEGER_CACHE.get(stringRepresentation);
    }

    public static synchronized long cachedParseLong(final String stringRepresentation) {
        return STRING_LONG_CACHE.computeIfAbsent(stringRepresentation, Long::parseLong);
    }

    public static synchronized long cachedParseLong(final String stringRepresentation, final int radix) {
        if (!STRING_LONG_CACHE.containsKey(stringRepresentation)) {
            STRING_LONG_CACHE.put(stringRepresentation, Long.parseLong(stringRepresentation, radix));
        }

        return STRING_LONG_CACHE.get(stringRepresentation);
    }

    public static synchronized int cachedGetNumericValue(final char charRepresentation) {
        return CHAR_INTEGER_CACHE.computeIfAbsent(charRepresentation, Character::getNumericValue);
    }

    public static List<Integer> multilineStringToIntegerList(final String stringRepresentation) {
        final String[] lines = UtStrings.splitMultilineString(stringRepresentation);
        final List<Integer> parsedList = new ArrayList<>(lines.length);
        for (final String line : lines) {
            parsedList.add(cachedParseInt(line));
        }
        return parsedList;
    }

    public static Map<Position, Character> multilineStringToPositionCharacterMap(final String stringRepresentation) {
        final Map<Position, Character> map = new HashMap<>();
        int y, x;

        final String[] lines = UtStrings.splitMultilineString(stringRepresentation);

        for (y = 0; y < lines.length; y++) {
            final String line = lines[y];
            for (x = 0; x < line.length(); x++) {
                map.put(new Position(y, x), line.charAt(x));
            }
        }

        return map;
    }

    public static String[] positionCharacterMapToStringArray(final Map<Position, Character> map) {
        final List<StringBuilder> stringBuilders = new ArrayList<>();
        map.keySet().stream()
                .sorted(Position::compareAsCoordinates)
                .forEach(coordinate -> {
                    if (stringBuilders.size() < coordinate.y() + 1) {
                        stringBuilders.add(new StringBuilder());
                    }
                    final StringBuilder stringBuilder = stringBuilders.get(coordinate.y());
                    stringBuilder.append(map.get(coordinate));
                });
        return stringBuilders.stream()
                .map(StringBuilder::toString)
                .toArray(String[]::new);
    }

    public static Map<Position, Integer> multilineStringToPositionIntegerMap(final String stringRepresentation) {
        final Map<Position, Integer> map = new HashMap<>();
        int y, x;

        final String[] lines = UtStrings.splitMultilineString(stringRepresentation);

        for (y = 0; y < lines.length; y++) {
            for (x = 0; x < lines[y].length(); x++) {
                map.put(new Position(y, x), cachedGetNumericValue(lines[y].charAt(x)));
            }
        }

        return map;
    }

    public static List<Integer> commaSeparatedStringToIntegerList(final String csv) {
        final String[] tokens = UtStrings.splitCommaSeparatedString(csv);
        final List<Integer> parsedList = new ArrayList<>();
        for (final String token : tokens) {
            parsedList.add(cachedParseInt(token));
        }
        return parsedList;
    }

    public static List<Integer> whitespaceSeparatedStringToIntegerList(final String wss) {
        return Arrays.stream(wss.split(UtStrings.WHITE_SPACE_REGEX))
                .map(UtParsing::cachedParseInt)
                .toList();
    }

    public static List<Long> commaSeparatedStringToLongList(final String csv) {
        return Arrays.stream(UtStrings.splitCommaSeparatedString(csv))
                .map(String::trim)
                .map(UtParsing::cachedParseLong)
                .toList();
    }

    public static List<Long> whitespaceSeparatedStringToLongList(final String wss) {
        return Arrays.stream(wss.split(UtStrings.WHITE_SPACE_REGEX))
                .map(UtParsing::cachedParseLong)
                .toList();
    }
}
