package ch.thoenluk.ut;

public record Area(Position bottomLeft, Position topRight) {

    public Area {
        if (bottomLeft.y() > topRight.y()
                || bottomLeft.x() > topRight.x()) throw new AssertionError();
    }

    public boolean containsPosition(Position position) {
        return bottomLeft.y() <= position.y()
                && bottomLeft.x() <= position.x()
                && topRight.y() >= position.y()
                && topRight.x() >= position.x();
    }
}
