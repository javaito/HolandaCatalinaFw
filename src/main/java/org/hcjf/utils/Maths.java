package org.hcjf.utils;

import org.hcjf.errors.HCJFRuntimeException;

import java.util.ArrayList;
import java.util.List;

public class Maths {

    private static final double ERROR_THRESHOLD = 0.001;
    private static final int DEGREE = 3;

    /**
     * In statistics, linear regression is a linear approach for modelling the relationship between a scalar response
     * and one or more explanatory variables. The case of one explanatory variable is called simple linear regression;
     * for more than one, the process is called multiple linear regression.
     * @param xDataSet Set of independent values.
     * @param yDataSet Set of dependent values.
     * @param independentDataSet Set of independent values that we don't know the correspondent dependent value.
     * @return Set of deducted values, for each independent value.
     */
    public static List<Number> linearRegression(List<Number> xDataSet, List<Number> yDataSet, List<Number> independentDataSet) {
        Number xSum = 0.0;
        Number xSum2 = 0.0;
        Number ySum = 0.0;
        Number xySum = 0.0;

        for (int i = 0; i < xDataSet.size(); i++) {
            xSum = xSum2.doubleValue() + xDataSet.get(i).doubleValue();
            xSum2 = xSum2.doubleValue() + xDataSet.get(i).doubleValue() * xDataSet.get(i).doubleValue();
            ySum = ySum.doubleValue() + yDataSet.get(i).doubleValue();
            xySum = xySum.doubleValue() + xDataSet.get(i).doubleValue() * yDataSet.get(i).doubleValue();
        }

        double n = xDataSet.size();
        Number b = (n * xySum.doubleValue() - xSum.doubleValue() * ySum.doubleValue()) / (n * xSum2.doubleValue() - xSum.doubleValue() * xSum.doubleValue());
        Number a = (ySum.doubleValue() - b.doubleValue() * xSum.doubleValue()) / n;

        List<Number> result = new ArrayList<>();
        for(Number xd : independentDataSet) {
            result.add(a.doubleValue() + b.doubleValue() * xd.doubleValue());
        }

        return result;
    }

    /**
     * In statistics, polynomial regression is a form of regression analysis in which the relationship between the
     * independent variable x and the dependent variable y is modelled as an nth degree polynomial in x. Polynomial
     * regression fits a nonlinear relationship between the value of x and the corresponding conditional mean of y,
     * denoted E
     * @param xDataSet Set of independent values.
     * @param yDataSet Set of dependent values.
     * @param independentDataSet Set of independent values that we don't know the correspondent dependent value.
     * @return Set of deducted values, for each independent value.
     */
    public static List<Number> polynomialRegression(List<Number> xDataSet, List<Number> yDataSet, List<Number> independentDataSet) {
        return polynomialRegression(xDataSet, yDataSet, independentDataSet, DEGREE, ERROR_THRESHOLD);
    }

    /**
     * In statistics, polynomial regression is a form of regression analysis in which the relationship between the
     * independent variable x and the dependent variable y is modelled as an nth degree polynomial in x. Polynomial
     * regression fits a nonlinear relationship between the value of x and the corresponding conditional mean of y,
     * denoted E
     * @param xDataSet Set of independent values.
     * @param yDataSet Set of dependent values.
     * @param independentDataSet Set of independent values that we don't know the correspondent dependent value.
     * @param degree
     * @param errorThreshold
     * @return Set of deducted values, for each independent value.
     */
    public static List<Number> polynomialRegression(List<Number> xDataSet, List<Number> yDataSet, List<Number> independentDataSet, Number degree, Number errorThreshold) {
        double[] x = new double[xDataSet.size()], y = new double[xDataSet.size()];
        int n = xDataSet.size();
        int intDegree = degree.intValue();
        double[][] m = new double[intDegree+1][intDegree+1];
        double[] t = new double[intDegree+1];
        double[] a = new double[intDegree+1];

        for (int i = 0; i < xDataSet.size(); i++) {
            x[i] = xDataSet.get(i).doubleValue();
            y[i] = yDataSet.get(i).doubleValue();
        }

        double[] s=new double[2*intDegree+1];
        double sum;
        for(int k=0; k<=2*intDegree; k++){
            sum=0.0;
            for(int i=0; i<n; i++){
                sum+= Math.pow(x[i], k);
            }
            s[k]=sum;
        }
        for(int k=0; k<=intDegree; k++){
            sum=0.0;
            for(int i=0; i<n; i++){
                sum+= Math.pow(x[i], k)*y[i];
            }
            t[k]=sum;
        }
        for(int i=0; i<=intDegree; i++){
            for(int j=0; j<=intDegree; j++){
                m[i][j]=s[i+j];
            }
        }

        double aux;
        for(int i=0; i<=intDegree; i++){
            aux=m[i][i];
            for(int j=0; j<=intDegree; j++){
                m[i][j]=-m[i][j]/aux;
            }
            t[i]=t[i]/aux;
            m[i][i]=0.0;
        }

        double[] p=new double[intDegree+1];
        p[0]=t[0];
        for(int i=1; i<=intDegree; i++){
            p[i]=0.0;
        }

        double error;
        double max;
        do{
            max=0.0;
            for(int i=0; i<=intDegree; i++){
                a[i]=t[i];
                for(int j=0; j<i; j++){
                    a[i]+=m[i][j]*a[j];
                }
                for(int j=i+1; j<=intDegree; j++){
                    a[i]+=m[i][j]*p[j];
                }
                error=Math.abs((a[i]-p[i])/a[i]);
                if (error>max)  {
                    max=error;
                }
            }
            for(int i=0; i<=intDegree; i++){
                p[i]=a[i];
            }
        }while(max>errorThreshold.doubleValue());

        List<Number> resultList = new ArrayList<>();
        for(Number xp : independentDataSet) {
            double result = 0.0;
            for (int i = 0; i < a.length; i++) {
                result += a[i] * Math.pow(xp.doubleValue(), i);
            }
            resultList.add(result);
        }

        return resultList;
    }

    /**
     * This method add two matrix and returns the result matrix instance.
     * @param matrixA Matrix A
     * @param matrixB Matrix B
     * @return Result matrix.
     */
    public static Matrix matrixAdd(Matrix matrixA, Matrix matrixB) {
        if(matrixA.getRows() != matrixB.getRows() || matrixA.getCols() != matrixB.getCols()) {
            throw new HCJFRuntimeException("To add two matrix, these must have the same size");
        }

        Matrix result = new Matrix(matrixA.getRows(), matrixA.getCols());
        for (int row = 0 ; row < matrixA.getRows() ; row++ ) {
            for (int col = 0; col < matrixA.getCols(); col++) {
                result.set(row, col, matrixA.get(row, col) + matrixB.get(row, col));
            }
        }
        return result;
    }

    /**
     * This method subtract two matrix and returns the result matrix instance.
     * @param matrixA Matrix A
     * @param matrixB Matrix B
     * @return Result matrix.
     */
    public static Matrix matrixSubtract(Matrix matrixA, Matrix matrixB) {
        if(matrixA.getRows() != matrixB.getRows() || matrixA.getCols() != matrixB.getCols()) {
            throw new HCJFRuntimeException("To subtract two matrix, these must have the same size");
        }

        Matrix result = new Matrix(matrixA.getRows(), matrixA.getCols());
        for (int row = 0 ; row < matrixA.getRows() ; row++ ) {
            for (int col = 0; col < matrixA.getCols(); col++) {
                result.set(row, col, matrixA.get(row, col) - matrixB.get(row, col));
            }
        }
        return result;
    }

    /**
     * This method multiply all the values of the matrix instance by scalar.
     * @param matrix Matrix instance.
     * @param scalar Scalar value.
     * @return Result matrix.
     */
    public static Matrix matrixMultiplyByScalar(Matrix matrix, Number scalar) {
        Matrix result = new Matrix(matrix.getRows(), matrix.getCols());
        for (int row = 0 ; row < matrix.getRows() ; row++ ) {
            for (int col = 0; col < matrix.getCols(); col++) {
                result.set(row, col, matrix.get(row, col) * scalar.doubleValue());
            }
        }
        return result;
    }

    /**
     * This method multiply two matrix and returns a result matrix.
     * @param matrixA Matrix A.
     * @param matrixB Matrix B.
     * @return Result matrix.
     */
    public static Matrix matrixMultiply(Matrix matrixA, Matrix matrixB) {
        if(matrixA.getCols() != matrixB.getRows()) {
            throw new HCJFRuntimeException("To multiply two matrix the first matrix columns size must be equals than the second matrix row size");
        }

        Matrix result = new Matrix(matrixA.getRows(), matrixB.getCols());
        for (int rowA = 0 ; rowA < matrixA.getRows() ; rowA++ ) {
            for (int colB = 0 ; colB < matrixB.getCols() ; colB++ ) {
                double sum = 0.0;
                for (int i = 0 ; i < matrixA.getCols() ; i++ ) {
                    sum += matrixA.get(rowA, i) * matrixB.get(i, colB);
                }
                result.set(rowA, colB, sum);
            }
        }

        return result;
    }

    /**
     * Calcualtes the transposed matrix.
     * @param matrix Matrix instance.
     * @return Transposed matrix instance.
     */
    public static Matrix matrixTranspose(Matrix matrix) {
        Matrix transposedMatrix = new Matrix(matrix.getCols(), matrix.getRows());
        for (int row = 0; row < matrix.getRows(); row++) {
            for (int col = 0; col < matrix.getCols(); col++) {
                transposedMatrix.set(col, row, matrix.get(row, col));
            }
        }
        return transposedMatrix;
    }

    /**
     * Calculates de determinant value of the matrix.
     * @param matrix Matrix instance.
     * @return Determinant value.
     */
    public static double matrixDeterminant(Matrix matrix) {
        double result;
        if (!matrix.isSquare()) {
            throw new HCJFRuntimeException("Matrix neet to be square");
        }
        if (matrix.getRows() == 1) {
            result = matrix.get(0, 0);
        } else if (matrix.getRows() == 2) {
            result = (matrix.get(0, 0) * matrix.get(1, 1)) -
                    ( matrix.get(0, 1) * matrix.get(1, 0));
        } else {
            result = 0.0;
            for (int i = 0; i < matrix.getCols(); i++) {
                result += (i % 2 == 0 ? 1 : -1) * matrix.get(0, i) * matrixDeterminant(createSubMatrix(matrix, 0, i));
            }
        }
        return result;
    }

    /**
     * Calculates sub-matrix excluding one row and one column.
     * @param matrix Matrix instance.
     * @param excludingRow Row to exclude.
     * @param excludingCol Column to exclude.
     * @return Sub-matrix instance
     */
    public static Matrix createSubMatrix(Matrix matrix, int excludingRow, int excludingCol) {
        Matrix mat = new Matrix(matrix.getRows() - 1, matrix.getCols() - 1);
        int r = -1;
        for (int i=0;i<matrix.getRows();i++) {
            if (i==excludingRow)
                continue;
            r++;
            int c = -1;
            for (int j=0;j<matrix.getCols();j++) {
                if (j==excludingCol)
                    continue;
                mat.set(r, ++c, matrix.get(i, j));
            }
        }
        return mat;
    }

    /**
     * Calculates the matrix of cofactor.
     * @param matrix Matrix instance.
     * @return Matrix of cofactor.
     */
    public static Matrix matrixCofactor(Matrix matrix) {
        Matrix mat = new Matrix(matrix.getRows(), matrix.getCols());
        for (int i=0;i<matrix.getRows();i++) {
            for (int j=0; j<matrix.getCols();j++) {
                mat.set(i, j, (i % 2 == 0 ? 1 : -1) * (j % 2 == 0 ? 1 : -1) *
                        matrixDeterminant(createSubMatrix(matrix, i, j)));
            }
        }
        return mat;
    }

    /**
     * Calcualtes the inverse of the matrix.
     * @param matrix Matrix instance.
     * @return Inverse of the matrix.
     */
    public static Matrix matrixInverse(Matrix matrix) {
        return matrixMultiplyByScalar(matrixTranspose(matrixCofactor(matrix)), 1.0/matrixDeterminant(matrix));
    }

}
