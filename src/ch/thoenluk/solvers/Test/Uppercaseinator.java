package ch.thoenluk.solvers.Test;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.InputFormat;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static ch.thoenluk.InputParser.STREAMED_LINES;

@Day(0)
@InputFormat(STREAMED_LINES)
public class Uppercaseinator implements ChristmasSaver<Stream<String>> {
    @Override
    public String saveChristmas(final Stream<String> input) {
        return input.map(String::toUpperCase)
                .collect(Collectors.joining());
    }

    @Override
    public String saveChristmasAgain(final Stream<String> input) {
        return input.map(this::toSarcasmCase).collect(Collectors.joining());
    }

    private String toSarcasmCase(final String line) {
        final String lower = line.toLowerCase();
        final String upper = line.toUpperCase();
        return IntStream.range(0, line.length())
                .map(index -> index % 2 == 0 ? lower.charAt(index) : upper.charAt(index))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
