package ch.thoenluk.solvers.WhatDoYouMeanWritingVerticallyIsStrange;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.InputFormat;
import ch.thoenluk.InputParser;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;
import ch.thoenluk.ut.UtStrings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static ch.thoenluk.InputParser.STREAMED_LINES;

@Day(5)
@InputFormat(STREAMED_LINES)
public class ThingsByNinetyDegreesRotatinator implements ChristmasSaver<Stream<String>> {
    @Override
    public String saveChristmas(final Stream<String> input) {
        final String[][] pieces = input
                .map(s -> Arrays.stream(s.split(UtStrings.WHITE_SPACE_REGEX))
                        .filter(split -> !split.isBlank())
                        .toArray(String[]::new)
                )
                .toArray(String[][]::new);
        final int numberOfArguments = pieces.length - 1;
        final int numberOfEquations = pieces[0].length;
        final List<Equation> equations = new LinkedList<>();
        for (int i = 0; i < numberOfEquations; i++) {
            final List<String> numbers = new ArrayList<>(numberOfArguments);
            for (int k = 0; k < numberOfArguments; k++) {
                numbers.add(pieces[k][i]);
            }
            final String op = pieces[numberOfArguments][i];
            equations.add(Equation.fromDescription(numbers, op));
        }
        return UtMath.restOfTheLongOwl(equations.stream().map(Equation::getResult));
    }

    @Override
    public String saveChristmasAgain(final Stream<String> input) {
        return UtMath.restOfTheLongOwl(separateEquations(transpose(input)).stream()
                .map(Equation::fromEquationList)
                .map(Equation::getResult));
    }
    /*
     I'll be honest, I don't like this one.
     Usually, the point of these challenges is that the abstract description is simple, the practical implementation is not.
     At least if you'd like to reach an answer before the heat death of the universe, yes literally.
     In this case however, the first and second challenge hardly differ. You get an extra step of weird notation to parse
     (or as the japanese would put it, literally just normal writing?) but there is no need for optimisation.

     Unless you do something very wrong, one way or another you'll visit each character precisely once. Unless you're using
     LinkedLists to represent the Strings (don't) that will not cost significant time, so there's no need to optimise.
     The most effective solution is to simply do exactly as the description says.

     I know I moan a lot about the challenges being underspecified, but I make what's asked of me and that's that. If I
     wanted to get praised for coding by pattern without thinking, I may as well do it at work and get paid!

     Incidentally, this challenge is underspecified. No, really. The example features trimmed strings; That is, there are
     not any spaces at the end of the first two lines, "above" the final 4. There also are not spaces after the last +.
     In your input, however, all lines are padded with spaces to be the same length. Or mine just happened to end on a
     four-digit number, I guess. Either way, this produces the unique case that the example has an edge case your input won't.
    */

    private List<String> transpose(final Stream<String> input) {
        final char[][] characters = input.map(String::toCharArray).toArray(char[][]::new);
        final int columns = characters[0].length;
        return IntStream.range(0, columns)
                .mapToObj(column -> {
                    final StringBuilder builder = new StringBuilder();
                    Arrays.stream(characters)
                            .map(line -> line[column])
                            .forEach(builder::append);
                    return builder.toString();
                })
                .toList();
    }

    private List<List<String>> separateEquations(final List<String> equations) {
        final List<List<String>> separated = new LinkedList<>();
        int start = 0;
        for (int end = 0; end < equations.size(); end++) {
            if (equations.get(end).isBlank()) {
                separated.add(equations.subList(start, end));
                start = end + 1;
            }
        }
        separated.add(equations.subList(start, equations.size()));
        return separated;
    }

    private record Equation(List<Long> arguments, BinaryOperator<Long> operation) {
        private static final String ADDITION = "+";

        public static Equation fromDescription(final List<String> args, final String op) {
            final List<Long> parsed = args.stream()
                    .map(UtParsing::cachedParseLong)
                    .toList();
            final BinaryOperator<Long> operator = op.trim().equals(ADDITION)
                    ? UtMath::superOverflowSafeSum
                    : UtMath::superOverflowSafeProduct;
            return new Equation(parsed, operator);
        }

        public static Equation fromEquationList(final List<String> equation) {
            final List<Long> parsed = equation.stream()
                    .map(s -> s.replaceFirst("[*+]", ""))
                    .map(String::trim)
                    .map(UtParsing::cachedParseLong)
                    .toList();
            final BinaryOperator<Long> operator = equation.getFirst().endsWith(ADDITION)
                    ? UtMath::superOverflowSafeSum
                    : UtMath::superOverflowSafeProduct;
            return new Equation(parsed, operator);
        }

        public long getResult() {
            return arguments.stream().reduce(operation).orElseThrow();
        }
    }
}
