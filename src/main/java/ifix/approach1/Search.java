package ifix.approach1;

import ifix.fitness.FitnessFunction;
import ifix.fitness.FixTuple;
import util.Constants;
import util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Search 
{
	private FitnessFunction optimalFitness;
	
	private static List<Map<String, Double>> clusterGenerationFitnessScorePhase1;
	private static Map<String, Double> generationFitnessScorePhase2;
	
	private static List<GraphPlotting> fitnessScoreTrendGraphPhase1;
	
	public FitnessFunction getOptimalFitness() 
	{
		return optimalFitness;
	}

	public static List<Map<String, Double>> getClusterGenerationFitnessScorePhase1()
	{
		return clusterGenerationFitnessScorePhase1;
	}

	public static void setClusterGenerationFitnessScorePhase1(List<Map<String, Double>> clusterGenerationFitnessScorePhase1)
	{
		Search.clusterGenerationFitnessScorePhase1 = clusterGenerationFitnessScorePhase1;
	}

	public static Map<String, Double> getGenerationFitnessScorePhase2()
	{
		return generationFitnessScorePhase2;
	}

	public static void setGenerationFitnessScorePhase2(Map<String, Double> generationFitnessScorePhase2)
	{
		Search.generationFitnessScorePhase2 = generationFitnessScorePhase2;
	}

	public static List<GraphPlotting> getFitnessScoreTrendGraphPhase1()
	{
		return fitnessScoreTrendGraphPhase1;
	}

	public static void setFitnessScoreTrendGraphPhase1(List<GraphPlotting> fitnessScoreTrendGraphPhase1)
	{
		Search.fitnessScoreTrendGraphPhase1 = fitnessScoreTrendGraphPhase1;
	}

	/* GENETIC ALGORITHM TO FIND CANDIDATE SOLUTION FOR EACH GROUP OF DEPENDENT CLUSTERS */
	public Solution phase1Search(List<String> dependentClusters)
	{
		
		Map<String, Double> generationFitnessScore = new TreeMap<String, Double>();
		
		Phase1 p1 = new Phase1(dependentClusters);
		int generationCount = 1;
		int saturationCount = 0;
		Phase1.setCurrentGenerationNumber(generationCount);
		
		// initialize population
		System.out.println("Initialize population");
		p1.initializePopulation();
		p1.printPopulation(p1.getCurrentPopulation());

		// sort current population by fitness score
		p1.sortPopulationByFitnessScore(p1.getCurrentPopulation());
		System.out.println("Sorted initial population by fitness score (ascending order) =");
		p1.printPopulation(p1.getCurrentPopulation());
		
		// start loop
		double prevGenFitnessScore = p1.getCurrentPopulation().get(0).getFitnessScoreObj().getFitnessScore();
		while(!p1.isTerminate(generationCount, saturationCount))
		{
			Phase1.setCurrentGenerationNumber(generationCount);
			generationFitnessScore.put(MainIterator.getIterationCount() + "_" + generationCount, prevGenFitnessScore);
			
			System.out.println("**** phase 1 generation " + generationCount);
			
			p1.setNextGeneration(p1.getCurrentPopulation());
			
			// run AVM search
			System.out.println("Running AVM search (isolating variables)");
			p1.performAVMSearch(true);
			System.out.println("Population after AVM search (isolating variables) =");
			p1.printPopulation(p1.getNextGeneration());
			
			/*System.out.println("Running AVM search (not isolating variables)");
			p1.performAVMSearch(false);
			System.out.println("Population after AVM search (not isolating variables) =");
			p1.printPopulation(p1.getNextGeneration());*/

			
			// crossover
			/*System.out.println("Running crossover (weighted AVG)");
			p1.crossover(true);
			System.out.println("Population after crossover (weighted AVG)=");
			p1.printPopulation(p1.getNextGeneration());*/
			/*
			System.out.println("Running crossover (swap)");
			p1.crossover(false);
			System.out.println("Population after crossover (swap)=");
			p1.printPopulation(p1.getNextGeneration());
			 */
			//try different mutations
			double bellWidth = 8.0;
			//for (double bellWidth = 80; bellWidth >= 8.0; bellWidth -= 8) {
				// mutation
				System.out.println("Running mutation -" + bellWidth);
				p1.mutation(bellWidth);
				System.out.println("Population after mutation" + bellWidth + "=");
				p1.printPopulation(p1.getNextGeneration());
			//}
			
			
			// select
			p1.select();
			System.out.println("New current population after selection =");
			p1.printPopulation(p1.getCurrentPopulation());
			
			if(prevGenFitnessScore == p1.getCurrentPopulation().get(0).getFitnessScoreObj().getFitnessScore())
			{
				saturationCount++;
			}
			else
			{
				saturationCount = 0;
			}
			prevGenFitnessScore = p1.getCurrentPopulation().get(0).getFitnessScoreObj().getFitnessScore();
			generationCount++;
		}
		clusterGenerationFitnessScorePhase1.add(generationFitnessScore);
		
		// add fix tuples
		Solution solution = new Solution();
		for(Gene g : p1.getCurrentPopulation().get(0).getGenes())
		{
			for(FixTuple ft : g.getFixTuples())
			{
				solution.addFixTuple(ft);
			}
		}
		solution.setFitnessScoreObject(p1.getCurrentPopulation().get(0).getFitnessScoreObj().copy());
		System.out.println("++Best Chromosome is: "+ p1.getCurrentPopulation().get(0));
		for(Gene g: p1.getCurrentPopulation().get(0).getGenes()){
			for (FixTuple ft : g.getFixTuples()) {
				System.out.println("++++FixTuple in the chormosome: "+ft);
			}
		}
		return solution;
	}
	
	/* GENETIC ALGORITHM TO FIND BEST COMBINATION OF CANDIDATE SOLUTIONS */
	public List<Solution> phase2Search(List<Solution> candidateSolutions)
	{
		System.out.println("List of candidate solutions = ");
		int count = 1;
		for(Solution s : candidateSolutions)
		{
			System.out.println(count + ". " + s.getFixTuples());
			count++;
		}
		
		Phase2 p2 = new Phase2(candidateSolutions);
		// initialize population
		System.out.println("Initialize population");
		p2.initializePopulation();
		p2.printCurrentPopulation(p2.getCurrentPopulation());
		
		// sort current population by fitness score
		p2.setCurrentPopulation(p2.sortPopulationByFitnessScore(p2.getCurrentPopulation()));
		System.out.println("Sorted initial population by fitness score (ascending order) =");
		p2.printCurrentPopulation(p2.getCurrentPopulation());
		
		// start loop
		int generationCount = 1;
		int saturationCount = 0;

		double prevGenFitnessScore = p2.computeFitnessScore(p2.getCurrentPopulation().get(0));
		while(!p2.isTerminate(generationCount, saturationCount))
		{
			generationFitnessScorePhase2.put(MainIterator.getIterationCount() + "_" + generationCount, prevGenFitnessScore);
			
			System.out.println("**** phase 2 generation " + generationCount);
			
			p2.setNextGeneration(p2.getCurrentPopulation());
			
			// crossover
			System.out.println("Running crossover");
			p2.crossover();
			System.out.println("Population after crossover =");
			p2.printCurrentPopulation(p2.getNextGeneration());
			
			// mutation
			System.out.println("Running mutation");
			p2.mutate();
			System.out.println("Population after mutation =");
			p2.printCurrentPopulation(p2.getNextGeneration());
			
			// select
			p2.select();
			System.out.println("New current population after selection =");
			p2.printCurrentPopulation(p2.getCurrentPopulation());
			
			if(prevGenFitnessScore == p2.getOptimalFitness().getFitnessScore())
			{
				saturationCount++;
			}
			else
			{
				saturationCount = 0;
			}
			prevGenFitnessScore = p2.getOptimalFitness().getFitnessScore();
			generationCount++;
		}
		
		
		// get solution from binary string
		System.out.println("Phase 2 optimal solution binary string = " + p2.getOptimalBinaryString());
		List<Solution> solution = new ArrayList<Solution>();
		String solutionBinaryString = p2.getCurrentPopulation().get(0);
		for(int i = 0; i < solutionBinaryString.length(); i++)
		{
			if(solutionBinaryString.charAt(i) == '1')
				solution.add(candidateSolutions.get(i));
		}
		optimalFitness = p2.getOptimalFitness().copy();
		return solution;
	}

	public void setOptimalFitness(Solution solution) {
		Util.resetPageToOriginalState();
		Util.applyChangesToPage(solution.getFixTuples(), false);
		List<Integer> changeValues = new ArrayList<>();
		
		for(FixTuple ft : solution.getFixTuples())
		{
			if(!ft.isParentFix())
				changeValues.add(ft.getchangeValue());
		}

		
		optimalFitness = new FitnessFunction();		
		optimalFitness.calculateFitnessScore(solution.getFixTuples());
		
	}
}
