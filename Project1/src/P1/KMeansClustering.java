package P1;

import java.io.File;
import java.io.IOException;
import java.util.*;

import jxl.*;
import jxl.read.biff.BiffException;
//download jxl from "https://jar-download.com/?search_box=jxl" and add to class path

public class KMeansClustering {
	private String inputFile;
	private double[][] data;
	private double[] normal;
	private int x;
	private double[][] manhattan;
	private double[] centroids;
	private String[][] clusters;
	private String[] dna;

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public double[][] read() throws IOException {
		File inputWorkbook = new File(inputFile);
		Workbook w;

		try {
			w = Workbook.getWorkbook(inputWorkbook);

			// Get the sheet
			Sheet sheet = w.getSheet(1);
			// initialize the array to hold the data
			data = new double[sheet.getColumns()][sheet.getRows()];
			dna = new String[sheet.getRows()];

			// don't need columns 0-2 or 6-9, so start on column 3 and end on column 5
			for (int j = 3; j < 6; j++) {
				// start on 1 to leave out headers
				for (int i = 1; i < sheet.getRows(); i++) {
					Cell cell = sheet.getCell(j, i);
					String x = cell.getContents();
					data[j][i] = Double.parseDouble(x);
				}
			}
		} catch (BiffException e) {
			e.printStackTrace();
		}

		return data;

	}

	public double[] normalize(double[][] d, int x) throws IOException {

		normal = new double[d[x].length];

		// data[x][i] -> x is the column number
		// data[3][i] = sch9/wt
		// data[4][i] = ras2/wt
		// data[5][i] = tor1/wt

		// normalize to [0.0, 1.0]

		// set target min/max (incase we need to change)
		double tmin = 0.0;
		double tmax = 1.0;

		// find the min/max values
		double min = 1.0;
		double max = -10.0;

		for (int j = 0; j < d[x].length; j++) {
			if (d[x][j] < min) {
				min = d[x][j];
			} else if (d[x][j] > max) {
				max = d[x][j];
			}
		}

		System.out.println("Column: " + x);
		System.out.println("min: " + min);
		System.out.println("max: " + max + "\n");

		// Formula
		// NEW VALUE = ((CURRENT VALUE - MINIMUM) / (MAXIMUM - MINIMUM)) * (TARGET
		// MAXIMUM - TARGET MINIMUM) + TARGET MINIMUM

		for (int i = 0; i < d[x].length; i++) {
			normal[i] = (((d[x][i] - min) / (max - min)) * (tmax - tmin)) + tmin;
		}
		return normal;
	}

	public double[][] manhattanDistance(double normal[], double normal1[], double normal2[], double[][] centroid,
			int k) {
		// formula for Manhattan distance
		manhattan = new double[data[x].length][k];

		// formula
		// manhattan = abs(xi - xj) + abs(x2 - x2)

		// centroid[0] = the centroid's SCH value
		// centroid[1] = the centroid's RAS value
		// centroid[2] = the centroid's TOR value

		for (int i = 0; i < data[x].length; i++) {
			for (int j = 0; j < k; j++) {
				manhattan[i][j] = Math.abs(centroid[j][0] - normal[i]) + Math.abs(centroid[j][1] - normal1[i])
						+ Math.abs(centroid[j][2] - normal2[i]);

			}
		}

		return manhattan;
	}

	public String[] readDNA() throws IOException {
		File inputWorkbook = new File(inputFile);
		Workbook w;

		try {
			w = Workbook.getWorkbook(inputWorkbook);

			// Get the sheet
			Sheet sheet = w.getSheet(1);
			// initialize the array to hold the data
			// data = new double[sheet.getColumns()][sheet.getRows()];
			dna = new String[sheet.getRows()];

			// don't need columns 0-2 or 6-9, so start on column 3 and end on column 5

			// start on 1 to leave out headers
			for (int i = 0; i < sheet.getRows(); i++) {
				Cell cell1 = sheet.getCell(0, i);
				dna[i] = cell1.getContents();
			}

		} catch (BiffException e) {
			e.printStackTrace();
		}
		String[] dna1 = new String[dna.length - 1];

		for (int i = 0; i < dna.length - 1; i++) {
			dna1[i] = dna[i + 1];
		}

		return dna1;

	}

	public ArrayList<String>[] cluster(double[][] manhattan, double[][] centroids, int k, String[] dna, double[] norm,
			double[] norm1, double[] norm2, int iterations) {

		// store dna names in an arraylist
		// there will be 'k' arraylist stored in an array
		ArrayList<String> clusterData[] = new ArrayList[k];

		double minDistance = 1.0;
		manhattan = manhattanDistance(norm, norm1, norm2, centroids, k);

		// initialize 'k' arraylist
		for (int j = 0; j < k; j++) {
			clusterData[j] = new ArrayList<>();
		}

		int index = 0;
		int d = dna.length;
		for (int i = 0; i < dna.length; i++) {
			// reset minDistance and index
			minDistance = 1.0;
			index = 0;
			// comparing all distances using for loop
			for (int j = 0; j < k; j++) {
				// if current distance < previous distance, then set minDistance to current
				// distance
				if (manhattan[i][j] <= minDistance) {
					minDistance = manhattan[i][j];
					// get the index of j. this determines which arraylist to add to
					index = j;
				}
			}
			clusterData[index].add(dna[i]);
		}
		return clusterData;
	}

	public double[][] newCentroids(double[][] manhattan, double[][] centroids, int k, String[] dna, double[] norm,
			double[] norm1, double[] norm2, int iterations) {
		// store dna names in an arraylist
		// there will be 'k' arraylist stored in an array

		// another arraylist array to store distances
		ArrayList<Integer> clusterDistances[] = new ArrayList[k];

		double minDistance = 1.0;

		manhattan = manhattanDistance(norm, norm1, norm2, centroids, k);

		// initialize 'k' arraylist
		for (int j = 0; j < k; j++) {
			clusterDistances[j] = new ArrayList<>();
		}

		int index = 0;

		for (int i = 0; i <= dna.length - 1; i++) {
			// reset minDistance and index
			minDistance = 1.0;
			index = 0;

			// comparing all distances using for loop
			for (int j = 0; j < k; j++) {
				// if current distance < previous distance, then set minDistance to current
				// distance
				if (manhattan[i][j] <= minDistance) {
					minDistance = manhattan[i][j];
					// get the index of j. this determines which arraylist to add to
					index = j;

				}

			}
			// clusterDistances[index].add(minDistance);
			clusterDistances[index].add(i);
		}

		// double[][] newCentroids = new double[k][3];
		// double[][] sum = new double[k][3];
		double s1 = 0;
		double s2 = 0;
		double s3 = 0;

		for (int j = 0; j < k; j++) {
			double l = clusterDistances[j].size();
			for (int i = 0; i < clusterDistances[j].size(); i++) {
				s1 = s1 + norm[clusterDistances[j].get(i)];
				s2 = s2 + norm1[clusterDistances[j].get(i)];
				s3 = s3 + norm2[clusterDistances[j].get(i)];

			}
			// new centroids
			centroids[j][0] = s1 / clusterDistances[j].size();
			centroids[j][1] = s2 / clusterDistances[j].size();
			centroids[j][2] = s3 / clusterDistances[j].size();
			s1 = 0;
			s2 = 0;
			s3 = 0;

		}
		return centroids;
	}

	public static void main(String[] args) throws IOException {
		Scanner scan = new Scanner(System.in);

		KMeansClustering test = new KMeansClustering();
		test.setInputFile("Longotor1delta.xls");

		// read data
		// puts data into double array
		double[][] d = test.read();
		String[] dna = test.readDNA();

		// MIN-MAX Normalization on each column
		// normalize accepts the data and the column number
		double[] normSCH = test.normalize(d, 3);
		double[] normRAS = test.normalize(d, 4);
		double[] normTOR = test.normalize(d, 5);

		int n = normSCH.length;

		// find the centroids (first ones can be random?) *use the first row of data for
		// first centroid*

		// get k
		System.out.println("Enter the number of clusters (k): ");
		int k = scan.nextInt();
		System.out.println("Enter the number of max iterations (100, 200, 300)");
		int iterations = scan.nextInt();

		// initialize first centroids
		// puts centroid into double array containing all three data points
		double[][] centroids = new double[k][3];

		// picks initial centroids from the top of normalized data set
		for (int i = 0; i < k; i++) {

			centroids[i][0] = normSCH[i];
			centroids[i][1] = normRAS[i];
			centroids[i][2] = normTOR[i];

			// centroid[i][0] = SCH value
			// centroid[i][1] = RAS valuez
			// centroid[i][2] = TOR value

		}

		double[][] manhattan;
		ArrayList<String>[] cluster;
		// double[][] newCentroids;

		// initial run
		manhattan = test.manhattanDistance(normSCH, normRAS, normTOR, centroids, k);
		cluster = test.cluster(manhattan, centroids, k, dna, normSCH, normRAS, normTOR, iterations);

		int c = 1;
		do {
			// manhattan = test.manhattanDistance(normSCH, normRAS, normTOR, centroids, k);
			centroids = test.newCentroids(manhattan, centroids, k, dna, normSCH, normRAS, normTOR, iterations);
			cluster = test.cluster(manhattan, centroids, k, dna, normSCH, normRAS, normTOR, iterations);

			c++;
		} while (c < iterations);

		for (int i = 0; i < k; i++) {
			System.out.println(cluster[i]);
			System.out.println(cluster[i].size());
		}

	}
}
