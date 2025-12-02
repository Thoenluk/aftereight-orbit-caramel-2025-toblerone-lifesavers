package ch.thoenluk.solvers.Obsecurity;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.InputFormat;
import ch.thoenluk.InputParser;
import ch.thoenluk.ut.UtParsing;

import java.util.stream.Stream;

@Day(1)
@InputFormat(InputParser.STREAMED_LINES)
public class DecoySafePasswordPadCrackinator implements ChristmasSaver<Stream<String>> {
    @Override
    public String saveChristmas(Stream<String> input) {
        final DialPointinator pointinator = new DialPointinator();
        input.map(this::parseMovement).forEach(pointinator::turn);
        return Integer.toString(pointinator.getTimesPointedAtZero());
    }

    @Override
    public String saveChristmasAgain(Stream<String> input) {
        final DialPointinator pointinator = new DialPointinator();
        pointinator.countCrossings();
        input.map(this::parseMovement).forEach(pointinator::turn);
        return Integer.toString(pointinator.getTimesPointedAtZero());
    }

    private int parseMovement(final String description) {
        final char direction = description.charAt(0);
        final int distance = UtParsing.cachedParseInt(description.substring(1));
        return direction == 'R' ? distance : -distance;
    }
}
