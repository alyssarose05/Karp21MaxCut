import java.io.*;
import java.util.*;

public class Karp21MaxCut {
  
	// Necessary variables
	public static int problemSize, sum, graphSum, firstCutIndex, secondCutIndex, solFirstCutIndex, solSecondCutIndex, subtractIndex, addIndex, solutionCount;
	public static File inputFile, outputFile; 
	public static Edge[] graph = new Edge[1000];
	public static Vertex[][] theSubsets, theSolution;
	
	// Position 0: Worst fitness, Position 1: Average fitness, Position 2: Best fitness
	public static double[] gen0, midgen, maxgen; 
	
	// Stopping criteria variables
	public static int bestSolCount;
	public static double bestFitness;
	public static int max_gen = 100;
	public static int max_sol = 10000;
	public static double max_fitness = 1;
	
	public static boolean inputFileFlag, outputFileFlag, bruteForceFlag, mathRandomFlag, bbsFlag;
	public static String probSize;
	
	// winner[0] = firstCutIndex, winner[1] = secondCutIndex
	public static int[] winner = new int[2]; 
	
	public static ArrayList<Double> fitnesses = new ArrayList<Double>();
  
	public static void main(String[] args) throws FileNotFoundException {
    
		// Flag system
		inputFileFlag = false;
		outputFileFlag = false;
		bruteForceFlag = false;
		mathRandomFlag = false;
		bbsFlag = false;
		
		for (int i = 0; i < args.length; i++) {
      
			// Input file flag
			if (args[i].equalsIgnoreCase("-inputFile")) { 
				inputFile = new File(args[i + 1]);
				inputFileFlag = true;
			} 
      
			// Output file flag
			if (args[i].equalsIgnoreCase("-outputFile")) { 
				outputFile = new File(args[i+1]);
				outputFileFlag = true;
			} 
			
			// Problem size flag
			if (args[i].equalsIgnoreCase("-size")) 
				probSize = args[i + 1];
			
			// Brute force flag
			if (args[i].equalsIgnoreCase("-bruteForce"))
				bruteForceFlag = true;
			
			// Math.random() flag
			if (args[i].equalsIgnoreCase("-mathRandom")) 
				mathRandomFlag = true;
			
			// BBS (Blum-Blum-Shub) flag
			if (args[i].equalsIgnoreCase("-bbs"))
				bbsFlag = true;
			
			// Max generation flag
			if (args[i].equalsIgnoreCase("-max_gen"))
				max_gen = Integer.parseInt(args[i + 1]);
			
			// Max solution flag
			if (args[i].equalsIgnoreCase("-max_sol"))
				max_sol = Integer.parseInt(args[i + 1]);
			
			// Max fitness flag
			if (args[i].equalsIgnoreCase("-max_fitness"))
				max_fitness = Double.parseDouble(args[i + 1]);
			
			
			// Help flag
			if (args[i].equals("--h") || args[i].equals("--help")) {
				System.out.println("FLAGS:");
				System.out.println("-inputFile <input-file>: For your input files (input.txt by default)");
				System.out.println("-outputFile <output-file>: For your output files (output.txt by default)");
				System.out.println("-size <size>: Input size here. Available sizes: small (>= 10), medium (>= 15), large (>= 20)");
				System.out.println("-bruteForce: Solve using brute force");
				System.out.println("-mathRandom: Solve using Math.random()");
				System.out.println("-bbs: Solve using the Blum-Blum-Shub (BBS) algorithm");
				System.out.println("-max_gen <number>: The maximum amount of generations you want to stop at (100 by default)");
				System.out.println("-max_sol <number>: The maximum amount of correct solutions you want to stop at (10000 by default)");
				System.out.println("-max_fitness <number>: The maximum fitness you want to stop at (1 by default)");
			} 
		} 
		
		// Default file names, if needed
		if (!inputFileFlag)
			inputFile = new File("input.txt"); 
		if (!outputFileFlag)
			outputFile = new File("output.txt"); 
		
		// Fill the graph with the input data, and get problem size
		Scanner scr = new Scanner(inputFile);
		int index = 1;
		int graphIndex = 0;
		while (scr.hasNext()) {
			Edge a = new Edge (new Vertex(index++), new Vertex(index),Integer.parseInt(scr.next()));
			graph[graphIndex++] = a;
			problemSize++;
		}
		
		// Verify problem sizes
		if (probSize.equals("small") && problemSize < 10) {
			System.out.println("The problem size must be at least 10 for small input.");
			System.exit(1);
		}
		
		else if (probSize.equals("medium") && problemSize < 15) {
			System.out.println("The problem size must be at least 15 for medium input.");
			System.exit(1);
		}
		
		else if (probSize.equals("large") && problemSize < 20) {
			System.out.println("The problem size must be at least 20 for large input.");
			System.exit(1);
		}
		
		// Initialize some variables
		secondCutIndex =  problemSize-1;
		firstCutIndex = 3;
		theSubsets = new Vertex[2][problemSize];
		theSolution = new Vertex[2][problemSize];
		for (int i = 0; i < graph.length && graph[i] != null; i++)
			graphSum += graph[i].weight;
		
		/* The subset array will be affected by changing the cut indices. Depending on their values,
		the index will change that will hence change how the final solution is displayed. 
		There are also add and subtract indices that are necessary to get all vertices to show, which vary*
		for every size and solution method. */
		
		// Initialize first half of first subset of subset array
		for (int i = 0; i < problemSize/2 - 1 && graph[i+1] != null; i++) 
			theSubsets[0][i] = graph[i].vertex1;
		
		// Initialize second half of first subset of subset array
		for (int i = problemSize/2 - 1; i < problemSize-2 && graph[i+1] != null; i++) 
			theSubsets[0][i] = graph[i+1].vertex2;
		
		// Initialize second subset of subset array
		for (int i = 0; i < problemSize-2 && graph[i+1] != null; i++) 
			theSubsets[1][i] = graph[i].vertex2;
		
		// Initialize generation 0, mid gen, and max gen data
		gen0 = new double[3];
		midgen = new double[3];
		maxgen = new double[3];
		
		// The rest of the methods to be executed: solution method and print output data
		if (bruteForceFlag) 
			bruteForce();
		else if (mathRandomFlag)
			mathRandom();
		else if (bbsFlag)
			bbs();
		
		if (bruteForceFlag || mathRandomFlag || bbsFlag) {
			fillSolution();
			printOutput();
		}
	}
	
	// The verification method, used to check if a solution is correct
	public static boolean isValid(Edge e1, Edge e2) {
		/* We need the largest sum of broken edges that a cut gives. So if the proposed sum is larger than the final one, then
		it needs to be updated accordingly. */
		return e1.weight + e2.weight > sum;
	}
	
	// The fitness function to rank solutions
	public static double fitness(int a, int b) {
		return (double) (a+b)/graphSum;
	}
	
	// The method that determines the final solution to the problem
	public static void fillSolution() {
		
		// Fill the first half of the first subset solution
		int i = 0;
		for (i = 0; i <= solFirstCutIndex && theSubsets[0][i] != null; i++) 
			theSolution[0][i] = theSubsets[0][i];
		
		// Fill the second half of the first subset solution
		for (int j = solSecondCutIndex+1; j < problemSize; j++)
			theSolution[0][i++] = theSubsets[0][j];
		
		// Fill the second subset solution
		i = 0;
		for (int j = solFirstCutIndex; j <= solSecondCutIndex && theSubsets[1][j] != null; j++)
			theSolution[1][i++] = theSubsets[1][j];
	}
	
	// Print the output data
	public static void printOutput() throws FileNotFoundException {
		System.setOut(new PrintStream(outputFile));
		
		// The problem name - KARP#21: Max Cut
		System.out.println("KARP#21: Max Cut\n");
		
		// Display the verticies and their corresponding weights
		System.out.println("Input data:");
		for (int i = 0; i < problemSize && graph[i] != null; i++)
			System.out.println("V" + (i+1) + ": " + graph[i].weight);
		
		// Display the solution to the user
		System.out.println("\nSolution: \n----------------------");
		
		// Display the first subset
		System.out.println("First subset: ");
		for (int i = 0; i < problemSize && theSolution[0][i] != null; i++)
			System.out.print(theSolution[0][i] + " ");
		
		// Display the second subset
		System.out.println("\nSecond subset: ");
		for (int i = 0; i < problemSize && theSolution[1][i] != null; i++)
			System.out.print(theSolution[1][i] + " ");
		
		// Display the solution count
		System.out.println("\n\nSolution count: " + solutionCount);
		
		if (!bruteForceFlag) {
			// Generation 0 statistics
			System.out.println("\nGeneration 0 statistics:");
			System.out.println("Worst fitness: " + gen0[0]);
			System.out.println("Average fitness: " + gen0[1]);
			System.out.println("Best fitness: " + gen0[2]);
		
			// Midpoint generation statistics
			System.out.println("\nMidpoint generation statistics:");
			System.out.println("Worst fitness: " + midgen[0]);
			System.out.println("Average fitness: " + midgen[1]);
			System.out.println("Best fitness: " + midgen[2]);
		
			// Maximum generation statistics
			System.out.println("\nMaximum generation statistics:");
			System.out.println("Worst fitness: " + maxgen[0]);
			System.out.println("Average fitness: " + maxgen[1]);
			System.out.println("Best fitness: " + maxgen[2]);
		}
		else {
			// Brute force
			System.out.println("\nStatistics:");
			System.out.println("Worst fitness: " + maxgen[0]);
			System.out.println("Average fitness: " + maxgen[1]);
			System.out.println("Best fitness: " + maxgen[2]);
		}
	}
	
	// Using brute force to find all possible solutions
	public static void bruteForce() {
		if (problemSize >= 10 && problemSize < 15)
			subtractIndex = 1;
		else if (problemSize >= 15 && problemSize < 20)
			subtractIndex = 2;
		else if (problemSize >= 20)
			subtractIndex = 3;
		
		// The brute force nested for-loop
		for (int i = 0; i < problemSize/2 - 1; i++) {
			for (int j = 1; j < problemSize/2; j++) {
				secondCutIndex--;
				
				// Calculate fitness and compare to current winner: closer to 1 = better fitness
				double currFitness = fitness(graph[i].weight,graph[problemSize-j-1].weight);
				fitnesses.add(currFitness);
				if (!(winner[0] == 0 && winner[1] == 0)) {
					if (currFitness > fitness(graph[winner[0]].weight,graph[winner[1]].weight)) {
						winner[0] = firstCutIndex;
						winner[1] = secondCutIndex;
						bestFitness = currFitness;
					}
					else {
						firstCutIndex = winner[0];
						secondCutIndex = winner[1];
					}
				}
				
				// If the verification process is successful, we need to update the final solution and cut indices.
				if (graph[i] != null && graph[problemSize-j-1] != null && isValid(graph[i],graph[problemSize-j-1])) {
					sum = graph[i].weight + graph[problemSize-j-1].weight;
					solFirstCutIndex = firstCutIndex;
					solSecondCutIndex = secondCutIndex;
					bestFitness = currFitness;
					bestSolCount++;
				}	
				solutionCount++;
			}
			
			if (bestSolCount >= max_sol) break;
			if (bestFitness >= max_fitness) break;
			secondCutIndex = problemSize-subtractIndex;
			if (firstCutIndex < problemSize / 2 - 1) firstCutIndex++;
		}
		
		// Determine statistics (Generations are not involved in brute force and are as such not included.)
		maxgen[0] = Collections.min(fitnesses);
		double overallSum = 0;
		for (int k = 0; k < fitnesses.size(); k++)
			overallSum += fitnesses.get(k);
		maxgen[1] = overallSum/fitnesses.size();
		maxgen[2] = bestFitness;
	}
	
	// Using Math.random() to obtain a random solution through a population of 1000 for 100 generations
	public static void mathRandom() {
		for (int i = 0; i < max_gen; i++) { // max_gen generations
			for (int j = 0; j < 1000; j++) { // A population of 1000
				firstCutIndex = 0;
				secondCutIndex = 0;

				while (firstCutIndex >= secondCutIndex) {
					firstCutIndex = (int) (Math.random() * problemSize-1);
					secondCutIndex = (int) (Math.random() * problemSize-1);
				}
				
				// Add current fitness to ArrayList for reference later
				double currFitness = fitness(graph[firstCutIndex].weight,graph[secondCutIndex].weight);
				fitnesses.add(currFitness);
				
				// Calculate fitness and compare to current winner: closer to 1 = better fitness
				if (!(winner[0] == 0 && winner[1] == 0)) {
					if (currFitness > fitness(graph[winner[0]].weight,graph[winner[1]].weight)) {
						winner[0] = firstCutIndex;
						winner[1] = secondCutIndex;
						bestFitness = currFitness;
					}
					else {
						firstCutIndex = winner[0];
						secondCutIndex = winner[1];
					}
				}
				
				// If the verification process is successful, we need to update the final solution and cut indices.
				if (graph[firstCutIndex] != null && graph[secondCutIndex] != null && isValid(graph[firstCutIndex],graph[secondCutIndex])) {
					sum = graph[firstCutIndex].weight + graph[secondCutIndex].weight;
					solFirstCutIndex = min(firstCutIndex,secondCutIndex);
					solSecondCutIndex = max(firstCutIndex,secondCutIndex);
					bestFitness = currFitness;
					bestSolCount++;
				}	
				
				// First run: initialize winner
				if (winner[0] == 0 && winner[1] == 0) {
					winner[0] = firstCutIndex;
					winner[1] = secondCutIndex;
				}
				solutionCount++;
			}
			
			
			// Generation 0
			if (i == 0) {
				gen0[0] = Collections.min(fitnesses);
				double overallSum = 0;
				for (int k = 0; k < fitnesses.size(); k++)
					overallSum += fitnesses.get(k);
				gen0[1] = overallSum/fitnesses.size();
				gen0[2] = bestFitness;
			}
			
			// Midpoint generation
			else if (i == max_gen/2) {
				midgen[0] = Collections.min(fitnesses);
				double overallSum = 0;
				for (int k = 0; k < fitnesses.size(); k++)
					overallSum += fitnesses.get(k);
				midgen[1] = overallSum/fitnesses.size();
				midgen[2] = bestFitness;
			}
			
			// Maximum generation
			else if (i == max_gen-1) {
				maxgen[0] = Collections.min(fitnesses);
				double overallSum = 0;
				for (int k = 0; k < fitnesses.size(); k++)
					overallSum += fitnesses.get(k);
				maxgen[1] = overallSum/fitnesses.size();
				maxgen[2] = bestFitness;
			}
			
			if (bestSolCount >= max_sol) break;
			if (bestFitness >= max_fitness) break;
		}
	}
	
	// Implement the Blum-Blum-Shub (BBS) algorithm
	public static void bbs() {
		// 3 variables given: Xi, P, and Q. Determine N from multiplying P and Q.
		double Xi = 127;
		double P = 67;
		double Q = 79;
		double N = P*Q;
		
		for (int i = 0; i < max_gen; i++) { // max_gen generations
			for (int j = 0; j < 1000; j++) { // A population of 1000
				
				Xi = exponent(Xi,2) % N;
				// Initialize Xi and cut indices
				
					
					firstCutIndex = (int) Math.round(Xi/(N-1) * problemSize-1);
					secondCutIndex = (int) Math.round(Xi/(N-1) * problemSize-1);
				

				// Since the indices could be less than 0, they'll be converted to 0 if they are since array indices can't be 0.
				if (firstCutIndex < 0) firstCutIndex = 0;
				if (secondCutIndex < 0) secondCutIndex = 0;
						
				// Calculate fitness and compare to current winner: closer to 1 = better fitness
				double currFitness = fitness(graph[firstCutIndex].weight,graph[secondCutIndex].weight);
				fitnesses.add(currFitness);
				if (!(winner[0] == 0 && winner[1] == 0)) {
					if (currFitness > fitness(graph[winner[0]].weight,graph[winner[1]].weight)) {
						winner[0] = firstCutIndex;
						winner[1] = secondCutIndex;
						bestFitness = currFitness;
					}
					else {
						firstCutIndex = winner[0];
						secondCutIndex = winner[1];
					}
				}
				
				// If the verification process is successful, we need to update the final solution and cut indices.
				if (graph[firstCutIndex] != null && graph[secondCutIndex] != null && isValid(graph[firstCutIndex],graph[secondCutIndex])) {
					sum = graph[firstCutIndex].weight + graph[secondCutIndex].weight;
					solFirstCutIndex = min(firstCutIndex,secondCutIndex);
					solSecondCutIndex = max(firstCutIndex,secondCutIndex);
					bestFitness = currFitness;
					bestSolCount++;
				}	
				
				// First run: initialize winner
				if (winner[0] == 0 && winner[1] == 0) {
					winner[0] = firstCutIndex;
					winner[1] = secondCutIndex;
				}
				solutionCount++;
			}
			
			// Generation 0
			if (i == 0) {
				gen0[0] = Collections.min(fitnesses);
				double overallSum = 0;
				for (int k = 0; k < fitnesses.size(); k++)
					overallSum += fitnesses.get(k);
				gen0[1] = overallSum/fitnesses.size();
				gen0[2] = bestFitness;
			}
			
			// Midpoint generation
			else if (i == max_gen/2) {
				midgen[0] = Collections.min(fitnesses);
				double overallSum = 0;
				for (int k = 0; k < fitnesses.size(); k++)
					overallSum += fitnesses.get(k);
				midgen[1] = overallSum/fitnesses.size();
				midgen[2] = bestFitness;
			}
			
			// Maximum generation
			else if (i == max_gen-1) {
				maxgen[0] = Collections.min(fitnesses);
				double overallSum = 0;
				for (int k = 0; k < fitnesses.size(); k++)
					overallSum += fitnesses.get(k);
				maxgen[1] = overallSum/fitnesses.size();
				maxgen[2] = bestFitness;
			}
			
			if (bestSolCount >= max_sol) break;
			if (bestFitness >= max_fitness) break;
		}
	}
	
	// Min method used by mathRandom() and bbs()
	public static int min(int a, int b) {
		if (a < b)
			return a;
		return b;
	}
	
	// Max method used by mathRandom() and bbs()
	public static int max(int a, int b) {
		if (a > b)
			return a;
		return b;
	}
	
	// Exponent method used by bbs() (num^exp)
	public static double exponent(double num, int exp) {
		int solution = 1;
		for (int i = 0; i < exp; i++) {
			solution *= num;
		}
		return solution;
	}
}