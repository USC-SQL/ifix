package ifix.approach1;

public class GraphPlotting
{
	// phase 1 plotting
	private String clusterId;
	private String stepName;	// e.g., AVM, crossover, mutation, etc.
	private int iterationNumber;
	private int generationNumber;
	private double computedFitnessScore;
	private double computedInconsistenciesObjectiveScore;
	private double computedAestheticObjectiveScore;
	
	public GraphPlotting(String clusterId, String stepName,int iterationNumber, int generationNumber, double computedFitnessScore, 
			double computedInconsistenciesObjectiveScore, double computedAestheticObjectiveScore)
	{
		super();
		this.clusterId = clusterId;
		this.stepName = stepName;
		this.iterationNumber = iterationNumber;
		this.generationNumber = generationNumber;
		this.computedFitnessScore = computedFitnessScore;
		this.computedInconsistenciesObjectiveScore = computedInconsistenciesObjectiveScore;
		this.computedAestheticObjectiveScore = computedAestheticObjectiveScore;
		
	}
	public String getClusterId()
	{
		return clusterId;
	}
	public String getStepName()
	{
		return stepName;
	}
	public int getGenerationNumber()
	{
		return generationNumber;
	}
	public double getComputedFitnessScore()
	{
		return computedFitnessScore;
	}
	public int getIterationNumber()
	{
		return iterationNumber;
	}
	public double getComputedInconsistenciesObjectiveScore()
	{
		return computedInconsistenciesObjectiveScore;
	}
	public double getComputedAestheticObjectiveScore()
	{
		return computedAestheticObjectiveScore;
	}
	
	@Override
	public String toString()
	{
		return clusterId + "," + stepName + "," + iterationNumber + "," + generationNumber + "," + computedFitnessScore +
				"," + computedInconsistenciesObjectiveScore + "," + computedAestheticObjectiveScore ;
	}
	
	public static String getHeader() {
		return "fitnessCallNoOverall,fitnessCallNoByClusterGroup,dependentClusterGroup,searchStepName,iterationNumber,generationNumber,"
				+ "computedFitnessScore,computedInconsistenciesObjectiveScore,computedAestheticObjectiveScore";
	}
}