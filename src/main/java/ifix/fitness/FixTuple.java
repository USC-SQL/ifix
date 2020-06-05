package ifix.fitness;

import java.util.ArrayList;

import ifix.approach1.Gene;
import util.Util;

public class FixTuple
{
	private String xpath;
	private String cssProperty;
	private String originalValue;
	private String newValue;
	private int changeValue;
	private String clusterId;

	//this flag is used to distinguish between fixes applied to the elements in the cluster (or fixes applied to the
	//parents of these elements)
	private boolean isParentFix;

	
	public FixTuple(String xpath, String cssProperty, int changeValue, String clusterId)
	{
		super();
		this.xpath = xpath;
		this.cssProperty = cssProperty;
		this.changeValue = changeValue;
		this.clusterId = clusterId;
		this.isParentFix = false;
	}

	public FixTuple(String xpath, String cssProperty, String originalValue, String newValue, boolean isParentFix)
	{
		super();
		this.xpath = xpath;
		this.cssProperty = cssProperty;
		this.originalValue = originalValue;
		this.newValue = newValue;
		this.isParentFix = isParentFix;
		updateChangeValue();
	}
	
	public String getXpath()
	{
		return xpath;
	}
	public void setXpath(String xpath)
	{
		this.xpath = xpath;
	}
	public String getCssProperty()
	{
		return cssProperty;
	}
	public void setCssProperty(String cssProperty)
	{
		this.cssProperty = cssProperty;
	}
	public String getOriginalValue()
	{
		if(originalValue == null){
			originalValue = Util.getOriginalValue(this.getXpath(),this.getCssProperty());
		}
		return originalValue;
	}
	public void setOriginalValue(String originalValue)
	{
		this.originalValue = originalValue;
	}
	public String getNewValue()
	{
		return newValue;
	}
	public void setNewValue(String newValue)
	{
		this.newValue = newValue;
		updateChangeValue();
	}
	private void updateChangeValue() {
		this.changeValue = Util.computeChangeValue(originalValue, newValue);
	}

	public int getchangeValue()
	{
		return changeValue;
	}

	public void setChangeValue(int changeValue)
	{
		this.changeValue = changeValue;
	}
	public String getClusterId()
	{
		return clusterId;
	}
	public void setClusterId(String clusterId)
	{
		this.clusterId = clusterId;
	}


	public boolean isParentFix() { return isParentFix; }

	@Override
	public int hashCode() {
		int result = xpath != null ? xpath.hashCode() : 0;
		result = 31 * result + (cssProperty != null ? cssProperty.hashCode() : 0);
		result = 31 * result + changeValue;
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FixTuple fixTuple = (FixTuple) o;

		if (changeValue != fixTuple.changeValue) return false;
		if (xpath != null ? !xpath.equals(fixTuple.xpath) : fixTuple.xpath != null) return false;
		return cssProperty != null ? cssProperty.equals(fixTuple.cssProperty) : fixTuple.cssProperty == null;
	}

	@Override
	public String toString()
	{
		String fixTupleStr = "<" + clusterId + ", " + xpath + ", " + cssProperty + ", " + changeValue + ">";
		if(isParentFix)
			fixTupleStr += "(parent fix)";
		return fixTupleStr;
	}
	
	public FixTuple copy()
	{
		FixTuple copiedFixTuple = new FixTuple(xpath, cssProperty, changeValue, clusterId);
		copiedFixTuple.xpath = new String(this.xpath);
		copiedFixTuple.cssProperty = new String(this.cssProperty);
		copiedFixTuple.originalValue = this.originalValue == null? null : new String(this.originalValue);
		copiedFixTuple.newValue = this.newValue == null ? null : new String(this.newValue);
		copiedFixTuple.changeValue = this.changeValue;
		copiedFixTuple.clusterId = this.clusterId == null ? null : new String(this.clusterId);
		copiedFixTuple.isParentFix = this.isParentFix;
		return copiedFixTuple;
	}
	
}
