package ifix.fitness;

import ifix.approach1.*;
import ifix.input.ReadInput;
import util.Constants;
import util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.usc.main.Main;

public class FitnessFunction
{
	private double fitnessScore;
	private String fitnessScoreBreakdown;
	private static int phase1FitnessFunctionCalls;
	private static int phase2FitnessFunctionCalls;
	private static int phase1CachedFitnessFunctionCalls;
	private static int phase2CachedFitnessFunctionCalls;
	private static int parentFitnessFunctionCalls;
	private static double totalFitnessFunctionTimeInSec;
	private List<FixTuple> fixTuples;
	private List<String> potentiallyFaultyElements;
	private List<String[]> potentiallyFaultyElementPairs;
	private static List<Double[]> objectives;
	private int inconsistenciesObjective;
	private double aestheticObjective;
	private int numberOfVisibleElements;
	public static Map<Chromosome, FitnessFunction> fitnessScoreCache  = new HashMap<Chromosome, FitnessFunction>();;



	public double getFitnessScore()
	{
		return fitnessScore;
	}
	public String getFitnessScoreBreakdown()
	{
		return fitnessScoreBreakdown;
	}
	public static int getPhase1FitnessFunctionCalls()
	{
		return phase1FitnessFunctionCalls;
	}
	public static int getPhase2FitnessFunctionCalls()
	{
		return phase2FitnessFunctionCalls;
	}
	public static int getPhase1CachedFitnessFunctionCalls()
	{
		return phase1CachedFitnessFunctionCalls;
	}
	public static int getPhase2CachedFitnessFunctionCalls()
	{
		return phase2CachedFitnessFunctionCalls;
	}
	public static double getTotalFitnessFunctionTimeInSec()
	{
		return totalFitnessFunctionTimeInSec;
	}

	public List<String> getPotentiallyFaultyElements()
	{
		return potentiallyFaultyElements;
	}
	public List<String[]> getPotentiallyFaultyElementPairs()
	{
		return potentiallyFaultyElementPairs;
	}
	public static List<Double[]> getObjectives()
	{
		return objectives;
	}
	public int getInconsistenciesObjective()
	{
		return inconsistenciesObjective;
	}
	public double getAestheticObjective()
	{
		return aestheticObjective;
	}
	public static void setObjectives(List<Double[]> objectives)
	{
		FitnessFunction.objectives = objectives;
	}
	public void setFitnessScore(double fitnessScore) {
		this.fitnessScore = fitnessScore;
	}
	public void setFitnessScoreBreakdown(String fitnessScoreBreakdown) {
		this.fitnessScoreBreakdown = fitnessScoreBreakdown;
	}
	public static void setPhase1FitnessFunctionCalls(int phase1FitnessFunctionCalls) {
		FitnessFunction.phase1FitnessFunctionCalls = phase1FitnessFunctionCalls;
	}
	public static void setPhase2FitnessFunctionCalls(int phase2FitnessFunctionCalls) {
		FitnessFunction.phase2FitnessFunctionCalls = phase2FitnessFunctionCalls;
	}
	public static void setPhase1CachedFitnessFunctionCalls(int phase1CachedFitnessFunctionCalls) {
		FitnessFunction.phase1CachedFitnessFunctionCalls = phase1CachedFitnessFunctionCalls;
	}
	public static void setPhase2CachedFitnessFunctionCalls(int phase2CachedFitnessFunctionCalls) {
		FitnessFunction.phase2CachedFitnessFunctionCalls = phase2CachedFitnessFunctionCalls;
	}
	public static int getParentFitnessFunctionCalls()
	{
		return parentFitnessFunctionCalls;
	}
	public static void setParentFitnessFunctionCalls(int parentFitnessFunctionCalls)
	{
		FitnessFunction.parentFitnessFunctionCalls = parentFitnessFunctionCalls;
	}
	public static void setTotalFitnessFunctionTimeInSec(double totalFitnessFunctionTimeInSec) {
		FitnessFunction.totalFitnessFunctionTimeInSec = totalFitnessFunctionTimeInSec;
	}
	public void setPotentiallyFaultyElements(List<String> potentiallyFaultyElements) {
		this.potentiallyFaultyElements = potentiallyFaultyElements;
	}
	public void setPotentiallyFaultyElementPairs(List<String[]> potentiallyFaultyElementPairs) {
		this.potentiallyFaultyElementPairs = potentiallyFaultyElementPairs;
	}
	public void setInconsistenciesObjective(int inconsistenciesObjective) {
		this.inconsistenciesObjective = inconsistenciesObjective;
	}
	public void setAestheticObjective(double aestheticObjective) {
		this.aestheticObjective = aestheticObjective;
	}
	public static void incrementPhase1FitnessCalls()
	{
		phase1FitnessFunctionCalls++;
	}
	public static void incrementPhase2FitnessCalls()
	{
		phase2FitnessFunctionCalls++;
	}
	public static void incrementCachedPhase1FitnessCalls()
	{
		phase1CachedFitnessFunctionCalls++;
	}
	public static void incrementCachedPhase2FitnessCalls()
	{
		phase2CachedFitnessFunctionCalls++;
	}
	public static void incrementParentFitnessCalls()
	{
		parentFitnessFunctionCalls++;
	}
	public static Map<Chromosome, FitnessFunction> getFitnessScoreCache() {
		return fitnessScoreCache;
	}
	public static void setFitnessScoreCache(Map<Chromosome, FitnessFunction> fitnessScoreCache) {
		FitnessFunction.fitnessScoreCache = fitnessScoreCache;
	}
	
	public FitnessFunction copy()
	{
		FitnessFunction ffCopied = new FitnessFunction();
		ffCopied.fitnessScore = this.fitnessScore;
		ffCopied.fitnessScoreBreakdown = this.fitnessScoreBreakdown;
		ffCopied.fixTuples = this.fixTuples;
		ffCopied.potentiallyFaultyElements = this.potentiallyFaultyElements;
		ffCopied.potentiallyFaultyElementPairs = this.potentiallyFaultyElementPairs;
		ffCopied.inconsistenciesObjective = this.inconsistenciesObjective;
		ffCopied.aestheticObjective = this.aestheticObjective;
		ffCopied.numberOfVisibleElements = this.numberOfVisibleElements;
		return ffCopied;
	}
	
	public void calculateFitnessScore(List<FixTuple> tuples)
	{
		
		this.fixTuples = tuples;

		long startTime = System.nanoTime();
		if(containsBadFixTuple(tuples)){
			inconsistenciesObjective = 1000000;
		}
		else{
			inconsistenciesObjective = executeGwali();
		}
		aestheticObjective = getDistortionInAestheticQuality(tuples);

		fitnessScoreBreakdown = " [objs = " + Util.round((double) inconsistenciesObjective) + ", " + Util.round(aestheticObjective) + "] ";
		
		//fitnessScore = calculateFitnessScoreAprioriWeights(inconsistenciesObjective, aestheticObjective);
		//fitnessScore = calculateFitnessScoreWeightBased(inconsistenciesObjective, aestheticObjective);
		fitnessScore = calculateFitnessScorePrioritized(inconsistenciesObjective, aestheticObjective);

		//add very large value, so this solution is not picked up!
		if(MainIterator.getOriginalFitness() != null && numberOfVisibleElements < MainIterator.getOriginalFitness().numberOfVisibleElements)
			fitnessScore += 100000 * (MainIterator.getOriginalFitness().numberOfVisibleElements - numberOfVisibleElements);
		
		long endTime = System.nanoTime();
		totalFitnessFunctionTimeInSec = totalFitnessFunctionTimeInSec + Util.convertNanosecondsToSeconds(endTime - startTime);
	}

	private boolean containsBadFixTuple(List<FixTuple> tuples) {
		for (FixTuple ft : tuples) {
			// if any fix tuple contains fontsize increase or width decrease or height decrease, then it's bad!
			if(ft.getCssProperty().equals("font-size") && ft.getchangeValue() > 0){
				return true;
			}
			else if(ft.getCssProperty().equals("width") && ft.getchangeValue() < 0){
				return true;
			}
			else if(ft.getCssProperty().equals("height") && ft.getchangeValue() < 0){
				return true;
			}
		}
		return false;
	}
	
	
	private int executeGwali()
	{	
		
		MainIterator.getGwali().ChangePUT(ReadInput.getTestDriver());
		MainIterator.getGwali().runGwali();
		potentiallyFaultyElements = MainIterator.getGwali().getPotentiallyFaultyElements();
		potentiallyFaultyElementPairs = MainIterator.getGwali().getPotentiallyFaultyElementPairs();
		numberOfVisibleElements = MainIterator.getGwali().getPutLG().getVertices().size();
		
		
		
		//int inconsistenciesObjective = MainIterator.getGwali().getNoOfIncosistancy();
		int inconsistenciesObjective = MainIterator.getGwali().getAmountOfInconsistancy();
		return inconsistenciesObjective;
	}
	
	private double getDistortionInAestheticQuality(List<FixTuple> fixTuples)
	{
		double TotalDistortionInAesthetic = 0.0;
		// calculate cumulative changes
		if(fixTuples != null)
		{
			for(FixTuple fixTuple : fixTuples)
			{
				double changeValue = fixTuple.getchangeValue() 
						* Constants.CHANGE_FACTORS.get(fixTuple.getCssProperty())
						* Constants.CHANGE_PENALTY.get(fixTuple.getCssProperty());
				
				double originalValue = Util.getNumbersFromString(fixTuple.getOriginalValue());
				double percOfChange = (Math.abs(changeValue) / originalValue) * 100;

				if(originalValue == 0)
					percOfChange = 100;	// heuristic, since percOfChange = infinity
				
				double distortionInAesthetic = Math.pow(percOfChange,Constants.POWER_FACTOR_AESTHETIC); //polynomial.. we can add weights for different properties
				TotalDistortionInAesthetic = TotalDistortionInAesthetic + distortionInAesthetic;
				
			}
		}
		return TotalDistortionInAesthetic;
	}
	
	private double calculateFitnessScoreAprioriWeights(int inconsistenciesObjective, double aestheticObjective)
	{
		// part1: structure matching
		double inconsistenciesObjectiveNormalized = 0.0;
		int originalinconsistenciesObjective = MainIterator.getOriginalFitness().getInconsistenciesObjective() == 0 ? inconsistenciesObjective : MainIterator.getOriginalFitness().getInconsistenciesObjective();
		if(inconsistenciesObjective > 0)
		{
			inconsistenciesObjectiveNormalized = (double)inconsistenciesObjective / (double)originalinconsistenciesObjective;
		}
		double fitnessScorePart1 = inconsistenciesObjectiveNormalized * Constants.FITNESS_FUNCTION_WEIGHT_STRUCTURE;
		fitnessScoreBreakdown = fitnessScoreBreakdown + "(" + inconsistenciesObjective + "/" + originalinconsistenciesObjective + 
				" = " + Util.round(inconsistenciesObjectiveNormalized) + " * " + Constants.FITNESS_FUNCTION_WEIGHT_STRUCTURE + " = " + Util.round(fitnessScorePart1) + ")"; 
	
		// part 2: distortion in aesthetic quality
		double aestheticQualityNormalized = aestheticObjective / (double)fixTuples.size();
		double fitnessScorePart2 = aestheticQualityNormalized * Constants.FITNESS_FUNCTION_WEIGHT_AESTHETIC;
		fitnessScoreBreakdown = fitnessScoreBreakdown + " + (" + Util.round(aestheticObjective) + "/" + fixTuples.size() +
				" = " + Util.round(aestheticQualityNormalized) + " * " + Constants.FITNESS_FUNCTION_WEIGHT_AESTHETIC + " = " + Util.round(fitnessScorePart2) + ")";

		double fitnessScore = fitnessScorePart1 + fitnessScorePart2;
		return fitnessScore;
	}
	
	// WBGA-MO proposed by "Hajela, P. and Lin, C.-Y., Genetic search strategies in multicriterion optimal design, Structural Optimization 4(2) (1992) 99-107" 
	private double calculateFitnessScoreWeightBased(int inconsistenciesObjective, double aestheticObjective)
	{
		// w1 = Constants.FITNESS_FUNCTION_WEIGHT_STRUCTURE and w2 = Constants.FITNESS_FUNCTION_WEIGHT_AESTHETIC
		// try 9 combinations of w1 and w2: {[0.1, 0.9], [0.2, 0.8], ..., [0.9, 0.1]} and select the best one. Therefore, multiple solutions can be simultaneously searched in a single run.
		// w1 + w2 = 1.0 and the two objective functions need to be normalized.
		
		double minFitnessScore = Double.MAX_VALUE;
		String minFitnessScoreBreakdown = "";
		double bestW1 = 0.0;
		double bestW2 = 0.0;
		double delta = 0.1;
		
		Constants.FITNESS_FUNCTION_WEIGHT_STRUCTURE = 0.0;
		Constants.FITNESS_FUNCTION_WEIGHT_AESTHETIC = 1.0;
		for(int i = 0; i < 9; i++)
		{
			Constants.FITNESS_FUNCTION_WEIGHT_STRUCTURE = Util.round(Constants.FITNESS_FUNCTION_WEIGHT_STRUCTURE + delta);
			Constants.FITNESS_FUNCTION_WEIGHT_AESTHETIC = Util.round(Constants.FITNESS_FUNCTION_WEIGHT_AESTHETIC - delta);
			
			double candidateFitnessScore = calculateFitnessScoreAprioriWeights(inconsistenciesObjective, aestheticObjective);
			System.out.println("WBGA-MO [w1=" + Constants.FITNESS_FUNCTION_WEIGHT_STRUCTURE + ", w2=" + Constants.FITNESS_FUNCTION_WEIGHT_AESTHETIC + "] = " + Util.round(candidateFitnessScore) + " (" + fitnessScoreBreakdown + ")");
			
			if(candidateFitnessScore < minFitnessScore)
			{
				minFitnessScore = candidateFitnessScore;
				minFitnessScoreBreakdown = fitnessScoreBreakdown;
				bestW1 = Constants.FITNESS_FUNCTION_WEIGHT_STRUCTURE;
				bestW2 = Constants.FITNESS_FUNCTION_WEIGHT_AESTHETIC;
			}
		}
		fitnessScoreBreakdown = minFitnessScoreBreakdown;
		System.out.println("Best Fitness Score: WBGA-MO [w1=" + bestW1 + ", w2=" + bestW2 + "] = " + Util.round(minFitnessScore) + " (" + fitnessScoreBreakdown + ")");
		
		return minFitnessScore;
	}
	
	// http://geekyisawesome.blogspot.com/2013/06/fitness-function-for-multi-objective.html 
	private double calculateFitnessScorePrioritized(int inconsistenciesObjective, double aestheticObjective)
	{
		// lexicographic ordering. Fitness structure (obj1) has higher priority than aesthetic distortion (obj2). obj1 is the whole number and obj2 is the fractional part. 
		double fitnessScore = inconsistenciesObjective + Util.sigmoid(aestheticObjective);
		fitnessScoreBreakdown = fitnessScoreBreakdown + "(" + inconsistenciesObjective + " + sigmoid(" + aestheticObjective + ") = " + inconsistenciesObjective + " + " + Util.sigmoid(aestheticObjective) + ")";
		return fitnessScore;
	}



    public static FitnessFunction computeFitnessScore(Chromosome chromosome, String stepName)
    {
        FitnessFunction fitnessFunction = null;

        if(fitnessScoreCache.containsKey(chromosome))
        {
            System.out.println("Fitness score returned from cache = " + FitnessFunction.fitnessScoreCache.get(chromosome));
            fitnessFunction = fitnessScoreCache.get(chromosome);
            FitnessFunction.incrementCachedPhase1FitnessCalls();
        }
        else
        {
            //apply new values to the test page
            List<FixTuple> tuples = new ArrayList<>();
            for(Gene g : chromosome.getGenes())
            {
                if(g.getChangeValue() != 0) {
                    tuples.addAll(g.getFixTuples());
                }
            }

            Util.resetPageToOriginalState();
            Util.applyChangesToPage(tuples, false);

            // get fitness score
            fitnessFunction = new FitnessFunction();
            fitnessFunction.calculateFitnessScore(tuples);
            fitnessScoreCache.put(chromosome, fitnessFunction);

            FitnessFunction.incrementPhase1FitnessCalls();
        }

        chromosome.setFitnessScoreObj(fitnessFunction.copy());

        //for graph plotting..
        FitnessFunction.getObjectives().add(new Double[]{Util.round((double) fitnessFunction.getInconsistenciesObjective()), Util.round(fitnessFunction.getAestheticObjective())});
        List<String> dependentClusters = new ArrayList<>();
        for (Gene gene:chromosome.getGenes()) {
            if(!dependentClusters.contains(gene.getClusterId())){
                dependentClusters.add(gene.getClusterId());
            }
        }
        Search.getFitnessScoreTrendGraphPhase1().add(new GraphPlotting("\"" + dependentClusters + "\"", stepName, MainIterator.getIterationCount(), Phase1.getCurrentGenerationNumber(), fitnessFunction.getFitnessScore(),
                fitnessFunction.getInconsistenciesObjective(), fitnessFunction.getAestheticObjective()));


        return fitnessFunction;
    }
	
	@Override
	public String toString()
	{
		return Util.round(fitnessScore) + " " + fitnessScoreBreakdown;
	}
}
