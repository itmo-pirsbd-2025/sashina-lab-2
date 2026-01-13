package ru.spb.itmo.pirsbd.asashina.core.knn;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

class TreeKnnTest {

    @Test
    void getKNeighborsWhenLeafSizeLessThanPointsTest() {
        var x1 = new int[]{2, 2};
        var x2 = new int[]{9, 3};
        var searchedX = new int[]{5, 5};
        var x3 = new int[]{6, 6};
        var x4 = new int[]{4, 7};
        var x = new int[][]{x1, x2, searchedX, x3, x4};

        var classifier = new TreeKnn(x, 1, 2, false);

        var result = classifier.getKNeighbours(searchedX);
        assertThat(result).hasNumberOfRows(2);
        var resultSet = new HashSet<>(Arrays.asList(x));
        assertThat(resultSet).contains(x3, x4);
    }

    @Test
    void getKNeighborsWhenLeafSizeMoreThanPointsTest() {
        var x1 = new int[]{2, 2};
        var x2 = new int[]{9, 3};
        var searchedX = new int[]{5, 5};
        var x3 = new int[]{6, 6};
        var x4 = new int[]{4, 7};
        var x = new int[][]{x1, x2, searchedX, x3, x4};

        var classifier = new TreeKnn(x, 6, 2, false);

        var result = classifier.getKNeighbours(searchedX);
        assertThat(result).hasNumberOfRows(2);
        var resultSet = new HashSet<>(Arrays.asList(x));
        assertThat(resultSet).contains(x3, x4);
    }

}