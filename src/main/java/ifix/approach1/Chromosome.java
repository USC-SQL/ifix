package ifix.approach1;

import ifix.fitness.FitnessFunction;

import java.util.ArrayList;
import java.util.List;

public class Chromosome implements Comparable<Chromosome>
{
	private List<Gene> genes;
	private FitnessFunction fitnessScoreObj;
	private String originTrace;

	public Chromosome()
	{
		this.genes = new ArrayList<Gene>();
		this.originTrace = "";
	}
	
	public Chromosome(List<Gene> genes)
	{
		this.genes = genes;
	}

	public List<Gene> getGenes()
	{
		return genes;
	}

	public void setGenes(List<Gene> genes)
	{
		this.genes = genes;
	}

	public FitnessFunction getFitnessScoreObj()
	{
		return fitnessScoreObj;
	}

	public void setFitnessScoreObj(FitnessFunction fitnessScoreObj)
	{
		this.fitnessScoreObj = fitnessScoreObj;
	}
	
	public String getOriginTrace()
	{
		return originTrace;
	}

	public void addOrigin(String originTrace)
	{
		if(this.originTrace.isEmpty())
			this.originTrace = originTrace;
		else
			this.originTrace = this.originTrace + ", " + originTrace;
	}

	public void addGene(Gene gene)
	{
		genes.add(gene);
	}

	public Gene getGene(int index)
	{
		return genes.get(index);
	}
	
	public Gene getGene(String clusterId, String cssProperty)
	{
		for(Gene g : genes)
		{
			if(g.getClusterId().equalsIgnoreCase(clusterId) && g.getCssProperty().equalsIgnoreCase(cssProperty))
				return g;
		}
		return null;
	}
	
	public List<Gene> getGenes(String clusterId)
	{
		List<Gene> matchingGenes = new ArrayList<>();
		for(Gene g : genes)
		{
			if(g.getClusterId().equalsIgnoreCase(clusterId))
				matchingGenes.add(g);
		}
		if(matchingGenes.size() > 0)
			return matchingGenes;
		return null;
	}
	
	public Chromosome copy()
	{
		Chromosome c = new Chromosome();
		for(Gene g : this.genes)
		{
			c.addGene(g.copy());
		}
		c.fitnessScoreObj = this.fitnessScoreObj == null ? null : this.fitnessScoreObj.copy();
		c.originTrace = this.originTrace;
		return c;
	}
	
	public void replaceGene(Gene oldGene, Gene newGene)
	{
		int index = genes.indexOf(oldGene);
		genes.remove(index);
		genes.add(index, newGene);
	}
	
	@Override
	public int compareTo(Chromosome o)
	{
		if(this.fitnessScoreObj.getFitnessScore() < o.fitnessScoreObj.getFitnessScore())
	          return -1;
	    else if(this.fitnessScoreObj.getFitnessScore() > o.fitnessScoreObj.getFitnessScore())
	          return 1;
	    return 0;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((genes == null) ? 0 : genes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Chromosome other = (Chromosome) obj;
		if (genes == null)
		{
			if (other.genes != null)
				return false;
		}
		else if (!genes.equals(other.genes))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "Chromosome [genes=" + genes + ", fitnessScoreObj=" + fitnessScoreObj + ", originTrace={" + originTrace + "}]";
	}
}
