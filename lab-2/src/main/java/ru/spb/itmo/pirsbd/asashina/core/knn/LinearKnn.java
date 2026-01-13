package ru.spb.itmo.pirsbd.asashina.core.knn;

import ru.spb.itmo.pirsbd.asashina.utils.KnnUtils.DistanceIndex;

import java.util.Comparator;
import java.util.PriorityQueue;

import static ru.spb.itmo.pirsbd.asashina.utils.KnnUtils.euclideanDistance;
import static ru.spb.itmo.pirsbd.asashina.utils.KnnUtils.updateKNeighbors;

public class LinearKnn implements Knn {

    private final int[][] data;
    private final int neighboursAmount;

    public LinearKnn(int[][] data, int neighboursAmount) {
        this.data = data;
        this.neighboursAmount = neighboursAmount;
    }

    @Override
    public int[][] getKNeighbours(int[] x) {
        var queue = new PriorityQueue<DistanceIndex>();
        for (var i = 0; i < this.data.length; i++) {
            var current = this.data[i];
            if (current == x) {
                continue;
            }
            var distance = euclideanDistance(current, x);
            updateKNeighbors(queue, neighboursAmount, distance, i);
        }
        return queue.stream()
                .sorted(Comparator.comparingDouble(DistanceIndex::distance))
                .map(di -> this.data[di.index()])
                .toArray(int[][]::new);
    }

}
