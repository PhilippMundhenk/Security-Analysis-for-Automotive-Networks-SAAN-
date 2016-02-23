package sas.ModelChecker;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class SteadyStateAnalyzer {

	private Boolean debugOutput = false;

	public Double analyze(Double[][] Q, Double[] rho) throws Exception {
		if (debugOutput) {
			System.out.println("Q:");
			for (int i = 0; i < Q.length; i++) {
				for (int j = 0; j < Q[i].length; j++) {
					System.out.print(Q[i][j] + ", ");
				}
				System.out.println();
			}
		}
		double[][] matrix = new double[Q.length + 1][Q.length];
		for (int i = 0; i < Q.length; i++) {
			for (int j = 0; j < Q[i].length; j++) {
				matrix[j][i] = Q[i][j].doubleValue();
			}
		}
		for (int i = 0; i < matrix[matrix.length - 1].length; i++) {
			matrix[matrix.length - 1][i] = 1.0;
		}

		if (debugOutput) {
			System.out.println();
			System.out.println("matrix:");
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					System.out.print(matrix[i][j] + ", ");
				}
				System.out.println();
			}
		}
		RealMatrix coeff = new Array2DRowRealMatrix(matrix);

		DecompositionSolver solver = new QRDecomposition(coeff).getSolver();

		double[] constants = new double[Q.length + 1];
		for (int i = 0; i < constants.length; i++) {
			constants[i] = 0;
		}
		constants[constants.length - 1] = 1;

		if (debugOutput) {
			System.out.println();
			System.out.println("constants:");
			for (int i = 0; i < constants.length; i++) {
				System.out.print(constants[i] + ", ");
			}
			System.out.println();
		}

		RealVector consts = new ArrayRealVector(constants, false);

		RealVector solution = solver.solve(consts);
		double[] R = solution.toArray();

		if (debugOutput) {
			System.out.println();
			System.out.println("Solution vector:");
			for (int i = 0; i < R.length; i++) {
				System.out.println(R[i] + ", ");
			}
		}

		Double sum = 0.0;
		for (int i = 0; i < R.length; i++) {
			if (rho[i] > 0) {
				sum += rho[i] * R[i];
			}
		}

		return sum;
	}
}
