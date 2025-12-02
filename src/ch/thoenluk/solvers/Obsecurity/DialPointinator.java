package ch.thoenluk.solvers.Obsecurity;

import ch.thoenluk.ut.UtMath;

public class DialPointinator {
    private static final int DIAL_SIZE = 100;

    private int dial = 50;
    private int timesPointedAtZero = 0;
    private boolean countCrossings = false;

    public void countCrossings() {
        this.countCrossings = true;
    }

    public void turn(final int turning) {
        if (countCrossings) {
            turnWithCountingCrossings(turning);
        } else {
            turnWithoutCountingCrossings(turning);
        }
    }

    public int getTimesPointedAtZero() {
        return timesPointedAtZero;
    }

    private void turnWithoutCountingCrossings(final int turning) {
        dial = UtMath.modForNormalPeople(dial + turning, DIAL_SIZE);
        if (dial == 0) {
            timesPointedAtZero++;
        }
    }

    private void turnWithCountingCrossings(final int turning) {
        timesPointedAtZero += Math.abs(turning / DIAL_SIZE);
        final int turningWithinOneRotation = turning % DIAL_SIZE;
        final boolean dialWasNot0 = dial != 0;
        dial += turningWithinOneRotation;
        if ((dial <= 0 && dialWasNot0) || dial >= DIAL_SIZE) {
            timesPointedAtZero++;
        }
        dial = UtMath.modForNormalPeople(dial, DIAL_SIZE);
    }
}
