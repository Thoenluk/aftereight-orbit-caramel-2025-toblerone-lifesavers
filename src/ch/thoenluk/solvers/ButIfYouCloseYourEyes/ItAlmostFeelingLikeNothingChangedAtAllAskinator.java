package ch.thoenluk.solvers.ButIfYouCloseYourEyes;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.InputFormat;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtStrings;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.thoenluk.InputParser.STREAMED_LINES;

@Day(11)
@InputFormat(STREAMED_LINES)
public class ItAlmostFeelingLikeNothingChangedAtAllAskinator implements ChristmasSaver<Stream<String>> {
    private static final ScopedValue<Map<String, Router>> NETWORK = ScopedValue.newInstance();
    private static final ScopedValue<Stream<String>> INPUT = ScopedValue.newInstance();

    @Override
    public String saveChristmas(Stream<String> input) {
        return ScopedValue.where(NETWORK, new HashMap<>()).where(INPUT, input).call(this::doTheThing);
    }

    private String doTheThing() {
        // Could do this earlier? Yes. Will? No.
        parseNetwork();
        return Long.toString(NETWORK.get().get("you").getPathsToOut());
    }

    private static void parseNetwork() {
        final Map<String, Router> parsed = INPUT.get()
                .map(Device::fromDescription)
                .collect(Collectors.toMap(Device::label, Function.identity()));
        NETWORK.get().putAll(parsed);
        NETWORK.get().put("out", new Reactor());
    }

    @Override
    public String saveChristmasAgain(Stream<String> input) {
        return ScopedValue.where(NETWORK, new HashMap<>()).where(INPUT, input).call(this::doTheOtherThing);
    }

    private String doTheOtherThing() {
        parseNetwork();
        final Map<String, Router> network = NETWORK.get();
        final Router dac = network.get("dac");
        final Router fft = network.get("fft");
        final Router svr = network.get("svr");
        final long dacThenFft = svr.getPathsToDac() * dac.getPathsToFft() * fft.getPathsToOut();
        final long fftThenDac = svr.getPathsToFft() * fft.getPathsToDac() * dac.getPathsToOut();
        return Long.toString(dacThenFft + fftThenDac); // Only one of these is nonzero, and I am too lazy to write an if.
        // For anyone confused, here's the idea:
        //
        // We can safely assume that there are no cycles in the network. If there were, the number of paths may be infinite
        // or, at any rate, the description would have to tell us to ignore cycles for some extra challenge why not.
        //
        // So it's a tree, not a graph. No cycles. We can deduce that only one of DAC and FFT will ever lead into the other.
        // Suppose that it's FFT which leads into DAC, as in the example. Consequently, EVERY path that leads from SVR
        // to OUT while passing through DAC and FFT will contain one of the paths from FFT into DAC.
        // In the example, note that that is only FFT -> CCC -> EEE -> DAC, so one path.
        //
        // Additionally, we know that every valid path converges on FFT, again on DAC, then ends at OUT. Every path that
        // passes one of them by is not valid. This means that the number of valid paths from SVR to DAC MUST equal the
        // number of paths from SVR to FFT times the number of paths from FFT to DAC (again, if FFT leads into DAC).
        // The same way, the number of valid paths from SVR to OUT equals that number of paths from SVR to DAC times the
        // number of paths from DAC to OUT.
        //
        // From there, apply the solution from part 1. Rare that you can do that this year. Finding the number of paths
        // from a given start to finish is trivial with the Recursion Breaker 5000tron...inator!
        // But seriously, remember the pattern shown by the getPathsToOut method. It's strictly speaking just a depth-first
        // tree search, but it's so simple you don't even see it as that. Do the recursive calculation once, then cache
        // the result since it won't change. Done.
    }

    private static class Device implements Router {
        private final String label;
        private final List<String> outputs;
        private long pathsToOut = -1;
        private long pathsToDac = -1;
        private long pathsToFft = -1;

        public static Device fromDescription(final String description) {
            final String[] parts = description.split(": ");
            final String label = parts[0];
            final List<String> outputs = Arrays.stream(parts[1].split(UtStrings.WHITE_SPACE_REGEX)).toList();
            return switch (label) {
                case "fft" -> new Fft(label, outputs);
                case "dac" -> new Dac(label, outputs);
                default -> new Device(label, outputs);
            };
        }

        private Device(final String label, final List<String> outputs) {
            this.label = label;
            this.outputs = outputs;
        }

        @Override
        public long getPathsToOut() {
            if (pathsToOut == -1) {
                pathsToOut = calculatePaths(Router::getPathsToOut);
            }
            return pathsToOut;
        }

        // If it's stupid but it works, it's still stupid and I'm lazy :3
        // I could make these ints into null Integers, but... nah.
        @Override
        public long getPathsToDac() {
            if (pathsToDac == -1) {
                pathsToDac = calculatePaths(Router::getPathsToDac);
            }
            return pathsToDac;
        }

        @Override
        public long getPathsToFft() {
            if (pathsToFft == -1) {
                pathsToFft = calculatePaths(Router::getPathsToFft);
            }
            return pathsToFft;
        }

        private long calculatePaths(final ToLongFunction<Router> pathFunction) {
            return outputs.stream()
                    .map(NETWORK.get()::get)
                    .mapToLong(pathFunction)
                    .reduce(UtMath::superOverflowSafeSum)
                    .orElseThrow();
        }

        public String label() {
            return label;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Device) obj;
            return Objects.equals(this.outputs, that.outputs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(outputs);
        }

        @Override
        public String toString() {
            return "Device[" +
                    "label=" + label + ']' +
                    "outputs=" + outputs + ']';
        }
    }

    private static class Dac extends Device {
        private Dac(String label, List<String> outputs) {
            super(label, outputs);
        }

        @Override
        public long getPathsToDac() {
            return 1;
        }
    }

    private static class Fft extends Device {
        private Fft(String label, List<String> outputs) {
            super(label, outputs);
        }

        @Override
        public long getPathsToFft() {
            return 1;
        }
    }

    private record Reactor() implements Router {
        @Override
        public long getPathsToOut() {
            return 1;
        }

        @Override
        public long getPathsToDac() {
            return 0;
        }

        @Override
        public long getPathsToFft() {
            return 0;
        }
    }
}
