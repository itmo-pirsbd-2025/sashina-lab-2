package ru.spb.itmo.pirsbd.asashina.core.knn;

import ru.spb.itmo.pirsbd.asashina.core.BallTreeV2;
import ru.spb.itmo.pirsbd.asashina.utils.KnnUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TreeKnnV2 implements Knn {

    private static final String FILE_OUTPUT = "data/output_v2.txt";
    private static final int READ_CAPACITY = 256 * 1024;
    private static final int WRITE_CAPACITY = 256 * 1024;

    private final BallTreeV2 tree;

    public TreeKnnV2(int[][] data, int leafSize, int neighboursAmount, boolean saveData) {
        this.tree = new BallTreeV2(neighboursAmount, leafSize, data);
        if (saveData) {
            saveData();
        }
    }

    public TreeKnnV2(String fileName, int rowAmount, int leafSize, int neighboursAmount) {
        var data = readData(fileName, rowAmount);
        this.tree = new BallTreeV2(neighboursAmount, leafSize, data);
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
                .sorted(Comparator.comparingDouble(KnnUtils.DistanceIndex::distance))
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

    private int[][] readDataV2(String fileName, int rowAmount) {
        var result = new ArrayList<int[]>(rowAmount);
        try (var channel = FileChannel.open(Paths.get(fileName), StandardOpenOption.READ)) {
            var buffer = ByteBuffer.allocateDirect(READ_CAPACITY);
            var lineBuilder = new StringBuilder(256);
            var isFirstLine = true;
            byte[] leftoverBytes = null;
            int bytesRead;
            while ((bytesRead = channel.read(buffer)) != -1 && result.size() < rowAmount) {
                buffer.flip();
                leftoverBytes = processBuffer(buffer, lineBuilder, result, leftoverBytes, isFirstLine, rowAmount);
                isFirstLine = false;
                buffer.clear();
            }

            if (leftoverBytes != null && result.size() < rowAmount) {
                processLeftoverBytes(leftoverBytes, lineBuilder, result, isFirstLine);
            }

        } catch (IOException e) {
            throw new RuntimeException("IO Exception occurred", e);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid data format, only int data can be accepted", e);
        }
        return result.toArray(int[][]::new);
    }

    private byte[] processBuffer(
        ByteBuffer buffer,
        StringBuilder lineBuilder,
        List<int[]> result,
        byte[] previousLeftover,
        boolean skipFirstLine,
        int maxRows
    ) {
        byte[] data;
        if (previousLeftover != null) {
            data = new byte[previousLeftover.length + buffer.remaining()];
            System.arraycopy(previousLeftover, 0, data, 0, previousLeftover.length);
            buffer.get(data, previousLeftover.length, buffer.remaining());
        } else {
            data = new byte[buffer.remaining()];
            buffer.get(data);
        }

        var lineStart = 0;
        var isFirstLineInBuffer = skipFirstLine;

        for (var i = 0; i < data.length && result.size() < maxRows; i++) {
            if (data[i] == '\n' || data[i] == '\r') {
                if (i > lineStart) {
                    var lineBytes = Arrays.copyOfRange(data, lineStart, i);
                    processLineBytes(lineBytes, lineBuilder, result, isFirstLineInBuffer);
                    isFirstLineInBuffer = false;
                }
                lineStart = i + 1;
            }
        }
        if (lineStart < data.length) {
            return Arrays.copyOfRange(data, lineStart, data.length);
        }

        return null;
    }

    private void processLineBytes(
        byte[] lineBytes,
        StringBuilder lineBuilder,
        List<int[]> result,
        boolean skipLine
    ) {
        if (skipLine) {
            return;
        }

        lineBuilder.setLength(0);
        for (var b : lineBytes) {
            lineBuilder.append((char) b);
        }
        var line = lineBuilder.toString();
        var columns = line.split(",");

        if (columns.length <= 1) {
            return;
        }

        var rowData = new int[columns.length - 1];
        for (var i = 1; i < columns.length; i++) {
            rowData[i - 1] = parseIntFast(columns[i]);
        }
        result.add(rowData);
    }

    private void processLeftoverBytes(
        byte[] leftoverBytes,
        StringBuilder lineBuilder,
        List<int[]> result,
        boolean skipLine
    ) {
        if (skipLine || leftoverBytes.length == 0) {
            return;
        }

        lineBuilder.setLength(0);
        for (byte b : leftoverBytes) {
            lineBuilder.append((char) b);
        }

        var line = lineBuilder.toString().trim();
        if (!line.isEmpty()) {
            var columns = line.split(",");
            if (columns.length > 1) {
                var rowData = new int[columns.length - 1];
                for (var i = 1; i < columns.length; i++) {
                    rowData[i - 1] = parseIntFast(columns[i]);
                }
                result.add(rowData);
            }
        }
    }

    private int parseIntFast(String s) {
        var result = 0;
        var negative = false;
        var i = 0;
        while (i < s.length() && s.charAt(i) == ' ') {
            i++;
        }
        if (i < s.length() && s.charAt(i) == '-') {
            negative = true;
            i++;
        }
        while (i < s.length()) {
            var c = s.charAt(i++);
            if (c >= '0' && c <= '9') {
                result = result * 10 + (c - '0');
            }
        }
        return negative
                ? -result
                : result;
    }

    private void saveDataV2() {
        var treeString = tree.toString();
        var data = treeString.getBytes(StandardCharsets.UTF_8);

        try (FileChannel channel = FileChannel.open(
                Paths.get(FILE_OUTPUT),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            var buffer = ByteBuffer.allocateDirect(Math.min(data.length, WRITE_CAPACITY));
            var offset = 0;
            while (offset < data.length) {
                var chunkSize = Math.min(buffer.capacity(), data.length - offset);
                buffer.clear();
                buffer.put(data, offset, chunkSize);
                buffer.flip();
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                offset += chunkSize;
            }
            channel.force(true);
        } catch (IOException e) {
            throw new RuntimeException("IO Exception occurred", e);
        }
    }

}
