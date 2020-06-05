package ifix.approach1;

import ifix.fitness.FitnessFunction;
import ifix.fitness.FixTuple;
import ifix.heuristic.Heuristic;
import ifix.input.ReadInput;
import util.Constants;
import util.Util;

import java.util.*;


public class Phase1
{
	private List<Chromosome> currentPopulation;
	private List<Chromosome> nextGeneration;
	private List<String> dependentClusters;
	private Chromosome originalChromosome;
	private static int currentGenerationNumber;
	
	enum stepName 
	{
		initialization, avm, crossover, mutation, selection;
	}
	
	public Phase1(List<String> dependentClusters)
	{
		this.dependentClusters = dependentClusters;
		this.currentPopulation = new ArrayList<Chromosome>();
		this.nextGeneration = new ArrayList<Chromosome>();
	}

	public List<Chromosome> getCurrentPopulation()
	{
		return currentPopulation;
	}

	public void setCurrentPopulation(List<Chromosome> currentPopulation)
	{
		this.currentPopulation = currentPopulation;
	}

	public List<String> getDependentClusters()
	{
		return dependentClusters;
	}

	public void setDependentClusters(List<String> dependentClusters)
	{
		this.dependentClusters = dependentClusters;
	}

	public List<Chromosome> getNextGeneration()
	{
		return nextGeneration;
	}

	public void setNextGeneration(List<Chromosome> nextGeneration)
	{
		this.nextGeneration = new ArrayList<Chromosome>(nextGeneration);
	}

	public Chromosome getOriginalChromosome() {
		return originalChromosome;
	}

	public static int getCurrentGenerationNumber()
	{
		return currentGenerationNumber;
	}

	public static void setCurrentGenerationNumber(int currentGenerationNumber)
	{
		Phase1.currentGenerationNumber = currentGenerationNumber;
	}
	
	


	public void initializePopulation()
	{
		Map<String, Map<String, Integer>> initMap = new HashMap<>();
		for(String cId : dependentClusters)
		{
			Map<String, Integer> map = new HashMap<>();
			for(String cssProperty : Constants.CSS_PROPERTIES_MASTER_LIST)
			{
				// put change value from analytical approach
				Heuristic h = new Heuristic(ReadInput.getRefDriver(), ReadInput.getTestDriver(), MainIterator.getGwali().getMatchedNodes());
				int changeValue = h.getEstimatedChangeValue(cssProperty, cId);
				map.put(cssProperty, changeValue);
			}
			initMap.put(cId, map);
		}
		
		// create original chromosome with 0 ChangeValue
		originalChromosome = new Chromosome();
		for(String cId : dependentClusters)
		{
			for(String cssProperty : Constants.CSS_PROPERTIES_MASTER_LIST)
			{
				int changeValue = 0;
				Gene gene = new Gene(cId, cssProperty, changeValue);
				
				// carry forward change values from previous iteration
				for(FixTuple ft : MainIterator.getSolution().getFixTuples())
				{
					if(!ft.isParentFix() && ft.getClusterId().equalsIgnoreCase(cId) && ft.getCssProperty().equalsIgnoreCase(cssProperty))
					{
						gene.setChangeValue(ft.getchangeValue());
					}
				}
				originalChromosome.addGene(gene);
			}
		}
		Chromosome orignialCopy = originalChromosome.copy();
		String currentStepName = stepName.initialization.toString() + "-original_" + currentGenerationNumber;
		orignialCopy.addOrigin(currentStepName);
		FitnessFunction.computeFitnessScore(orignialCopy, stepName.initialization.toString());
		currentPopulation.add(orignialCopy);

		
		
		// create one chromosome for each CSS property with the exact estimated values returned from analytical approach
		for(String cssProperty: Constants.CSS_PROPERTIES_MASTER_LIST){
			Chromosome chromosome = originalChromosome.copy();
			for(Gene g : chromosome.getGenes())
			{
				if(g.getCssProperty().equals(cssProperty)){
					int changeValue = initMap.get(g.getClusterId()).get(g.getCssProperty());
					g.setChangeValue(changeValue);
				}
			}	
			currentStepName = stepName.initialization.toString() + "-analytical" + currentGenerationNumber;
			chromosome.addOrigin(currentStepName);
			FitnessFunction.computeFitnessScore(chromosome, stepName.initialization.toString());
			
			//insert chromosomes
			currentPopulation.add(chromosome);
		}
		
		

		int remainingPopulationSize = Constants.POPULATION_SIZE_APPROACH1_PHASE1 - currentPopulation.size();
		List<Chromosome> remainingPopulation = new ArrayList<Chromosome>();

		// choose remaining initial population by randomly mutating the initial population
		double changeProbability = 1.0 / (double)originalChromosome.getGenes().size();
		for(int i = 0; i < remainingPopulationSize; i++)
		{
		//while(!isTimeOut()){
			int randomChromosomeIndex = Util.getRandomIntValueInRange(0, currentPopulation.size());
			
			Chromosome chromosome = currentPopulation.get(randomChromosomeIndex).copy();
			for(Gene g : chromosome.getGenes())
			{
				if(Util.getRandomDoubleValueInRange(0, 1) < changeProbability)
				{
					int changeValue = getGaussianValue(g, 8.0);
					g.setChangeValue(changeValue);
				}
			}
			currentStepName = stepName.initialization.toString() + "-random" + currentGenerationNumber;
			chromosome.addOrigin(currentStepName);
			FitnessFunction.computeFitnessScore(chromosome, stepName.initialization.toString());
			remainingPopulation.add(chromosome);
		}
		
		currentPopulation.addAll(remainingPopulation);
		
	}
	
	private boolean isTimeOut() {
		long timeout = Constants.TIME_OUTS_SEC.get(ReadInput.getSubjectName());
		long currentTimeNano = System.nanoTime() - Approach1.approachStartTime;
		double currentTime = Util.convertNanosecondsToSeconds(currentTimeNano);
		
		if(currentTime > timeout)
			return true;
		return false;
	}

	public void crossover(boolean weightedAverageMode)
	{
		String crossoverMode = weightedAverageMode ? "weightedAVG" : "swap";
		int numberOfCrossovers = (int) Math.round(currentPopulation.size() * Constants.CROSSOVER_RATE_PHASE1);
		System.out.println("Number of crossovers to be performed = " + numberOfCrossovers);
		for(int n = 0; n < numberOfCrossovers; n++)
		{
			Chromosome parent1 = selectParent();
			Chromosome parent2 = selectParent();
			
			// uniform crossover
			int numberOfGenesToProcess = Util.getRandomIntValueInRange(1, parent1.getGenes().size() + 1);
			Set<Integer> geneIndicesToProcess = new HashSet<Integer>();
			for(int i = 0; i < numberOfGenesToProcess; i++)
			{
				int randomGeneIndex = Util.getRandomIntValueInRange(0, parent1.getGenes().size());
				geneIndicesToProcess.add(randomGeneIndex);
			}
			
			System.out.println("Parent1: " + parent1);
			System.out.println("Parent2: " + parent2);
			System.out.println("Gene indices to process = " + geneIndicesToProcess);
			List<Chromosome> children = new ArrayList<>();
			
			if(weightedAverageMode)
			{
				List<Chromosome> childrenSet1 = createChildUniformCrossoverWeightedAverage(parent1, parent2, geneIndicesToProcess);
				List<Chromosome> childrenSet2 = createChildUniformCrossoverWeightedAverage(parent2, parent1, geneIndicesToProcess);
				children.addAll(childrenSet1);
				children.addAll(childrenSet2);
			}
			else
			{
				List<Chromosome> childrenSet = createChildUniformCrossoverSwap(parent1, parent2, geneIndicesToProcess);
				children.addAll(childrenSet);
			}
			
			int count = 1;
			for(Chromosome child : children)
			{
				child.addOrigin(stepName.crossover.toString() + "-" + crossoverMode + "_" + currentGenerationNumber);
				FitnessFunction.computeFitnessScore(child, stepName.crossover.toString());
				nextGeneration.add(child);
				System.out.println("Child" + count + ": " + child);
				count++;
			}
		}
	}
	
	private List<Chromosome> createChildUniformCrossoverWeightedAverage(Chromosome parent1, Chromosome parent2, Set<Integer> geneIndicesToProcess)
	{
		List<Chromosome> children = new ArrayList<Chromosome>();
		
		Chromosome child1 = parent1.copy();
		Chromosome child2 = parent1.copy();
		for(int i : geneIndicesToProcess)
		{
			Gene gene1 = child1.getGene(i);
			Gene gene2 = child2.getGene(i);
			
			double alpha = Util.getRandomDoubleValueInRange(0.0, 1.0);
			int value1 = parent1.getGene(i).getChangeValue();
			int value2 = parent2.getGene(i).getChangeValue();
			int newChangeValue1 = (int) Math.round(Util.getWeightedAverage(value1, value2, alpha));
			int newChangeValue2 = (int) Math.round(Util.getWeightedAverage(value2, value1, alpha));
			System.out.println("child1: alpha = " + alpha + " -> old gene = " + gene1 + " -> new val = " + newChangeValue1);
			System.out.println("child2: alpha = " + alpha + " -> old gene = " + gene2 + " -> new val = " + newChangeValue2);
			gene1.setChangeValue(newChangeValue1);
			gene2.setChangeValue(newChangeValue2);
		}
		children.add(child1);
		children.add(child2);
		return children;
	}
	
	private List<Chromosome> createChildUniformCrossoverSwap(Chromosome parent1, Chromosome parent2, Set<Integer> geneIndicesToProcess)
	{
		List<Chromosome> children = new ArrayList<Chromosome>();
		
		Chromosome child1 = parent1.copy();
		Chromosome child2 = parent2.copy();
		for(int i : geneIndicesToProcess)
		{
			Gene gene1 = child1.getGene(i).copy();
			Gene gene2 = child2.getGene(i).copy();
			
			child1.replaceGene(gene1, gene2.copy());
			child2.replaceGene(gene2, gene1.copy());
		}
		children.add(child1);
		children.add(child2);
		return children;
	}
	
	private Chromosome selectParent()
	{
		// roulette wheel selection
		double populationSum = 0;
		List<Chromosome> tempPopulation = new ArrayList<Chromosome>(currentPopulation);
		
		Collections.shuffle(tempPopulation);
		
		for(Chromosome c : tempPopulation)
		{
			populationSum = populationSum + c.getFitnessScoreObj().getFitnessScore();
		}
		
		double r = Math.random();
		double sum = 0.0;
		for(Chromosome c : tempPopulation)
		{
			double probability = c.getFitnessScoreObj().getFitnessScore() / populationSum;
			sum = sum + (1 - probability); // (1 - probability) -> as the fitness function is minimizing 
			if(r < sum)
			{
				return c;
			}
		}
		return currentPopulation.get(0);
	}
	
	public void mutation(double bellwidth)
	{
		int numberOfChromosomesToMutate = (int) Math.round(currentPopulation.size() * Constants.MUTATION_RATE_PHASE1);
		System.out.println("Number of chromosomes to mutate = " + numberOfChromosomesToMutate);
		for (int i = 0; i < numberOfChromosomesToMutate; i++)
		{
			int randomChromosomeIndex = Util.getRandomIntValueInRange(0, currentPopulation.size());
			Chromosome mutatedChromosome = currentPopulation.get(randomChromosomeIndex).copy();
			System.out.println("Chromosome to mutate (index = " + randomChromosomeIndex + "): " + mutatedChromosome);
			
			double mutationProbability = 1.0 / (double)mutatedChromosome.getGenes().size();
			
			for (int j = 0; j < mutatedChromosome.getGenes().size(); j++)
			{
				// mutate every gene with probability of 1 / (size of chromosome)
				if(Util.getRandomDoubleValueInRange(0, 1) < mutationProbability){
				
					Gene geneToMutate = mutatedChromosome.getGene(j);
					Gene mutatedGene = geneToMutate.copy();
					
					// gaussian mutation
					int newValue = getGaussianValue(geneToMutate, bellwidth);
					
					mutatedGene.setChangeValue(newValue);
					mutatedChromosome.replaceGene(geneToMutate, mutatedGene);
					System.out.println("guassian value = " + newValue + " -> oldGene = " + geneToMutate + " -> newGene = " + mutatedGene);
				}
			}
			// compute new fitness score
			mutatedChromosome.addOrigin(stepName.mutation.toString() + "-" + bellwidth + "_" + currentGenerationNumber);
			FitnessFunction.computeFitnessScore(mutatedChromosome, stepName.mutation.toString());
			System.out.println("Mutated chromosome: " + mutatedChromosome);
			
			nextGeneration.add(mutatedChromosome);
		}
		
	}
	
	private int getGaussianValue(Gene gene, double bellWidth)
	{
		double geneAvgOrgValue = MainIterator.getClusterCSSPropAvgValue(gene.getClusterId(), gene.getCssProperty());
		
		double pixelsChange = gene.getChangeValue() * Constants.CHANGE_FACTORS.get(gene.getCssProperty());
		
		double mean = pixelsChange + geneAvgOrgValue;
		
		
		double min = mean - (mean * Constants.MUTATION_MIN_MAX_FACTOR); 
		double max = mean + (mean * Constants.MUTATION_MIN_MAX_FACTOR);
		
		
		double stddev = (max - min) / bellWidth;
		Random r = new Random();
		double x1 = r.nextDouble();
		double x2 = r.nextDouble();

		if (x1 == 0)
			x1 = 1;
		if (x2 == 0)
			x2 = 1;

		double y1 = Math.sqrt(-2.0 * Math.log(x1)) * Math.cos(2.0 * Math.PI * x2);
		double val = y1 * stddev + mean;
		
		double gaussianValue = val;
		
		if (val >= max)
			gaussianValue = max;
		if (val <= min)
			gaussianValue = min;
		
		int newChangeValue = (int) Math.round(gaussianValue - geneAvgOrgValue);;
		
		
		// if font-size and the new mutated value is increasing it, then try again
		if(gene.getCssProperty().equalsIgnoreCase("font-size") && newChangeValue > 0)
			return getGaussianValue(gene, bellWidth);
		
		
		return newChangeValue;
	}

	public void select()
	{
		// sort population by fitness score
		sortPopulationByFitnessScore(nextGeneration);
		
		// add only top "POPULATION_SIZE" chromosomes to the new current population
		currentPopulation = new ArrayList<Chromosome>();
		for(int i = 0; i < Constants.POPULATION_SIZE_APPROACH1_PHASE1; i++)
		{
			currentPopulation.add(nextGeneration.get(i));
		}
	}
	
	public void sortPopulationByFitnessScore(List<Chromosome> population)
	{
		Collections.sort(population);
	}
	
	public boolean isTerminate(int generationCount, int saturationCount)
	{
		if(Constants.IS_RANDOM_SEARCH)
			return true;
		if(generationCount > Constants.MAX_GENERATIONS_APPROACH1_PHASE1)
			System.out.println("Terminating because max generations reached");
		else if(saturationCount >= Constants.SATURATION_POINT_APPROACH1_PHASE1)
			System.out.println("Terminating because saturation point reached");
		
		return generationCount > Constants.MAX_GENERATIONS_APPROACH1_PHASE1 || 
		saturationCount >= Constants.SATURATION_POINT_APPROACH1_PHASE1;
	}
	

	
	public void printPopulation(List<Chromosome> population)
	{
		int count = 1;
		System.out.println("(Size = " + population.size() + ")");
		for(Chromosome c : population)
		{
			System.out.println(count + ". " + c);
			count++;
		}
	}



	
	public void performAVMSearch(boolean isIsolation)
	{
		String isolated = isIsolation ? "isolated" : "notIsolated";
		List<Chromosome> avmChromosomes = new ArrayList<>();
		int numberOfAVMSearchesToPerform = (int) Math.round(nextGeneration.size() * Constants.AVM_RATE_PHASE1);
		System.out.println("Number of AVM searches to be performed = " + numberOfAVMSearchesToPerform);

		for(int n = 0; n < numberOfAVMSearchesToPerform; n++)
		{
            Chromosome oldChromosome = nextGeneration.get(n);
            Chromosome newChromosome = oldChromosome.copy();
            newChromosome.addOrigin("avm-" + isolated + "_" +currentGenerationNumber);
			System.out.println("Performing AVM for chromosome " + (n+1));
			for(int i = 0; i < newChromosome.getGenes().size(); i++)
			{
				AVMSearch avm = new AVMSearch(newChromosome, i);
				avm.runAVM();
				//compare the genes inside the chromosome one by one
				if(!newChromosome.equals(oldChromosome))
				{
					avmChromosomes.add(newChromosome);
				}
				
				if(isIsolation)
				{
					newChromosome = oldChromosome.copy();
		            newChromosome.addOrigin("avm-" + isolated + "_" +currentGenerationNumber);
                }
				else{
                    newChromosome = newChromosome.copy();
                }
			}
		}
		nextGeneration.addAll(avmChromosomes);
	}
}
