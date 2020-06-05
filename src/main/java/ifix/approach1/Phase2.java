package ifix.approach1;

import ifix.fitness.FitnessFunction;
import ifix.fitness.FixTuple;
import util.Constants;
import util.Util;

import java.math.BigInteger;
import java.util.*;

public class Phase2
{
	private List<Solution> candidateSolutions;
	
	private List<String> currentPopulation;
	private List<String> nextGeneration;
	private int binaryStringLength;
	private Map<BigInteger, FitnessFunction> fitnessScoreCache;
	
	private String optimalBinaryString;
	private FitnessFunction optimalFitness;
	
	public Phase2(List<Solution> candidateSolutions)
	{
		this.candidateSolutions = candidateSolutions;
		this.binaryStringLength = candidateSolutions.size();
		this.currentPopulation = new ArrayList<String>();
		this.nextGeneration = new ArrayList<String>();
		this.fitnessScoreCache = new HashMap<BigInteger, FitnessFunction>();
		
		char[] allOnBinaryChars = new char [binaryStringLength];
		Arrays.fill(allOnBinaryChars, '0');
		String allOff = new String(allOnBinaryChars);
		fitnessScoreCache.put(getDecimalValueForBinaryString(allOff), MainIterator.getOriginalFitness());
	}
	
	public List<Solution> getCandidateSolutions()
	{
		return candidateSolutions;
	}
	public void setCandidateSolutions(List<Solution> candidateSolutions)
	{
		this.candidateSolutions = candidateSolutions;
	}
	public List<String> getCurrentPopulation()
	{
		return currentPopulation;
	}
	public void setCurrentPopulation(List<String> currentPopulation)
	{
		this.currentPopulation = new ArrayList<String>(currentPopulation);
	}
	public List<String> getNextGeneration()
	{
		return nextGeneration;
	}
	public void setNextGeneration(List<String> nextGeneration)
	{
		this.nextGeneration = new ArrayList<String>(nextGeneration);
	}

	public BigInteger getDecimalValueForBinaryString(String bin)
	{
		return new BigInteger(bin, 2);
	}
	
	public String getBinaryStringForDecimalValue(BigInteger dec)
	{
		return String.format("%" + binaryStringLength + "s", dec.toString(2)).replace(' ', '0');
	}
	
	public String getOptimalBinaryString() {
		return optimalBinaryString;
	}

	public FitnessFunction getOptimalFitness() {
		return optimalFitness;
	}

	public void setOptimalBinaryString(String optimalBinaryString) {
		this.optimalBinaryString = optimalBinaryString;
	}

	public void initializePopulation()
	{
		// add first chromosome with all switches on
		char[] allOnBinaryChars = new char [binaryStringLength];
		Arrays.fill(allOnBinaryChars, '1');
		String allOn = new String(allOnBinaryChars);
		currentPopulation.add(allOn);
		computeFitnessScore(allOn);
		
		// initialize population using roulette wheel with stochastic acceptance
		for (int i = 0; i < Constants.POPULATION_SIZE_APPROACH1_PHASE2-1; i++)
		{
			String bitString = runRouletteWheelToGetBinaryString();
			currentPopulation.add(bitString);
			computeFitnessScore(bitString);
		}
	}
	
	public String selectParent()
	{
		// roulette wheel selection
		double populationSum = 0;
		List<String> tempPopulation = new ArrayList<String>(currentPopulation);
		
		// shuffle chromosomes in the population
		Collections.shuffle(tempPopulation);
		
		for(String c : tempPopulation)
		{
			populationSum = populationSum + fitnessScoreCache.get(getDecimalValueForBinaryString(c)).getFitnessScore();
		}
		
		double r = Math.random();
		double sum = 0.0;
		for(String c : tempPopulation)
		{
			double probability = fitnessScoreCache.get(getDecimalValueForBinaryString(c)).getFitnessScore() / populationSum;
			sum = sum + (1 - probability); // (1 - probability) -> as the fitness function is minimizing 
			if(r < sum)
			{
				return c;
			}
		}
		return currentPopulation.get(0);
	}
	
	private String runRouletteWheelToGetBinaryString()
	{
		int n = binaryStringLength;
		double [] weight = new double [n];
		double max_weight = Double.MIN_VALUE;
		int cnt = 0;
		for(Solution s : candidateSolutions)
		{
			weight[cnt] = s.getFitnessScoreObject().getFitnessScore();
			if(weight[cnt] > max_weight)
			{
				max_weight = weight[cnt];
			}
			cnt++;
		}
		
		boolean isUnique = false;
		int UNIQUE_STRING_MAX_TRIES = Math.min(10, (int)Math.pow(2, binaryStringLength) - 1);
		int uniqueTriesCnt = 0;
		String binaryString = currentPopulation.get(0);
		do
		{
			char[] binaryChars = new char [n];
			Arrays.fill(binaryChars, '0');
			int index = 0;
			boolean notaccepted;
			for (int j = 0; j < n; j++)
			{
				notaccepted = true;
				while (notaccepted)
				{
					index = (int) (n * Math.random());
					if (Math.random() < weight[index] / max_weight)
					{
						notaccepted = false;
					}
				}
				binaryChars[index] = '1';
			}
			binaryString = new String(binaryChars);
			if(!fitnessScoreCache.containsKey(getDecimalValueForBinaryString(binaryString)))
			{
				isUnique = true;
			}
			else if(uniqueTriesCnt >= UNIQUE_STRING_MAX_TRIES)
			{
				isUnique = true;
			}
			uniqueTriesCnt++;
		} while(!isUnique);
		
		return binaryString;
	}
	
	public void crossover()
	{
		for(int n = 0; n < currentPopulation.size(); n++)
		{
			String parent1 = selectParent();
			String parent2 = selectParent();
			
			// uniform crossover
			int numberOfGenesToProcess = Util.getRandomIntValueInRange(1, parent1.length()+1);
			Set<Integer> geneIndicesToProcess = new HashSet<Integer>();
			for(int i = 0; i < numberOfGenesToProcess; i++)
			{
				int randomGeneIndex = Util.getRandomIntValueInRange(0, parent1.length());
				geneIndicesToProcess.add(randomGeneIndex);
			}
			
			String child1 = createChildUniformCrossover(parent1, parent2, geneIndicesToProcess);
			computeFitnessScore(child1);
			nextGeneration.add(child1);
			
			String child2 = createChildUniformCrossover(parent2, parent1, geneIndicesToProcess);
			computeFitnessScore(child2);
			nextGeneration.add(child2);
		}
	}
	
	private String createChildUniformCrossover(String parent1, String parent2, Set<Integer> geneIndicesToProcess)
	{
		StringBuilder child = new StringBuilder(parent1);
		for(int i : geneIndicesToProcess)
		{
			child.setCharAt(i, parent2.charAt(i));
		}
		return child.toString();
	}
	
	public void mutate()
	{
		int numberOfChromosomesToMutate = Util.getRandomIntValueInRange(1, currentPopulation.size()+1);
		for (int i = 0; i < numberOfChromosomesToMutate; i++)
		{
			int randomChromosomeIndex = Util.getRandomIntValueInRange(0, currentPopulation.size());
			int numberOfGenesToMutate = Util.getRandomIntValueInRange(1, currentPopulation.get(randomChromosomeIndex).length()+1);
			String oldChromosome = currentPopulation.get(randomChromosomeIndex);
			StringBuilder newChromosome = new StringBuilder(oldChromosome);
			
			for (int j = 0; j < numberOfGenesToMutate; j++)
			{
				int randomGeneIndex = Util.getRandomIntValueInRange(0, currentPopulation.get(randomChromosomeIndex).length());
				char newBit = (oldChromosome.charAt(randomGeneIndex) == '0' ? '1' : '0');
				newChromosome.setCharAt(randomGeneIndex, newBit);
			}
			computeFitnessScore(newChromosome.toString());
			nextGeneration.add(newChromosome.toString());
		}
	}
	
	public void select()
	{
		// sort population by fitness score
		setNextGeneration(sortPopulationByFitnessScore(nextGeneration));
		
		// add only top "POPULATION_SIZE" chromosomes to the new current population
		currentPopulation = new ArrayList<String>();
		for(int i = 0; i < Constants.POPULATION_SIZE_APPROACH1_PHASE2 && i < nextGeneration.size(); i++)
		{
			currentPopulation.add(nextGeneration.get(i));
		}
		optimalBinaryString = currentPopulation.get(0);
		computeFitnessScore(optimalBinaryString);
		optimalFitness = fitnessScoreCache.get(getDecimalValueForBinaryString(optimalBinaryString));
	}
	
	public List<String> sortPopulationByFitnessScore(List<String> population)
	{
		Map<BigInteger, Double> populationSortedByFitnessScore = new LinkedHashMap<>();
		for(String chromosome : population)
		{
			populationSortedByFitnessScore.put(getDecimalValueForBinaryString(chromosome), computeFitnessScore(chromosome));
		}
		populationSortedByFitnessScore = Util.sortMapByValue(populationSortedByFitnessScore);
		population = new ArrayList<String>();
		for(BigInteger i : populationSortedByFitnessScore.keySet())
		{
			population.add(getBinaryStringForDecimalValue(i));
		}
		return population;
	}
	
	public double computeFitnessScore(String binaryStringSolution)
	{
		FitnessFunction fitnessFunction = null;
		
		System.out.println("Compute fitness score for binary string \"" + binaryStringSolution + "\"");
		if(fitnessScoreCache.containsKey(getDecimalValueForBinaryString(binaryStringSolution)))
		{
			fitnessFunction = fitnessScoreCache.get(getDecimalValueForBinaryString(binaryStringSolution));
			//System.out.println("Fitness score returned from cache = " + fitnessFunction.getFitnessScore());
			
			FitnessFunction.incrementCachedPhase2FitnessCalls();
		}
		else
		{
			// get all "on" candidate solutions
			List<Solution> solution = new ArrayList<Solution>();
			for(int i = 0; i < binaryStringSolution.length(); i++)
			{
				if(binaryStringSolution.charAt(i) == '1')
					solution.add(candidateSolutions.get(i));
			}
			
			// apply new values to the test page
			List<FixTuple> tuples = new ArrayList<>();
			List<Integer> changeValues = new ArrayList<>();
			for(Solution s : solution)
			{
				for(FixTuple ft : s.getFixTuples())
				{
					FixTuple t = new FixTuple(ft.getXpath(), ft.getCssProperty(), ft.getchangeValue(), ft.getClusterId());
					tuples.add(t);
					if(!ft.isParentFix())
						changeValues.add(t.getchangeValue());
				}
			}
			
			 Util.applyChangesToPage(tuples, false);
			
			// get fitness score
			fitnessFunction = new FitnessFunction();
			fitnessFunction.calculateFitnessScore(tuples);
			fitnessScoreCache.put(getDecimalValueForBinaryString(binaryStringSolution), fitnessFunction.copy());
			
			FitnessFunction.incrementPhase2FitnessCalls();
		}
		
		FitnessFunction.getObjectives().add(new Double[]{Util.round((double) fitnessFunction.getInconsistenciesObjective()), Util.round(fitnessFunction.getAestheticObjective())});
		
		return fitnessFunction.getFitnessScore();
	}
	
	public boolean isTerminate(int generationCount, int saturationCount)
	{
		if(generationCount > Constants.MAX_GENERATIONS_APPROACH1_PHASE2)
			System.out.println("Terminating because max generations reached");
		else if(saturationCount >= Constants.SATURATION_POINT_APPROACH1_PHASE2)
			System.out.println("Terminating because saturation point reached");
		
		return generationCount > Constants.MAX_GENERATIONS_APPROACH1_PHASE2 || 
		saturationCount >= Constants.SATURATION_POINT_APPROACH1_PHASE2;
	}
	
	public void printCurrentPopulation(List<String> population)
	{
		int count = 1;
		System.out.println("(Size = " + population.size() + ")");
		for(String c : population)
		{
			System.out.println(count + ". " + c + " -> " + fitnessScoreCache.get(getDecimalValueForBinaryString(c)));
			count++;
		}
	}
}
