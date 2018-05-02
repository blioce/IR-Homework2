package edu.uw.blioce.IR_HW2;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * This program will take in a text file containing three columns that
 * will be used to create an adjacency matrix. This will simulate the 
 * linkage between websites in order to create a ranking of the popularity
 * or importance of each website. It will do this by creating a Random
 * Surfer Model of the links and will converge at the optimal ranking of 
 * each webpage. 
 * 
 * @author Brandon Lioce
 * @version May 2, 2018
 * @class Information Retrieval - Spring 2018
 * @assignment HW2 - Page Rank
 *
 */
public class App {

	/** The graph.txt file. */
	private static final String GRAPH_FILE = "src/graph.txt";

	/** The dampening factor for use in Random Surfer Model creation. */
	private static final double DAMP_FACTOR = 0.85;
	
	/** The threshold to stop computation after the total difference between old and new matrix. */
	private static final double THRESHOLD = 0.05;

	private static Map<Integer, Node> nodes;

	private static double[][] matrix;

	private static double[] finalRank;
	
	private static Map<String, Integer> mapping;

	/**
	 * The main entry to the program that will set up data structures
	 * the initiate the appropriate processing of the data.
	 * 
	 * @param args No arguments are used/passed to the program.
	 * @throws FileNotFoundException An exception is thrown if the file is not found.
	 */
	public static void main(String[] args) throws FileNotFoundException {
		nodes = new HashMap<Integer, Node>();
		scanGraphFile();

		matrix = new double[mapping.size()][mapping.size()];
		createStochMatrix();

		System.out.println("The Stochastic matrix before processing:");
		System.out.println(matrixToString(matrix));

		int itr = randomSurfer();
		System.out.println("\nComputation took " + itr + " iterations to converge "
				+ "(total difference between new rank and last rank less than " + THRESHOLD +").");
		System.out.println("\nThe new rank vector:");
		for(double d: finalRank) System.out.print(d + " ");
		System.out.println("\n");
		System.out.println(getFinalRank());
	}

	/**
	 * This method scans through the graph file to create a record of
	 * the linkages between nodes using Node objects. This will keep 
	 * track of outgoing links and be used to create a Stochastic matrix.
	 * 
	 * @throws FileNotFoundException An exception is thrown if the file is not found.
	 */
	private static void scanGraphFile() throws FileNotFoundException {
		Scanner scan = new Scanner(new File(GRAPH_FILE));
		
		mapping = new HashMap<String, Integer>();
		int nextMapping = 0;

		while(scan.hasNextLine()) {
			String line = scan.nextLine();
			String[] tokens = line.split(" ");
			String a = tokens[0];
			String b = tokens[1];
			if(!mapping.containsKey(a)) {
				mapping.put(a, nextMapping++);
			}
			if(!mapping.containsKey(b)) {
				mapping.put(b, nextMapping++);
			}
			Integer hasLink = Integer.valueOf(tokens[2]);

			if(nodes.containsKey(mapping.get(a))) {
				Node n = nodes.get(mapping.get(a));
				if(hasLink == 1) n.pointsTo.add(mapping.get(b));
			} else {
				Node n = new Node(mapping.get(a));
				if(hasLink == 1) n.pointsTo.add(mapping.get(b));
				nodes.put(mapping.get(a), n);
			}
		}

		scan.close();
	}

	/**
	 * This method uses the nodes object to create a Stochastic matrix
	 * of the nodes and links between them. After this is completed, each
	 * column in the matrix will sum to 1.
	 */
	private static void createStochMatrix() {
		for(int i: nodes.keySet()) {
			Node n = nodes.get(i);
			for(Integer j: n.pointsTo) {
				matrix[j][i] = 1.0 / n.pointsTo.size();
			}
		}
	}

	/**
	 * This method returns a String representation of the matrix passed to it.
	 * 
	 * @param matrix The matrix to be converted to a String.
	 * @return A String representation of the matrix.
	 */
	private static String matrixToString(double[][] matrix) {
		DecimalFormat df = new DecimalFormat("0.000");
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < matrix.length; i++) {
			for(int j = 0; j < matrix[i].length; j++) {
				sb.append(df.format(matrix[i][j]) + " ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * This method implements the Random Surfer Model computation. 
	 * v' = (beta)Mv + (1 - (beta))e/n
	 * And repeats this computation until the rank vector converges
	 * (does not significantly change with any further iterations).
	 */
	private static int randomSurfer() {
		double[] compVect = new double[mapping.size()];
		for(int i = 0; i < compVect.length; i++) {
			compVect[i] = (1.0 - DAMP_FACTOR) * (1.0 / compVect.length);
		}

		double[] rankVect = new double[mapping.size()];
		for(int i = 0; i < rankVect.length; i++) {
			rankVect[i] = 1.0 / mapping.size();
		}

		System.out.println("The rank vector - iteration 0:");
		for(double d: rankVect) System.out.print(d + " ");
		System.out.println();
		
		int iterations = 0;
		double diff = 0;
		do {
			diff = 0;
			// First, multiply matrix M by the beta value (dampening factor)
			for(int i = 0; i < matrix.length; i++) {
				for(int j = 0; j < matrix[i].length; j++) {
					matrix[i][j] = matrix[i][j] * DAMP_FACTOR;
				}
			}

			// Next, multiply matrix M by the rank vector
			double[] newRank = new double[mapping.size()];
			for(int i = 0; i < matrix.length; i++) {
				double sum = 0;
				for(int j = 0; j < matrix[i].length; j++) sum += matrix[i][j] * rankVect[i];
				newRank[i] = sum;
			}

			// Finally, add the rank vector and component vector
			for(int i = 0; i < newRank.length; i++) newRank[i] += compVect[i];

			// Compute difference between old and new matrix.
			// Stop when difference reaches THRESHOLD 
			for(int i = 0; i < newRank.length; i++) diff += Math.abs(newRank[i] - rankVect[i]);

			rankVect = newRank;
			iterations++;
		} while (diff >= THRESHOLD);

		finalRank = rankVect;
		return iterations;
	}

	/**
	 * Returns a string value of the final ranking with each node name
	 * listed with its rank.
	 * @return
	 */
	private static String getFinalRank() {
		StringBuilder sb = new StringBuilder();
		sb.append("Final ranking - the higher score, the higher the rank:\n");
		sb.append("Node\tRank Score\n");
		
		List<NodeRank> ranking = new ArrayList<NodeRank>();
		for(String s: mapping.keySet()) {
			ranking.add(new NodeRank(s, finalRank[mapping.get(s)]));
		}
		
		Collections.sort(ranking);
		for(NodeRank n: ranking) {
			sb.append(n.toString() + "\n");
		}
		return sb.toString();
	}
}

/**
 * The Node class is used to store the documents and their
 * links in the initial, graph scanning step.
 */
class Node {
	Integer document;
	Set<Integer> pointsTo;

	public Node(Integer doc) {
		document = doc;
		pointsTo = new HashSet<Integer>();
	}
}

/** 
 * NodeRank class is used to compare different nodes of different ranks
 * in the final processing step.
 *
 */
class NodeRank implements Comparable<NodeRank>{
	String document;
	Double rank;
	
	public NodeRank(String doc, double rank) {
		document = doc;
		this.rank = rank;
	}

	public int compareTo(NodeRank o) {
		return o.rank.compareTo(this.rank);
	}
	
	public String toString() {
		return document + ": " + rank;
	}
}
