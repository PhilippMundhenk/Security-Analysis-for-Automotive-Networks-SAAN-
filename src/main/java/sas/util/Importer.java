package sas.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Importer {
	private static Boolean debugOutput = false;

	public static Double[][] importTransitionMatrix(File file) throws Exception {
		if (file == null) {
			throw new Exception("Please enter a proper file");
		}

		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);

		String line;
		Boolean firstLine = true;
		Double[][] matrix = null;
		while ((line = br.readLine()) != null) {
			if (firstLine) {
				String[] input = line.split(" ");
				if (debugOutput) {
					System.out.println("matrix size = " + Integer.parseInt(input[0]));
				}
				matrix = new Double[Integer.parseInt(input[0])][Integer.parseInt(input[0])];

				for (int i = 0; i < matrix.length; i++) {
					for (int j = 0; j < matrix[i].length; j++) {
						matrix[i][j] = new Double(0.0);
					}
				}

				firstLine = false;
			} else {
				String[] input = line.split(" ");
				matrix[Integer.parseInt(input[0])][Integer.parseInt(input[1])] = Double.parseDouble(input[2]);
			}
		}

		if (debugOutput) {
			System.out.println();
			System.out.print("matrix:\t");
			for (int i = 0; i < matrix.length; i++) {
				System.out.print(i + "\t");
			}
			System.out.println();
			System.out.println("-----------------------------------------------------------------------------------");
			for (int i = 0; i < matrix.length; i++) {
				System.out.print(i + " |\t");
				for (int j = 0; j < matrix[i].length; j++) {
					System.out.print(matrix[i][j] + "\t");
				}
				System.out.println();
			}
		}

		br.close();
		fr.close();

		return matrix;
	}

	public static Double[][] generateNegativeSums(Double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			Double sum = 0.0;
			for (int j = 0; j < matrix[i].length; j++) {

				sum += matrix[i][j];
			}
			matrix[i][i] = (-1) * sum;
		}

		return matrix;
	}

	public static Double[] importRewardsMatrix(File file) throws Exception {
		if (file == null) {
			throw new Exception("Please enter a proper file");
		}

		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);

		String line;
		Boolean firstLine = true;
		Double[] rho = null;
		while ((line = br.readLine()) != null) {
			if (firstLine) {
				String[] input = line.split(" ");
				if (debugOutput) {
					System.out.println("matrix size = " + Integer.parseInt(input[0]));
				}
				rho = new Double[Integer.parseInt(input[0])];

				for (int i = 0; i < rho.length; i++) {
					rho[i] = new Double(0.0);
				}

				firstLine = false;
			} else {
				String[] input = line.split(" ");
				rho[Integer.parseInt(input[0])] = Double.parseDouble(input[1]);
			}
		}

		br.close();
		fr.close();

		return rho;
	}
}
