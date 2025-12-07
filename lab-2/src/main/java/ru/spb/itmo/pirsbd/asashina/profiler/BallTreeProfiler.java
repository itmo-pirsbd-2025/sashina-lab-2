package ru.spb.itmo.pirsbd.asashina.profiler;

import ru.spb.itmo.pirsbd.asashina.core.knn.TreeKnn;

import static ru.spb.itmo.pirsbd.asashina.benchmark.BenchmarkValues.DATA_FILEPATH;

public class BallTreeProfiler {

    public static void main(String[] args) {
        var knn = new TreeKnn(DATA_FILEPATH, 10_000, 5_000, 100);
        knn.getData();
    }

}
