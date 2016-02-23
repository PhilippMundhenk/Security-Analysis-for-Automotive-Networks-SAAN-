package sas.ModelChecker;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class ModelCheckerRaw {

	public Double checkModel(Double[][] Q, Double[] rho, Double time, Double accuracy) throws Exception {
		Double t = time;
		Boolean debugOutput = false;

		/* needs to be square matrix! */
		for (int i = 0; i < Q.length; i++) {
			if (Q.length != Q[i].length) {
				throw new Exception("Input matrix must be square matrix!");
			}
		}

		if (rho.length != Q.length) {
			throw new Exception("Size of rho must match size of input matrix!");
		}

		Double q = 0.0;
		for (int i = 0; i < Q.length; i++) {
			for (int j = 0; j < Q[i].length; j++) {
				if (Math.abs(Q[i][j]) > q) {
					q = Math.abs(Q[i][j]);
				}
			}
		}

		PoissonDistribution distr = new PoissonDistribution(q * t);

		Double[][] I = createIdentity(Q.length);
		Double[][] qOverQ = mscale((1 / q), Q);
		Double[][] p = madd(I, qOverQ);

		Double[][] output = qOverQ;
		if (debugOutput) {
			for (int j = 0; j < output.length; j++) {
				for (int j2 = 0; j2 < output.length; j2++) {
					System.out.print(output[j][j2] + "\t");
					if (j2 == output.length - 1) {
						System.out.println();
					}
				}
			}
		}

		long start = System.nanoTime();
		Double oldResult = Double.MAX_VALUE;
		Double[] result = vscale(((1 / q) * (1 - distr.cumulativeProbability(0))) * 1, rho);
		int i = 1;
//		System.out.println("accuracy: "+accuracy);
//		System.out.println("oldResult = " + oldResult);
//		System.out.println("intermediate result = " + result[0]);
//		System.out.println("diff: "+Math.abs(oldResult - result[0]));
		while((Math.abs(oldResult - result[0]) > accuracy) || (i < 5)) {
			oldResult = result[0];
			Double[] tmp = rho;
			for (int j = 0; j < i; j++) {
				tmp = multiply(p, tmp);
			}
			tmp = vscale(((1 / q) * (1 - distr.cumulativeProbability(i))), tmp);
			result = vadd(result, tmp);
//			System.out.println("i: "+i);
//			System.out.println("oldResult = " + oldResult);
//			System.out.println("intermediate result = " + result[0]);
//			System.out.println("diff: "+Math.abs(oldResult - result[0]));
			i++;
		}

		// This uses accuracy, but does not work reliably! As result changes are
		// oscillating, this can probably not be used
		// Double oldResult = Double.MAX_VALUE;
		// int i = 1;
		// while ((Math.abs(oldResult - result[0]) > accuracy) || i < 30) {
		// i++;
		// Double[] tmp = rho;
		// for (int j = 0; j < i; j++) {
		// tmp = multiply(p, tmp);
		// }
		// tmp = vscale(((1 / q) * (1 - distr.cumulativeProbability(i))), tmp);
		// result = vadd(result, tmp);
		// if (debugOutput) {
		// System.out.println("intermediate result = " + result[0]);
		// }
		// oldResult = result[0];
		// }

		long stop = System.nanoTime();

		if (debugOutput) {
			System.out.println("-----------------------------------");

			System.out.println("final:");
			for (int j = 0; j < result.length; j++) {
				System.out.println("s" + j + ": " + result[j]);
			}

			System.out.println("-----------------------------------");
			System.out.println("result: " + result[0]);
			System.out.println("-----------------------------------");

			System.out.println("time required: " + ((stop - start) / (double) 1000000000) + "s");
		}

		return result[0];

	}

	/* scalar times vector */
	public static Double[] vscale(Double x, Double[] vector) {
		Double[] result = new Double[vector.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = vector[i] * x;
		}
		return result;
	}

	/* scalar times matrix */
	public static Double[][] mscale(Double x, Double[][] matrix) {
		Double[][] result = new Double[matrix.length][matrix.length];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[i].length; j++) {
				result[i][j] = matrix[i][j] * x;
			}
		}
		return result;
	}

	/* vector plus vector */
	public static Double[] vadd(Double[] v1, Double[] v2) {
		if (v1.length != v2.length) {
			throw new RuntimeException("Illegal vector dimensions.");
		}
		Double[] result = new Double[v1.length];
		for (int i = 0; i < v2.length; i++) {
			result[i] = v1[i] + v2[i];
		}
		return result;
	}

	/* matrix plus matrix */
	public static Double[][] madd(Double[][] m1, Double[][] m2) {
		if (m1.length != m2.length) {
			throw new RuntimeException("Illegal matrix dimensions.");
		}
		Double[][] result = new Double[m1.length][m1.length];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[i].length; j++) {
				result[i][j] = m1[i][j] + m2[i][j];
			}
		}
		return result;
	}

	/* matrix times vector */
	public static Double[] multiply(Double[][] m, Double[] v) {
		int x = m.length;
		int y = m[0].length;
		if (v.length != y) {
			throw new RuntimeException("Illegal matrix dimensions.");
		}
		Double[] result = new Double[x];
		for (int i = 0; i < result.length; i++) {
			result[i] = 0.0;
		}
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				result[i] += (m[i][j] * v[j]);
			}
		}
		return result;
	}

	/* create identity matrix of size*size */
	public static Double[][] createIdentity(int size) {
		Double[][] result = new Double[size][size];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[i].length; j++) {
				if (i == j) {
					result[i][j] = 1.0;
				} else {
					result[i][j] = 0.0;
				}
			}
		}
		return result;
	}
}
