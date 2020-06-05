package ifix.approach1;

import ifix.fitness.FitnessFunction;
import ifix.fitness.FixTuple;
import util.Util;

import java.util.ArrayList;
import java.util.List;

public class Solution
{
	private List<FixTuple> fixTuples;
	private FitnessFunction fitnessScoreObject;

	public Solution()
	{
		this.fixTuples = new ArrayList<FixTuple>();
		if(MainIterator.getOriginalFitness() != null)
			this.fitnessScoreObject = MainIterator.getOriginalFitness().copy();
	}
	
	public void addFixTuple(FixTuple newFixTuple)
	{
		fixTuples.add(newFixTuple);
	}
	
	public List<FixTuple> getFixTuples()
	{
		return fixTuples;
	}

	public FitnessFunction getFitnessScoreObject()
	{
		if(this.fitnessScoreObject.getFitnessScore() == MainIterator.getOriginalFitness().getFitnessScore())
		{
			List<Integer> changeValues = new ArrayList<>();
			for(FixTuple ft : fixTuples)
			{
				if(!ft.isParentFix())
					changeValues.add(ft.getchangeValue());
			}
			Util.applyChangesToPage(fixTuples, false);
			
			// get fitness score
			FitnessFunction fitnessFunction = new FitnessFunction();
			fitnessFunction.calculateFitnessScore(fixTuples);
			fitnessScoreObject = fitnessFunction.copy();
		}
		
		return fitnessScoreObject.copy();
	}

	public void setFitnessScoreObject(FitnessFunction fitnessScoreObj)
	{
		this.fitnessScoreObject = fitnessScoreObj;
	}

	@Override
	public String toString()
	{
		return "Solution [fixTuples (size = " + fixTuples.size() + "): " + fixTuples + ", fitnessScore=" + fitnessScoreObject + "]";
	}
}
