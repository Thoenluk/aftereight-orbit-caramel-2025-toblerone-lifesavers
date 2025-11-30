package ch.thoenluk.ut;

import java.util.LinkedList;
import java.util.List;

public record ThreeDArea(ThreeDPosition bottomLeft, ThreeDPosition topRight) {

    public ThreeDArea {
        if (bottomLeft.x() > topRight.x() || bottomLeft.y() > topRight.y() || bottomLeft.z() > topRight.z())
            throw new AssertionError();
    }

    public long getVolume() {
        return ((long) topRight().x() - bottomLeft().x() + 1)
                * (topRight().y() - bottomLeft().y() + 1)
                * (topRight().z() - bottomLeft().z() + 1);
    }

    public boolean overlaps(ThreeDArea other) {
        return this.topRight.x() >= other.bottomLeft.x()
                && this.topRight.y() >= other.bottomLeft.y()
                && this.topRight.z() >= other.bottomLeft.z()
                && this.bottomLeft.x() <= other.topRight.x()
                && this.bottomLeft.y() <= other.topRight.y()
                && this.bottomLeft.z() <= other.topRight.z();
    }

    public ThreeDArea getOverlap(ThreeDArea other) {
        if (!overlaps(other)) {
            return null;
        }

        final ThreeDPosition overlapBottomLeft = new ThreeDPosition(
                Math.max(this.bottomLeft.x(), other.bottomLeft.x()),
                Math.max(this.bottomLeft.y(), other.bottomLeft.y()),
                Math.max(this.bottomLeft.z(), other.bottomLeft.z())
        );

        final ThreeDPosition overlapTopRight = new ThreeDPosition(
                Math.min(this.topRight.x(), other.topRight.x()),
                Math.min(this.topRight.y(), other.topRight.y()),
                Math.min(this.topRight.z(), other.topRight.z())
        );

        return new ThreeDArea(overlapBottomLeft, overlapTopRight);
    }

    public List<ThreeDArea> subtract(ThreeDArea other) {
        final List<ThreeDArea> pieces = new LinkedList<>();
        ThreeDArea remainder = this;
        final ThreeDArea overlap = getOverlap(other);

        if (null == overlap) {
            return List.of(this);
        }

        if (remainder.bottomLeft.x() < overlap.bottomLeft.x()) {
            pieces.add(
                    new ThreeDArea(
                            remainder.bottomLeft,
                            new ThreeDPosition(
                                    overlap.bottomLeft.x() - 1,
                                    remainder.topRight.y(),
                                    remainder.topRight.z()
                            )
                    )
            );
            remainder = new ThreeDArea(
                    new ThreeDPosition(
                            overlap.bottomLeft.x(),
                            remainder.bottomLeft.y(),
                            remainder.bottomLeft.z()
                    ),
                    remainder.topRight
            );
        }

        if (remainder.bottomLeft.y() < overlap.bottomLeft.y()) {
            pieces.add(
                    new ThreeDArea(
                            remainder.bottomLeft,
                            new ThreeDPosition(
                                    remainder.topRight.x(),
                                    overlap.bottomLeft.y() - 1,
                                    remainder.topRight.z()
                            )
                    )
            );
            remainder = new ThreeDArea(
                    new ThreeDPosition(
                            remainder.bottomLeft.x(),
                            overlap.bottomLeft.y(),
                            remainder.bottomLeft.z()
                    ),
                    remainder.topRight
            );
        }


        if (remainder.bottomLeft.z() < overlap.bottomLeft.z()) {
            pieces.add(
                    new ThreeDArea(
                            remainder.bottomLeft,
                            new ThreeDPosition(
                                    remainder.topRight.x(),
                                    remainder.topRight.y(),
                                    overlap.bottomLeft.z() - 1
                            )
                    )
            );
            remainder = new ThreeDArea(
                    new ThreeDPosition(
                            remainder.bottomLeft.x(),
                            remainder.bottomLeft.y(),
                            overlap.bottomLeft.z()
                    ),
                    remainder.topRight
            );
        }

        if (remainder.topRight.x() > overlap.topRight.x()) {
            pieces.add(
                    new ThreeDArea(
                            new ThreeDPosition(
                                    overlap.topRight.x() + 1,
                                    remainder.bottomLeft.y(),
                                    remainder.bottomLeft.z()
                            ),
                            remainder.topRight
                    )
            );
            remainder = new ThreeDArea(
                    remainder.bottomLeft,
                    new ThreeDPosition(
                            overlap.topRight.x(),
                            remainder.topRight.y(),
                            remainder.topRight.z()
                    )
            );
        }

        if (remainder.topRight.y() > overlap.topRight.y()) {
            pieces.add(
                    new ThreeDArea(
                            new ThreeDPosition(
                                    remainder.bottomLeft.x(),
                                    overlap.topRight.y() + 1,
                                    remainder.bottomLeft.z()
                            ),
                            remainder.topRight
                    )
            );
            remainder = new ThreeDArea(
                    remainder.bottomLeft,
                    new ThreeDPosition(
                            remainder.topRight.x(),
                            overlap.topRight.y(),
                            remainder.topRight.z()
                    )
            );
        }

        if (remainder.topRight.z() > overlap.topRight.z()) {
            pieces.add(
                    new ThreeDArea(
                            new ThreeDPosition(
                                    remainder.bottomLeft.x(),
                                    remainder.bottomLeft.y(),
                                    overlap.topRight.z() + 1
                            ),
                            remainder.topRight
                    )
            );
            remainder = new ThreeDArea(
                    remainder.bottomLeft,
                    new ThreeDPosition(
                            remainder.topRight.x(),
                            remainder.topRight.y(),
                            overlap.topRight.z()
                    )
            );
        }

        if (!remainder.equals(overlap)) throw new AssertionError();

        return pieces;
    }

    public List<ThreeDPosition> getPositionsWithin() {
        List<ThreeDPosition> positions = new LinkedList<>();

        for (int x = bottomLeft().x(); x <= topRight().x(); x++) {
            for (int y = bottomLeft().y(); y <= topRight().y(); y++) {
                for (int z = bottomLeft().z(); z <= topRight().z(); z++) {
                    positions.add(new ThreeDPosition(x, y, z));
                }
            }
        }

        return positions;
    }

    public boolean containsPosition(ThreeDPosition position) {
        return bottomLeft.x() <= position.x()
                && topRight.x() >= position.x()
                && bottomLeft.y() <= position.y()
                && topRight.y() >= position.y()
                && bottomLeft.z() <= position.z()
                && topRight.z() >= position.z();
    }
}
