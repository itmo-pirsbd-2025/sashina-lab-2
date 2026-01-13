package ru.spb.itmo.pirsbd.asashina.core.knn;

import ru.spb.itmo.pirsbd.asashina.core.BallTree;
import ru.spb.itmo.pirsbd.asashina.utils.KnnUtils.DistanceIndex;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class TreeKnn implements Knn {

    private static final String FILE_OUTPUT = "data/output.txt";

    private final BallTree tree;

    public TreeKnn(int[][] data, int leafSize, int neighboursAmount, boolean saveData) {
        this.tree = new BallTree(neighboursAmount, leafSize, data);
        if (saveData) {
            saveData();
        }
    }

    public TreeKnn(String fileName, int rowAmount, int leafSize, int neighboursAmount) {
        var data = readData(fileName, rowAmount);
        this.tree = new BallTree(neighboursAmount, leafSize, data);
        saveDataToTemp();
    }

    public int[][] getData() {
        return tree.getData();
    }

    @Override
    public int[][] getKNeighbours(int[] x) {
        tree.clearKNeighbors();
        tree.search(1, x);

        return tree.getKNeighbors()
                .stream()
                .sorted(Comparator.comparingDouble(DistanceIndex::distance))
                .map(di -> tree.getData()[di.index()])
                .toArray(int[][]::new);
    }

    private int[][] readData(String fileName, int rowAmount) {
        boolean isFirstLine = true;
        var size = 0;
        var data = new int[rowAmount][];
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
                BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] columns = line.split(",");
                if (columns.length <= 1) continue;

                int[] rowData = new int[columns.length - 1];
                for (int i = 1; i < columns.length; i++) {
                    rowData[i - 1] = Integer.parseInt(columns[i].trim());
                }
                data[size] = rowData;
                size++;
                if (size == rowAmount) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("IO Exception occurred", e);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid data format, only int data can be accepted", e);
        }
        return data;
    }

    private void saveData() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_OUTPUT))) {
            bw.write(tree.toString());
        } catch (IOException e) {
            throw new RuntimeException("IO Exception occurred", e);
        }
    }

    private void saveDataToTemp() {
        try {
            var tempDir = Files.createTempDirectory("knn_output_");
            var outputFile = tempDir.resolve("output.txt");
            try (BufferedWriter bw = Files.newBufferedWriter(outputFile)) {
                bw.write(tree.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("IO Exception occurred while saving data to temp directory", e);
        }
    }

}
