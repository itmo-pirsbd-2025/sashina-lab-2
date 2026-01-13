package ru.spb.itmo.pirsbd.asashina.core;

import java.util.Arrays;
import java.util.List;

public record BallTreeNode(BallTreePoint pivot, int radius, List<BallTreePoint> points) {

    @Override
    public String toString() {
        return "TreeNode: { pivot: " + pivot.toString()
                + "; radius: " + radius
                + "; points: " + points.toString() + "}";
    }

    public record BallTreePoint(int[] coordinates, int index) {

        @Override
        public String toString() {
            return "TreePoint: { coordinates: " + Arrays.toString(coordinates) + "; index: " + index + "}";
        }

    }

}
