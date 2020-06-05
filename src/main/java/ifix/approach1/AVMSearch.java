package ifix.approach1;

import ifix.fitness.FitnessFunction;
import ifix.fitness.FixTuple;
import ifix.fitness.FixTupleGenerator;
import util.Constants;
import util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AVMSearch
{
	private Chromosome chromosome;
	private int geneIndexToProcess;
	private Gene gene; 
	private FitnessFunction currentFitnessScoreObj;


	public AVMSearch(Chromosome chromosome, int geneIndexToProcess)
	{
		this.chromosome = chromosome;
		this.geneIndexToProcess = geneIndexToProcess;
		gene = this.chromosome.getGene(geneIndexToProcess).copy();
		currentFitnessScoreObj = this.chromosome.getFitnessScoreObj().copy();
	}

	public void runAVM()
	{
		boolean exploratoryImprovement;
		
			do
			{
				exploratoryImprovement = false;
				for (int direction : Constants.EXPLORATORY_MOVES_ARR)
				{
					if (exploratoryMove(direction))
					{
						exploratoryImprovement = true;

						// do pattern moves in the direction of improvement
						patternMove(direction);

						// break this cycle of exploratory moves to reestablish a new direction
						break;
					}
				}
			} while (exploratoryImprovement);
		
		System.out.println();
	}

	private boolean exploratoryMove(int direction)
	{
		Gene oldGene = gene.copy();
		int newValue = oldGene.getChangeValue() + direction;

		// store newValue in the chromosome
		gene.setChangeValue(newValue);

		double currentFitnessScore = currentFitnessScoreObj.getFitnessScore();
        Chromosome newChromosome = getNewChromosome();
        FitnessFunction newFitnessScoreObj = FitnessFunction.computeFitnessScore(newChromosome, Phase1.stepName.avm.toString());
        double newFitnessScore = newFitnessScoreObj.getFitnessScore();

		boolean improvement = (newFitnessScore < currentFitnessScore);
		System.out.println("In exploratory move with (" + gene.getClusterId() + ", " + gene.getCssProperty() + ", " + newValue + ", " + direction + "), "
				+ "new FS = " + newFitnessScore + ", current FS = " + currentFitnessScore + " = " + (improvement ? "IMPROVEMENT" : "NO IMPROVEMENT"));

		// reset the value
		if (!improvement)
		{
			//System.out.println("exploratory move resetting " + gene.getCssProperty() + " value = " + oldValue + " and fitness score = " + currentFitnessScore);
			gene = oldGene;
			chromosome.replaceGene(chromosome.getGene(geneIndexToProcess), oldGene);
		}
		else
		{
			currentFitnessScoreObj = newFitnessScoreObj.copy();
			chromosome.replaceGene(chromosome.getGene(geneIndexToProcess), gene);
			chromosome.setFitnessScoreObj(currentFitnessScoreObj.copy());
		}
		return improvement; // true == improvement
	}

	private void patternMove(int direction)
	{
		boolean improvement = true;
		
		int jumpSize = Constants.PATTERN_BASE;
		
		// if the direction is negative make the jumps in negative side
		if(direction < 0)
			jumpSize = -jumpSize;
		
		int jumpsCounter = 1;

		while (improvement)
		{
			Gene oldGene = gene.copy();
			int newValue = oldGene.getChangeValue() + jumpSize;

			// store newValue in the chromosome
			gene.setChangeValue(newValue);

			double currentFitnessScore = currentFitnessScoreObj.getFitnessScore();

            Chromosome newChromosome = getNewChromosome();
			FitnessFunction newFitnessScoreObj = FitnessFunction.computeFitnessScore(newChromosome, Phase1.stepName.avm.toString());

            double newFitnessScore = newFitnessScoreObj.getFitnessScore();
			improvement = (newFitnessScore < currentFitnessScore);
			System.out.println("In pattern move step (" + gene.getClusterId() + ", " + gene.getCssProperty() + ", " + newValue + ", " + direction + "), "
					+ "new FS = " + newFitnessScore + ", current FS = " + currentFitnessScore + " = " + (improvement ? "IMPROVEMENT" : "NO IMPROVEMENT"));

			if (!improvement)
			{
				// reset with the last stored value
				System.out.println("pattern move resetting " + gene.getCssProperty() + " value = " + oldGene.getChangeValue() + " and fitness score = " + currentFitnessScore);
				gene = oldGene;
				chromosome.replaceGene(chromosome.getGene(geneIndexToProcess), oldGene);
			}
			else
			{
				
				jumpSize = jumpSize * Constants.PATTERN_BASE;				
				currentFitnessScoreObj = newFitnessScoreObj.copy();
				chromosome.replaceGene(chromosome.getGene(geneIndexToProcess), gene);
				chromosome.setFitnessScoreObj(currentFitnessScoreObj.copy());
			}
			jumpsCounter++;
		}
		System.out.println("total pattern move steps = " + jumpsCounter);
	}
	

	private Chromosome getNewChromosome()
	{
		Chromosome newChromosome = chromosome.copy();
		newChromosome.replaceGene(chromosome.getGene(geneIndexToProcess), gene);
	    return newChromosome;
	}
}
