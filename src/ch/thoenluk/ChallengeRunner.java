package ch.thoenluk;

import ch.thoenluk.ut.UtParsing;
import ch.thoenluk.ut.UtStrings;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static ch.thoenluk.ut.UtStrings.println;

/**
 * @author Lukas Thöni lukas.thoeni@gmx.ch
 * A note to any current or future employers: This class is where I experiment each year to see how fancy I can make it.
 * Please see other classes for examples of how I would actually write productive code.
 */
public class ChallengeRunner {
    private final static Scanner USER_INPUT = new Scanner(System.in);
    private final static String FIRST_CHALLENGE_SUFFIX = "1";
    private final static String SECOND_CHALLENGE_SUFFIX = "2";
    public static final int NANO_TO_MILLI = 1_000_000;
    private final List<? extends Class<? extends ChristmasSaver<?>>> christmasSaverClasses = findChristmasSaverClasses();
    private Supplier<int[]> daySelectionStrategy = this::getSelectedChallengeFromUser;
    private Function<Integer, Long> executionStrategy = this::testAndRunChristmasSaver;
    private int[] argsChallenges = null;

    void main(final String[] args) {
        printChallengeFolderNames();
        pickStrategies(args);
        executeChristmasSavers();
    }

    private void printChallengeFolderNames() {
        println("Scanning for challenge folders...");
        println(String.format("Found %s challenges: ", christmasSaverClasses.size()));
        final StringBuilder output = new StringBuilder();
        for (int i = 0; i < christmasSaverClasses.size(); i++) {
            output.append(String.format("%s:\t %s\n", i, getChallengeFolderName(christmasSaverClasses.get(i))));
        }
        println(output.toString());
    }

    private String getChallengeFolderName(final Class<? extends ChristmasSaver<?>> christmasSaverClass) {
        final String fullPackageName = christmasSaverClass.getPackage().getName();
        return fullPackageName.substring(fullPackageName.lastIndexOf('.') + 1);
    }

    private void pickStrategies(final String[] args) {
        final String flag;
        if (args.length == 0) {
            println("No flags detected. Proceeding with default. Btw: You can use these flags: 'default', 'latest', 'latest-second-only', 'speedrun', 'challenges'.\n" + "For 'challenges', also give the challenges you want in a space-separated list.");
            flag = "default";
        } else {
            flag = args[0];
        }
        switch (flag) {
            case "default" -> {
                daySelectionStrategy = this::getSelectedChallengeFromUser;
                executionStrategy = this::testAndRunChristmasSaver;
            }
            case "latest" -> {
                daySelectionStrategy = this::selectMostRecentChallenge;
                executionStrategy = this::testAndRunChristmasSaver;
            }
            case "latest-second-only" -> {
                daySelectionStrategy = this::selectMostRecentChallenge;
                executionStrategy = this::onlyTestAndRunSecondChallenge;
            }
            case "speedrun" -> {
                daySelectionStrategy = this::selectAllChallenges;
                executionStrategy = this::onlyRunChristmasSaver;
            }
            case "challenges" -> {
                daySelectionStrategy = this::selectChallengesFromArgs;
                executionStrategy = this::testAndRunChristmasSaver;
                argsChallenges = Arrays.stream(args, 1, args.length).mapToInt(UtParsing::cachedParseInt).toArray();
            }
        }
    }

    private void executeChristmasSavers() {
        final int[] challenges = daySelectionStrategy.get();
        final List<Long> executionTimes = Arrays.stream(challenges).mapToObj(executionStrategy::apply).toList();
        final long total = executionTimes.stream().reduce(0L, Long::sum);
        println(String.format("Took a total of %sms for it all!", total / NANO_TO_MILLI));
        if (executionTimes.size() > 2) {
            final long average = total / executionTimes.size();
            final List<Long> sorted = executionTimes.stream().sorted().toList();
            final long median = sorted.get(sorted.size() / 2);
            final long totalWithoutWorstOffenders = sorted.subList(0, (sorted.size() * 9) / 10).stream().reduce(0L, Long::sum);
            final long averageWithoutWorstOffenders = totalWithoutWorstOffenders / ((sorted.size() * 9L) / 10L);
            println(String.format("""
                    Average runtime was %sms with a median of %sms.
                    If we ignore the 10%% longest running challenges (that I don't care to optimise more), the total is %sms!
                    (That's an average of only %sms!)""", average / NANO_TO_MILLI, median / NANO_TO_MILLI, totalWithoutWorstOffenders / NANO_TO_MILLI, averageWithoutWorstOffenders / NANO_TO_MILLI));
        }
    }

    private int[] selectMostRecentChallenge() {
        println("Proceeding with highest challenge.");
        return new int[]{christmasSaverClasses.size() - 1};
    }

    private int[] selectAllChallenges() {
        println("Proceeding with all challenges.");
        return IntStream.range(0, christmasSaverClasses.size()).toArray();
    }

    private int[] selectChallengesFromArgs() {
        println(String.format("Proceeding with challenges given as args: %s", (Object) argsChallenges));
        return argsChallenges;
    }

    private int[] getSelectedChallengeFromUser() {
        println("Now choose one.");
        int selectedChallenge = -1;
        while (selectedChallenge < 0) {
            selectedChallenge = USER_INPUT.nextInt();

            if (selectedChallenge < 0 || christmasSaverClasses.size() < selectedChallenge) {
                println("Only and exactly one of the above numbers shall you choose.");
                selectedChallenge = -1;
            }
        }
        return new int[]{selectedChallenge};
    }

    private <I> long testAndRunChristmasSaver(final int selectedChallenge) {
        final ChristmasSavingPackage<I> result = wrapChristmasSavingPackage(selectedChallenge);

        println("Running first challenge...");
        testChristmasSaver(result.challengeFolder(), result.christmasSaver()::saveChristmas, FIRST_CHALLENGE_SUFFIX, result.inputParsingFunction());
        final long timeForFirst = runChristmasSaver(result.christmasSaver()::saveChristmas, result.input(), result.inputParsingFunction());
        println("What fun that was. Running second challenge...");
        testChristmasSaver(result.challengeFolder(), result.christmasSaver()::saveChristmasAgain, SECOND_CHALLENGE_SUFFIX, result.inputParsingFunction());
        return timeForFirst + runChristmasSaver(result.christmasSaver()::saveChristmasAgain, result.input(), result.inputParsingFunction());
    }

    private <I> long onlyRunChristmasSaver(final int selectedChallenge) {
        final ChristmasSavingPackage<I> result = wrapChristmasSavingPackage(selectedChallenge);
        println(String.format("Challenge %s:", selectedChallenge));
        return runChristmasSaver(result.christmasSaver()::saveChristmas, result.input(), result.inputParsingFunction())
                + runChristmasSaver(result.christmasSaver()::saveChristmasAgain, result.input(), result.inputParsingFunction());
    }

    private <I> long onlyTestAndRunSecondChallenge(final int selectedChallenge) {
        final ChristmasSavingPackage<I> result = wrapChristmasSavingPackage(selectedChallenge);
        testChristmasSaver(result.challengeFolder(), result.christmasSaver()::saveChristmasAgain, SECOND_CHALLENGE_SUFFIX, result.inputParsingFunction());
        return runChristmasSaver(result.christmasSaver()::saveChristmasAgain, result.input(), result.inputParsingFunction());
    }

    private <I> ChristmasSavingPackage<I> wrapChristmasSavingPackage(final int selectedChallenge) {
        // Wrap as in wrapping paper AND as in object.
        // I only intended the wrapping paper meaning and I too hate this unintentional pun.
        final Class<? extends ChristmasSaver<I>> christmasSaverClass = (Class<? extends ChristmasSaver<I>>) christmasSaverClasses.get(selectedChallenge);
        final ChristmasSaver<I> christmasSaver = instantiateChristmasSaver(christmasSaverClass);
        final Package christmasSaverClassPackage = christmasSaverClass.getPackage();
        final File challengeFolder = new File("src\\" + christmasSaverClassPackage.getName().replaceAll("\\.", "\\\\"));
        final String input = findActualInput(challengeFolder);
        final InputParser inputParser = christmasSaverClass.getAnnotation(InputFormat.class).value();
        return new ChristmasSavingPackage<>(christmasSaver, challengeFolder, input, inputParser::parse);
    }

    private static String findActualInput(final File challengeFolder) {
        final File[] actualInputFiles = challengeFolder.listFiles((_, name) -> name.equals("input.txt"));

        if (actualInputFiles == null) throw new AssertionError();
        if (actualInputFiles.length != 1) throw new AssertionError();

        return UtStrings.readFile(actualInputFiles[0]);
    }

    private static <I> long runChristmasSaver(final Function<I, String> savingMethod, final String input, final Function<String, I> inputParsingFunction) {
        println("Determined the result for the challenge is:");
        final I actualInput = inputParsingFunction.apply(input);
        final long nanosBeforeStart = System.nanoTime();
        println(savingMethod.apply(actualInput));
        final long executionTime = System.nanoTime() - nanosBeforeStart;
        println(String.format("And did it in %sms!", executionTime / NANO_TO_MILLI));
        return executionTime;
    }

    // I do not fear what this method does; I fear what kind of further automation I'll think up next year.
    // 2023 update: I was correct to fear.
    // 2024 update: I suffered a dream of steel and oil. Of a hellscape of good intentions - to pre-parse input based on the
    //              desired input type as declared in an annotation. In my fervor to create a lazier program, saving one
    //              entire method call per saveChristmas, I had made a hellscape of Reflection and Object.
    //              I awoke when I realised that while it certainly worked, it removed all premise of type-safety, nay,
    //              even the concept of calling a method on an object rather than passing that object as a parameter.
    //              I abandoned the concept. Types are our greatest asset in Java. Some experiments should not be done.
    // Next day update: That being said, I didn't say I was NOT going to get lazier with annotations and make a nightmare
    //                  of Reflection and possibly steel and oil. More wine, yum yum!
    //                  I could package the classes into a record including their package; That'd remove the admittedly
    //                  less-than-guaranteed operation in wrapChristmasSavingPackage, because there is little guarantee
    //                  a class was actually loaded from the file system. But in this case, there is, because I'm scanning
    //                  the file system to begin with. And I wanted to know if I COULD find the folder from the class.
    // 2025 update: The dream of steel and oil I mentioned last year? It is done. I sought that maybe, if I gave it form,
    //              if I made it real, it would give peace to my mind. I was only the unwitting pawn of a thing that came
    //              into existence from its own spite. Note the shiny new generic input format type <I> and the InputFormat
    //              annotation that defines how to create the expected format. Note how consistently it is used across
    //              this class, and how it never does anything other than soothing warnings. While it is technically
    //              enforced that ChristmasSaver and input format match, neither of them have the least guarantee that
    //              they actually do. Both are created on little more basis than telling the compiler "bro trust me."
    //              Note also that every <I> in this class is method scoped and as such only loosely associated. The
    //              ChallengeRunner isn't typed, the ChristmasSaver is! All of this stuff is static anyway because, in the
    //              back-aching days of, like, 2023, a main method still had to be static and I was too lazy to create an instance.
    private static <I> List<? extends Class<? extends ChristmasSaver<I>>> findChristmasSaverClasses() {
        try (final Stream<Path> paths = Files.find(Paths.get(".", "src", "ch", "thoenluk", "solvers"), Integer.MAX_VALUE, ChallengeRunner::isJavaFile)) {
            return paths.map(Path::toString).map(ChallengeRunner::toSearchableFilePath).map(filePath -> {
                try {
                    return Class.forName(filePath);
                } catch (final ClassNotFoundException e) {
                    throw new AssertionError(e);
                }
            }).filter(ChristmasSaver.class::isAssignableFrom)
                    .map(christmasSaverClass -> (Class<? extends ChristmasSaver<I>>) christmasSaverClass.asSubclass(ChristmasSaver.class))
                    .sorted(Comparator.comparing(ChallengeRunner::findDayValue))
                    .toList();
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    private static boolean isJavaFile(final Path path, final BasicFileAttributes attrs) {
        return path.toString().endsWith(".java");
    }

    private static String toSearchableFilePath(final String filePath) {
        return filePath.substring(6, filePath.length() - 5).replaceAll("\\\\", ".");
    }

    private static int findDayValue(final Class<?> christmasSaverClass) {
        return Optional.ofNullable(christmasSaverClass.getAnnotation(Day.class)).map(Day::value).orElse(-1);
    }

    private static <I> ChristmasSaver<I> instantiateChristmasSaver(final Class<? extends ChristmasSaver<I>> christmasSaverClass) {
        try {
            return christmasSaverClass.getConstructor().newInstance();
        } catch (final ClassCastException | InstantiationException | IllegalAccessException |
                       InvocationTargetException | NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    private static <I> void testChristmasSaver(final File challengeFolder, final Function<I, String> savingMethod, final String challengeSuffix, final Function<String, I> inputParsingFunction) {
        final String inputPrefix = String.format("test%s_input", challengeSuffix);
        final String outputPrefix = String.format("test%s_output", challengeSuffix);

        final File[] testInputs = challengeFolder.listFiles((_, fileName) -> fileName.startsWith(inputPrefix));
        final File[] testOutputs = challengeFolder.listFiles((_, fileName) -> fileName.startsWith(outputPrefix));

        if (testInputs == null) throw new AssertionError();
        if (testOutputs == null) throw new AssertionError();

        if ((testInputs.length != testOutputs.length)) throw new AssertionError();

        Arrays.sort(testInputs);
        Arrays.sort(testOutputs);

        try (final StructuredTaskScope<Void, Void> scope = StructuredTaskScope.open()) {
            for (int i = 0; i < testInputs.length; i++) {
                final File testInput = testInputs[i];
                final File testOutput = testOutputs[i];
                scope.fork(() -> executeTestCase(savingMethod, testInput, testOutput, inputParsingFunction));
            }
            scope.join();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static <I> void executeTestCase(final Function<I, String> savingMethod, final File testInput, final File testOutput, final Function<String, I> inputParsingFunction) {
        UtStrings.print(String.format("Running test %s... ", testInput.getName()));
        final String testInputString = UtStrings.readFile(testInput);
        final I actualInput = inputParsingFunction.apply(testInputString);
        final String testOutputString = UtStrings.readFile(testOutput);
        final String actualOutput = savingMethod.apply(actualInput);

        if (!actualOutput.equals(testOutputString)) {
            final String message = String.format("""
                    Failed test %s
                    Input was:
                    %s
                    And expected output was:
                    %s
                    But actual output was:
                    %s""", testInput.getName(), testInputString, testInputString, actualOutput);
            throw new AssertionError(message);
        }

        println(String.format("Matched %s", testOutput.getName()));
        // return 0; // Forked processes must return something, but we don't care about the return - if we fail, we throw!
        // That used to be true, but by the magic of Java 25, no more! Hooray for typed scopes!
    }


    private record ChristmasSavingPackage<I>(ChristmasSaver<I> christmasSaver, File challengeFolder, String input, Function<String, I> inputParsingFunction) {
    }
}
