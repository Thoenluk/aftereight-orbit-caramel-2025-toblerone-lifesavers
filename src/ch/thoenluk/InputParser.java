package ch.thoenluk;

import ch.thoenluk.ut.UtParsing;
import ch.thoenluk.ut.UtStrings;

import java.util.function.Function;

public enum InputParser {
    COMMA_SEPARATED_STRINGS(UtStrings::splitCommaSeparatedString),
    MULTILINE_STRING(UtStrings::splitMultilineString),
    STREAMED_LINES(UtStrings::streamInputAsLines),
    COMMA_SEPARATED_INTEGER_LIST(UtParsing::commaSeparatedStringToIntegerList),
    COMMA_SEPARATED_LONG_LIST(UtParsing::commaSeparatedStringToLongList),
    WHITESPACE_SEPARATED_INTEGER_LIST(UtParsing::whitespaceSeparatedStringToIntegerList),
    WHITESPACE_SEPARATED_LONG_LIST(UtParsing::whitespaceSeparatedStringToLongList),
    MULTILINE_INTEGER_LIST(UtParsing::multilineStringToIntegerList),
    MULTILINE_POSITION_INTEGER_MAP(UtParsing::multilineStringToPositionIntegerMap),
    MULTILINE_POSITION_CHARACTER_MAP(UtParsing::multilineStringToPositionCharacterMap);

    private final Function<String, ?> parsingFunction; // Give me generics on enums, you cowards.

    InputParser(Function<String, ?> parsingFunction) {
        this.parsingFunction = parsingFunction;
    }

    public <T> T parse(final String inputString) {
        return (T) parsingFunction.apply(inputString);
    }
}
