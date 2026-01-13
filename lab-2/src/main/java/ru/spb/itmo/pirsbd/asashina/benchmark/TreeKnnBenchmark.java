package ru.spb.itmo.pirsbd.asashina.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(value = 2, warmups = 1)
public class TreeKnnBenchmark {

    @Benchmark
    public void knnGetOneNeighbour(Blackhole blackhole, BenchmarkValues benchmarkValues) {
        var treeKnn = benchmarkValues.getKnn();
        var data = treeKnn.getData();
        var result = treeKnn.getKNeighbours(data[0]);
        blackhole.consume(result);
    }

    @Benchmark
    public void knnGetAllNeighbours(Blackhole blackhole, BenchmarkValues benchmarkValues) {
        var treeKnn = benchmarkValues.getKnn();
        var data = treeKnn.getData();
        for (var x : data) {
            var result = treeKnn.getKNeighbours(x);
            blackhole.consume(result);
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TreeKnnBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

}
