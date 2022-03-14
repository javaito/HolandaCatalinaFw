package org.hcjf.utils;

import com.google.gson.JsonElement;
import org.hcjf.bson.BsonDocument;
import org.hcjf.utils.bson.BsonParcelable;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MathsTest {

    @Test
    public void matrixAdd() {
        Matrix matrixA = new Matrix(3, 2, 2, -1, 3, 0, -5, 2);
        Matrix matrixB = new Matrix(3, 2, 1, 6, -1, -2, 0, -3);

        Matrix matrixC = Maths.matrixAdd(matrixA, matrixB);

        Matrix matrixTest = new Matrix(3, 2, 3, 5, 2, -2, -5, -1);
        Assert.assertEquals(matrixC, matrixTest);
    }

    @Test
    public void matrixSubtract() {
        Matrix matrixA = new Matrix(2, 2, 3, -1, -2, 2);
        Matrix matrixB = new Matrix(2, 2, 2, 0, 1, 4);

        Matrix matrixC = Maths.matrixSubtract(matrixA, matrixB);

        Matrix matrixTest = new Matrix(2, 2, 1, -1, -3, -2);
        Assert.assertEquals(matrixC, matrixTest);
    }

    @Test
    public void matrixMultiplyByScalar() {
        Matrix matrix = new Matrix(2, 3, 1, 0, -3, -2, 4, 1);
        Matrix resultMatrix = Maths.matrixMultiplyByScalar(matrix, 2);

        Matrix matrixTest = new Matrix(2, 3, 2, 0, -6, -4, 8, 2);
        Assert.assertEquals(resultMatrix, matrixTest);
    }

    @Test
    public void matrixMultiply() {
        Matrix matrixA = new Matrix(2, 3, 1, 0, -3, -2, 4, 1);
        Matrix matrixB = new Matrix(3, 4, 1, 0, 4, 1, -2, 3, -1, 5, 0, -1, 2, 1);

        Matrix matrixC = Maths.matrixMultiply(matrixA, matrixB);

        Matrix matrixTest = new Matrix(2, 4, 1, 3, -2, -2, -10, 11, -10, 19);
        Assert.assertEquals(matrixC, matrixTest);
    }

    @Test
    public void matrixInverseTest() {
        Matrix matrix = new Matrix(2, 2, 2, 3, 5, 8);
        Matrix inverse = Maths.matrixInverse(matrix);

        Matrix matrixTest = new Matrix(2, 2, 8, -3, -5, 2);
        Assert.assertEquals(inverse, matrixTest);
    }

    @Test
    public void matrixIdentity() {
        Matrix identity = Matrix.identity(3);

        Matrix matrixTest = new Matrix(3,3, 1, 0, 0, 0, 1, 0, 0, 0, 1);
        Assert.assertEquals(identity, matrixTest);

        Matrix inverse = Maths.matrixInverse(identity);
        Assert.assertEquals(identity, inverse);
    }

    @Test
    public void matrixSerialization() {
        Matrix matrix = new Matrix(3,3, 1, 0, 0, 0, 1, 0, 0, 0, 1);
        BsonDocument document = matrix.toBson();

        Matrix matrixTest = BsonParcelable.Builder.create(document);
        Assert.assertEquals(matrixTest, matrix);
    }

    @Test
    public void matrixToJson() {
        Map<String,Object> map = new HashMap<>();
        map.put("matrix", new Matrix(2, 4, 1, 3, -2, -2, -10, 11, -10, 19));

        System.out.println(Introspection.resolve(map, "matrix.data.0.1").toString());

        JsonElement element = JsonUtils.toJsonTree(map);
        System.out.println(element);
    }
}
