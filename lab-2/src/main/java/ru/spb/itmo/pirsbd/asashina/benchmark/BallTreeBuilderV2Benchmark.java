package ru.spb.itmo.pirsbd.asashina.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import ru.spb.itmo.pirsbd.asashina.core.knn.TreeKnn;
import ru.spb.itmo.pirsbd.asashina.core.knn.TreeKnnV2;

import java.util.concurrent.TimeUnit;

import static ru.spb.itmo.pirsbd.asashina.benchmark.BenchmarkValues.DATA_FILEPATH;

@State(Scope.Thread)
public class BallTreeBuilderV2Benchmark {

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 3, time = 1)
    @Measurement(iterations = 10, time = 1)
    @Fork(value = 2, warmups = 1)
    public void createTree(Blackhole blackhole, BenchmarkValues benchmarkValues) {
        var treeKnn = new TreeKnnV2(DATA_FILEPATH, benchmarkValues.getDataAmount(), benchmarkValues.getLeafSize(), 100);
        blackhole.consume(treeKnn);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BallTreeBuilderV2Benchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

}
