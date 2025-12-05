package ch.thoenluk.solvers.EscalatorTemporarilyStairs;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.InputFormat;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static ch.thoenluk.InputParser.STREAMED_LINES;

@Day(3)
@InputFormat(STREAMED_LINES)
public class BatteryLickinator implements ChristmasSaver<Stream<String>> {
    @Override
    public String saveChristmas(Stream<String> input) {
        return doTheThing(input, 2);
    }

    @Override
    public String saveChristmasAgain(Stream<String> input) {
        return doTheThing(input, 12);
    }

    private String doTheThing(Stream<String> input, final int digitsInNumber) {
        return UtMath.restOfTheLongOwl(input.map(this::mapBatteryBank)
                .map(bank -> findHighestJoltageInSection(bank, digitsInNumber - 1))
        );
    }

    private List<Integer> mapBatteryBank(String batteryBankDescription) {
        return new ArrayList<>(batteryBankDescription.chars()
                .mapToObj(i -> (char) i)
                .map(UtParsing::cachedGetNumericValue)
                .toList());
    }

    private long findHighestJoltageInSection(final List<Integer> section, final int digitsAfterThis) {
        if (digitsAfterThis == 0) {
            return section.stream().mapToInt(i -> i).max().orElseThrow();
        }
        final int indexOfHighestFirstDigit = findIndexOfHighestFirstDigit(section, digitsAfterThis);
        final long raisedFirstDigit = section.get(indexOfHighestFirstDigit) * Math.powExact(10L, digitsAfterThis);
        final List<Integer> remainingSection = section.subList(indexOfHighestFirstDigit + 1, section.size());
        return raisedFirstDigit + findHighestJoltageInSection(remainingSection, digitsAfterThis - 1);
    }

    private int findIndexOfHighestFirstDigit(final List<Integer> section, final int digitsAfterThis) {
        // This section converted to loop from stream because loops can early-out, where a stream would always
        // check each digit even if it already found a higher one in the list.
        // This saved about 50% runtime!
        for (int digit = 9; digit >= 0; digit--) {
            final int index = section.indexOf(digit);
            if (index > -1 && index < section.size() - digitsAfterThis) {
                return index;
            }
        }
        throw new IllegalStateException();
    }
}
