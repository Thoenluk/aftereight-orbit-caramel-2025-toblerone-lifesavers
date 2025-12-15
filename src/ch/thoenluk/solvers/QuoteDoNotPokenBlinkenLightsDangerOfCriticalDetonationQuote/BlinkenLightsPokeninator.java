package ch.thoenluk.solvers.QuoteDoNotPokenBlinkenLightsDangerOfCriticalDetonationQuote;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.InputFormat;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static ch.thoenluk.InputParser.STREAMED_LINES;

@Day(10)
@InputFormat(STREAMED_LINES)
public class BlinkenLightsPokeninator implements ChristmasSaver<Stream<String>> {
    @Override
    public String saveChristmas(Stream<String> input) {
        return UtMath.restOfTheOwl(input.map(BlinkyMachine::fromDescription)
                .map(BlinkyMachine::determineButtonPokenation));
    }

    @Override
    public String saveChristmasAgain(Stream<String> input) {
        return UtMath.restOfTheOwl(input.map(BlinkyMachine::fromDescriptionForSecondChallenge)
                .map(BlinkyMachine::determineJoltageZappenation));
    }

    private record BlinkyMachine(int lights, int[] buttons, List<Integer> joltages) {
        // In which TL shows off that he knows about bitwise operations with no regard whether that actually be faster
        // and certainly not easier to read.
        // You gotta experiment and sometimes, you discover what not to do in productive code. That's what AoC is for.
        private static final char ON = '#';
        private static final Pattern DESCRIPTION_PARSER = Pattern.compile("\\[([.#]+)] (.+) \\{(.+)}");

        public static BlinkyMachine fromDescription(final String description) {
            final Matcher matcher = createMatcherAndComplainIfNotMatching(description);
            final String blinken = matcher.group(1);
            final String poken = matcher.group(2);
            final int machineLength = blinken.length();
            return new BlinkyMachine(
                    parseLights(blinken),
                    parseButtons(poken, machineLength),
                    createJoltagesForOffMachine(machineLength)
            );
        }

        public static BlinkyMachine fromDescriptionForSecondChallenge(final String description) {
            final Matcher matcher = createMatcherAndComplainIfNotMatching(description);
            final String blinken = matcher.group(1);
            final String poken = matcher.group(2);
            final String zappen = matcher.group(3);
            final int machineLength = blinken.length();
            return new BlinkyMachine(
                    0,
                    parseButtons(poken, machineLength),
                    parseJoltages(zappen)
            );
        }

        private static Matcher createMatcherAndComplainIfNotMatching(String description) {
            final Matcher matcher = DESCRIPTION_PARSER.matcher(description);
            if (!matcher.matches()) {
                throw new IllegalStateException(String.format("""
                        Wasn't given a proper machine description in some way: %s
                        Also, is it just me who thinks a Matcher should have groups available right away, since it's created with the specific string?
                        I know you can matches() or find(), but maybe do a lazy eval for matches() when a group() method is called?""", description));
            }
            return matcher;
        }

        private static int parseLights(final String blinken) {
            final int machineLength = blinken.length();
            return IntStream.range(0, blinken.length())
                    .filter(light -> blinken.charAt(light) == ON)
                    .map(i -> machineLength - i - 1)
                    .map(i -> 1 << i)
                    .reduce(UtMath::overflowSafeSum)
                    .orElseThrow();
        }

        private static int[] parseButtons(final String poken, final int machineLength) {
            return Arrays.stream(poken.split(" "))
                    .mapToInt(label -> parseButton(label, machineLength))
                    .toArray();
        }

        private static int parseButton(final String label, final int machineLength) {
            return UtParsing.commaSeparatedStringToIntStream(label.substring(1, label.length() - 1))
                    .map(i -> (machineLength - i) - 1)
                    .map(affectedLight -> 1 << affectedLight)
                    .reduce(UtMath::overflowSafeSum)
                    .orElseThrow();
        }

        private static List<Integer> createJoltagesForOffMachine(final int machineLength) {
            return List.of();
        }

        private static List<Integer> parseJoltages(final String zappen) {
            return UtParsing.commaSeparatedStringToIntStream(zappen)
                    .boxed()
                    .toList();
        }

        public int determineButtonPokenation() {
            return IntStream.range(1, 1 << this.buttons().length)
                    .map(this::pokenBlinkenLights)
                    .min()
                    .orElseThrow();
        }

        private int pokenBlinkenLights(final int combination) {
            int buttonsPoken = 0;
            int activatedLights = 0;
            for (int button = 0; button < buttons.length; button++) {
                if ((combination & (1 << button)) >= 1) {
                    buttonsPoken++;
                    activatedLights ^= buttons()[button];
                }
            }
            if (activatedLights == lights()) {
                return buttonsPoken;
            }
            return Integer.MAX_VALUE;
        }

        public int determineJoltageZappenation() {
            return 0;
        }

        @Override
        public String toString() {
            // Rebuild the String description specifically to check whether my representation accurately reflects the input.
            final String lightsDescription = Integer.toBinaryString(lights()).replace('0', '.')
                    .replace('1', '#');
            final String buttonsDescription = Arrays.stream(buttons)
                    .mapToObj(this::stringifyButton)
                    .collect(Collectors.joining(" "));
            return String.format("[%s] %s %s", lightsDescription, buttonsDescription, joltages);
        }

        private String stringifyButton(final int button) {
            final String binary = String.format(String.format("%%%ss", joltages().size()), Integer.toBinaryString(button)).replace(' ', '0');
            return "(" + IntStream.range(0, binary.length())
                    .filter(i -> binary.charAt(i) == '1')
                    .mapToObj(Integer::toString)
                    .collect(Collectors.joining(",")) + ")";
        }
    }
}
