package org.hcjf.utils;

import org.hcjf.bson.BsonDocument;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.Layers;
import org.hcjf.utils.bson.BsonParcelable;

import java.util.ArrayList;
import java.util.List;

public final class Matrix implements BsonParcelable {

    public static final class Fields {
        public static final String ROWS = "rows";
        public static final String COLS = "cols";
        public static final String DATA = "data";
    }

    static {
        Layers.publishLayer(MatrixBsonBuilderLayer.class);
    }

    private int rows;
    private int cols;
    private double[][] data;

    public Matrix(double[][] dat) {
        this.data = dat;
        this.rows = dat.length;
        this.cols = dat[0].length;
    }

    public Matrix(int rows, int cols, Number... values) {
        this.rows = rows;
        this.cols = cols;
        data = new double[rows][cols];

        int z = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if(z < values.length) {
                    data[i][j] = values[z++].doubleValue();
                } else {
                    break;
                }
            }
            if(z >= values.length) {
                break;
            }
        }
    }

    /**
     * Returns number of rows of the matrix.
     * @return Number of rows.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns number of columns of the matrix.
     * @return Number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Returns a cloned array with matrix information.
     * @return Cloned array
     */
    public double[][] getData() {
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = data[i][j];
            }
        }
        return result;
    }

    /**
     * Verify if the matrix is square or not.
     * @return Returns true if the matrix row size is equals than matrix column size and false in the otherwise.
     */
    public boolean isSquare() {
        return rows == cols;
    }

    /**
     * Check if the range of the row and the column specified are correct with the size of the matrix.
     * @param row Row specified to access data
     * @param col Column specified to access data
     */
    private void checkRange(int row, int col) {
        if(row < 0 || row >= rows) {
            throw new HCJFRuntimeException("Matrix [%s, %s] row out of range: %s", rows, cols, row);
        }

        if(col < 0 || col >= cols) {
            throw new HCJFRuntimeException("Matrix [%s, %s] col out of range: %s", rows, cols, col);
        }
    }

    /**
     * Set a new value into de data
     * @param row Row specified to access data
     * @param col Column specified to access data.
     * @param value Value to store into data.
     */
    public void set(int row, int col, Number value) {
        checkRange(row, col);
        data[row][col] = value.doubleValue();
        if(Double.compare(data[row][col], -0.0) == 0) {
            data[row][col] = 0.0;
        }
    }

    /**
     * Get a value from the data stored into the matrix.
     * @param row Row specified to access data.
     * @param col Column specified to access data.
     * @return Value stored into data.
     */
    public double get(int row, int col) {
        checkRange(row, col);
        return data[row][col];
    }

    /**
     * Compare numerically each value of the matrix, if all the values are equals between two matrix then are equals.
     * @param o Other object to verify if equals.
     * @return Returns true if the matrix are equals and false in the otherwise.
     */
    @Override
    public boolean equals(Object o) {
        boolean result = false;

        if(o instanceof Matrix) {
            Matrix matrix = (Matrix) o;
            result = true;
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    if (Double.compare(get(row, col), matrix.get(row, col)) != 0) {
                        result = false;
                        break;
                    }
                }
                if(!result) {
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Creates a string representation fo the matrix instance.
     * @return String representation fo the matrix instance.
     */
    @Override
    public String toString() {
        Strings.Builder builder = new Strings.Builder();
        int length = 0;
        List<String> values = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                String value = Double.toString(get(row,col));
                if(length < value.length()) {
                    length = value.length();
                }
                values.add(value);
            }
        }

        int z = 0;
        for (int row = 0; row < rows; row++) {
            builder.append("|");
            for (int col = 0; col < cols; col++) {
                builder.append(Strings.leftPad(values.get(z++), length), Strings.ARGUMENT_SEPARATOR);
            }
            builder.cleanBuffer();
            builder.append("|");
            builder.append(Strings.CARRIAGE_RETURN_AND_LINE_SEPARATOR);
        }
        builder.cleanBuffer();
        return builder.toString();
    }

    /**
     * This method creates a new identity matrix instance with a specific size.
     * @param size Size of the matrix.
     * @return Identity matrix instance.
     */
    public static Matrix identity(int size) {
        Matrix identity = new Matrix(size, size);
        for (int i = 0; i < size; i++) {
            identity.set(i, i, 1);
        }
        return identity;
    }

    @Override
    public BsonDocument toBson() {
        BsonDocument document = new BsonDocument();
        document.put(PARCELABLE_CLASS_NAME, getClass().getName());
        document.put(Fields.ROWS, rows);
        document.put(Fields.COLS, cols);
        List<Number> dataList = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                dataList.add(data[i][j]);
            }
        }
        document.put(Fields.DATA, dataList);
        return document;
    }
}
