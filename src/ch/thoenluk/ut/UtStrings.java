package ch.thoenluk.ut;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Stream;

public class UtStrings {


    public static final String WHITE_SPACE_REGEX = "[\\s\\n\\r]+";
    public static final String NEWLINE_REGEX = "\\r?\\n";
    public static final String NUMBERS_REGEX = "\\d+";

    public static String[] splitCommaSeparatedString(final String csv) {
        return csv.replaceAll(NEWLINE_REGEX, "").split(",");
    }

    public static void print(final Object objToPrint) {
        System.out.print(objToPrint);
    }

    public static void println() {
        System.out.println();
    }

    public static void println(final Object objToPrint) {
        System.out.println(objToPrint);
    }

    public static String readFile(final File file) {
        try {
            return Files.readString(file.toPath());
        }
        catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    public static String[] splitMultilineString(final String multiline) {
        return multiline.replaceAll(NEWLINE_REGEX, "\n").split("\n");
    }

    public static Stream<String> streamInputAsLines(final String input) {
        return input.lines();
    }

    public static String[] splitStringWithEmptyLines(final String emptyLineSeparatedString) {
        return emptyLineSeparatedString.replaceAll(NEWLINE_REGEX, "\n").split("\n\n");
    }

    public static String substringUntilDelimiter(final String string, final String delimiter) {
        final int indexOfDelimiter = string.indexOf(delimiter);
        if (indexOfDelimiter == -1) {
            return string;
        }
        return string.substring(0, indexOfDelimiter);
    }
}
