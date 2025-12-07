package ru.spb.itmo.pirsbd.asashina.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import ru.spb.itmo.pirsbd.asashina.core.knn.TreeKnn;

@State(Scope.Benchmark)
public class BenchmarkValues {

    public static final String DATA_FILEPATH = "data/fashion-mnist_train.csv";

    @Param({"50", "200", "700", "1000", "2500"})
    private int dataAmount;

    @Param({"100", "500", "1000"})
    private int leafSize;

    private TreeKnn treeKnn;

    public TreeKnn getKnn() {
        return treeKnn;
    }

    public int getLeafSize() {
        return leafSize;
    }

    public int getDataAmount() {
        return dataAmount;
    }

    @Setup(Level.Iteration)
    public void setUp(BenchmarkParams params) {
        if (params.getBenchmark().contains("knn")) {
            treeKnn = new TreeKnn(DATA_FILEPATH, dataAmount, leafSize, 100);
        }
    }

}
