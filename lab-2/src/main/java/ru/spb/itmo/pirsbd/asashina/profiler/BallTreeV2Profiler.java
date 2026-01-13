package ru.spb.itmo.pirsbd.asashina.profiler;

import ru.spb.itmo.pirsbd.asashina.core.knn.TreeKnnV2;

import static ru.spb.itmo.pirsbd.asashina.benchmark.BenchmarkValues.DATA_FILEPATH;

public class BallTreeV2Profiler {

    public static void main(String[] args) {
        var knn = new TreeKnnV2(DATA_FILEPATH, 10_000, 5_000, 100);
        knn.getData();
    }

}
