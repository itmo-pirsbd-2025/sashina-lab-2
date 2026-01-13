package ru.spb.itmo.pirsbd.asashina.core;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.apache.commons.lang3.ArrayUtils;
import ru.spb.itmo.pirsbd.asashina.utils.KnnUtils;

import java.util.*;

import static ru.spb.itmo.pirsbd.asashina.utils.KnnUtils.euclideanDistance;
import static ru.spb.itmo.pirsbd.asashina.utils.KnnUtils.updateKNeighbors;

public class BallTreeV2 {

    private BallTreeNode.BallTreePoint[] points;

    private final int leafSize;
    private final int[][] data;
    private final int neighborsAmount;
    private final Int2ObjectMap<BallTreeNode> nodes = new Int2ObjectOpenHashMap<>();
    private final PriorityQueue<KnnUtils.DistanceIndex> kNeighbors = new PriorityQueue<>();

    public BallTreeV2(int neighborsAmount, int leafSize, int[][] data) {
        this.neighborsAmount = neighborsAmount;
        this.points = new BallTreeNode.BallTreePoint[data.length];
        this.leafSize = leafSize;
        this.data = data;

        for (var i = 0; i < data.length; i++) {
            points[i] = new BallTreeNode.BallTreePoint(data[i], i);
        }

        createTree(1, 0, points.length);
    }

    public void clearKNeighbors() {
        kNeighbors.clear();
    }

    public PriorityQueue<KnnUtils.DistanceIndex> getKNeighbors() {
        return kNeighbors;
    }

    public int[][] getData() {
        return data;
    }

    public void search(int vertexIndex, int[] newPoint) {
        var currentNode = nodes.get(vertexIndex);
        if (currentNode == null) {
            return;
        }

        var currentNodePoints = currentNode.points();
        if (currentNodePoints.size() > 1) {
            for (var point : currentNodePoints) {
                if (point.coordinates() == newPoint) {
                    continue;
                }
                double distance = euclideanDistance(newPoint, point.coordinates());
                updateKNeighbors(kNeighbors, neighborsAmount, distance, point.index());
            }
            return;
        }

        var currentNodePivot = currentNode.pivot();
        var leftChild = vertexIndex * 2;
        var rightChild = vertexIndex * 2 + 1;
        double pivotDistance = euclideanDistance(newPoint, currentNodePivot.coordinates());
        if (newPoint != currentNodePivot.coordinates()) {
            updateKNeighbors(kNeighbors, neighborsAmount, pivotDistance, currentNodePivot.index());
        }

        searchInSubTree(leftChild, newPoint);
        searchInSubTree(rightChild, newPoint);
    }

    @Override
    public String toString() {
        var bs = new StringBuilder("TreeV2: {");
        for (var node : nodes.int2ObjectEntrySet()) {
            bs.append("Node index: ")
                    .append(node.getIntKey())
                    .append("; node: ")
                    .append(node.getValue())
                    .append("\n");
        }
        bs.append("}");
        return bs.toString();
    }

    private void createTree(int vertexIndex, int startPoint, int endPoint) {
        if (endPoint - startPoint <= 0) {
            return;
        }

        var subPoints = ArrayUtils.subarray(points, startPoint, endPoint);
        var dimension = getMaxSpreadDimension(subPoints);
        this.points = getSortedPointsByDimension(startPoint, endPoint, dimension);
        var centroidIndex = startPoint + getCentroidIndex(subPoints, dimension);
        var radius = getMaxDistance(points[centroidIndex], subPoints);
        var currentNode = new BallTreeNode(points[centroidIndex], radius, new ArrayList<>());
        nodes.put(vertexIndex, currentNode);

        if (endPoint - startPoint <= leafSize) {
            Collections.addAll(currentNode.points(), subPoints);
        } else {
            currentNode.points().add(points[centroidIndex]);
            createTree(vertexIndex * 2, startPoint, centroidIndex);
            createTree(vertexIndex * 2 + 1, centroidIndex + 1, endPoint);
        }
    }

    private int getMaxSpreadDimension(BallTreeNode.BallTreePoint[] points) {
        if (points.length == 0) {
            throw new IllegalArgumentException("Points are empty");
        }

        var dimensions = points[0].coordinates().length;
        var differences = getDimensionValuesDifferences(dimensions);
        var maxDimension = 0;
        for (var i = 1; i < differences.size(); i++) {
            if (differences.getInt(i) > differences.getInt(maxDimension)) {
                maxDimension = i;
            }
        }
        return maxDimension;
    }

    private IntList getDimensionValuesDifferences(int dimensions) {
        var differences = new IntArrayList(dimensions);
        for (var i = 0; i < dimensions; i++) {
            int maxVal = Integer.MIN_VALUE;
            int minVal = Integer.MAX_VALUE;
            for (var p : points) {
                var val = p.coordinates()[i];
                if (val > maxVal) {
                    maxVal = val;
                }
                if (val < minVal) {
                    minVal = val;
                }
            }
            differences.add(maxVal - minVal);
        }
        return differences;
    }

    private BallTreeNode.BallTreePoint[] getSortedPointsByDimension(int startPoint, int endPoint, int dimension) {
        var result = Arrays.copyOf(points, points.length);
        Arrays.sort(ArrayUtils.subarray(result, startPoint, endPoint), Comparator.comparingInt(p -> p.coordinates()[dimension]));
        return result;
    }

    private int getCentroidIndex(BallTreeNode.BallTreePoint[] points, int dimension) {
        if (points.length == 0) {
            throw new IllegalArgumentException("Points are empty");
        }

        var sum = 0.0;
        for (var point : points) {
            sum += point.coordinates()[dimension];
        }
        double average = sum / points.length;
        var minDiff = Double.MAX_VALUE;
        var closestIndex = 0;
        for (var i = 0; i < points.length; i++) {
            double diff = Math.abs(points[i].coordinates()[dimension] - average);
            if (diff < minDiff) {
                minDiff = diff;
                closestIndex = i;
            }
        }
        return closestIndex;
    }

    private int getMaxDistance(BallTreeNode.BallTreePoint point, BallTreeNode.BallTreePoint[] points) {
        var max = 0.0;
        var coordinates = point.coordinates();

        for (var p : points) {
            var currentDist = euclideanDistance(coordinates, p.coordinates());
            if (currentDist > max) {
                max = currentDist;
            }
        }
        return (int) max;
    }

    private void searchInSubTree(int child, int[] newPoint) {
        var node = nodes.get(child);
        if (node != null && !node.points().isEmpty() && node.pivot().coordinates() != newPoint) {
            var childDistance = euclideanDistance(newPoint, node.pivot().coordinates());
            if (kNeighbors.size() < neighborsAmount
                    || childDistance - node.radius() < kNeighbors.peek().distance()) {
                search(child, newPoint);
            }
        }
    }

}
