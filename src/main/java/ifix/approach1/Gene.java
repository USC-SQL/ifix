package ifix.approach1;

import ifix.fitness.FixTuple;
import ifix.fitness.FixTupleGenerator;
import util.Util;

import java.util.ArrayList;
import java.util.List;

public class Gene implements Comparable<Gene>
{
	private String clusterId;
	private String cssProperty;
	private int changeValue;

	private List<FixTuple> fixTuples;

	public Gene(){

	}

	public Gene(String clusterId, String cssProperty, int changeValue)
	{
		this.clusterId = clusterId;
		this.cssProperty = cssProperty;
		//set Change Value and fixTuples
		setChangeValue(changeValue);
	}
	public String getCssProperty()
	{
		return cssProperty;
	}

	public int getChangeValue()
	{
		return changeValue;
	}

	public ArrayList<Integer> getCumulativeChangeValue()
	{
		int numOfElements = MainIterator.getPageClustersMap().get(clusterId).size();
		ArrayList<Integer> changeValues = new ArrayList<>(numOfElements);
		for (int i = 0; i < numOfElements; i++) {
			changeValues.add(this.changeValue);
		}
		return changeValues;
	}

	public void setChangeValue(int changeValue)
	{

		this.changeValue = changeValue;
		this.fixTuples = FixTupleGenerator.generateGenesFixTuple(this,false);

	}


	public String getClusterId()
	{
		return clusterId;
	}

	public List<FixTuple> getFixTuples() {
		return fixTuples;
	}
	public Gene copy()
	{
		Gene copiedGene = new Gene();
		copiedGene.clusterId = this.clusterId;
		copiedGene.cssProperty = this.cssProperty;
		copiedGene.changeValue = this.changeValue;
		copiedGene.fixTuples = new ArrayList<>();
		for (FixTuple fixTuple : this.fixTuples) {
			copiedGene.fixTuples.add(fixTuple.copy());
		}
		return copiedGene;
	}
	
	@Override
	public int compareTo(Gene o)
	{
		return this.clusterId.compareToIgnoreCase(o.clusterId) &
				this.cssProperty.compareToIgnoreCase(o.cssProperty);
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + changeValue;
		result = prime * result + ((clusterId == null) ? 0 : clusterId.hashCode());
		result = prime * result + ((cssProperty == null) ? 0 : cssProperty.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Gene other = (Gene) obj;
		if (changeValue != other.changeValue)
			return false;
		if (clusterId == null) {
			if (other.clusterId != null)
				return false;
		} else if (!clusterId.equals(other.clusterId))
			return false;
		if (cssProperty == null) {
			if (other.cssProperty != null)
				return false;
		} else if (!cssProperty.equals(other.cssProperty))
			return false;
		return true;
	}
	@Override
	public String toString()
	{
		return "[" + clusterId + ", " + cssProperty + ", " + changeValue + "]";
	}
}
