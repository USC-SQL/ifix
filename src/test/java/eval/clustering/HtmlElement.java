package eval.clustering;

public class HtmlElement
{
	private String xpath;
	private String tagName;
	private String id;
	
	// location of top left hand corner	
	private int x;
	private int y;
	
	// rectangular dimensions
	private int width;
	private int height;

	// R-tree rectangle id
	private int rectId;
	
	public String getXpath()
	{
		return xpath;
	}
	public void setXpath(String xpath)
	{
		this.xpath = xpath;
	}
	public String getTagName()
	{
		return tagName;
	}
	public void setTagName(String tagName)
	{
		this.tagName = tagName;
	}
	public String getId()
	{
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}
	public int getX()
	{
		return x;
	}
	public void setX(int x)
	{
		this.x = x;
	}
	public int getY()
	{
		return y;
	}
	public void setY(int y)
	{
		this.y = y;
	}
	public int getWidth()
	{
		return width;
	}
	public void setWidth(int width)
	{
		this.width = width;
	}
	public int getHeight()
	{
		return height;
	}
	public void setHeight(int height)
	{
		this.height = height;
	}
	public int getRectId()
	{
		return rectId;
	}
	public void setRectId(int rectId)
	{
		this.rectId = rectId;
	}

	@Override
	public String toString()
	{
		return "HtmlElement [xpath=" + xpath + ", tagName=" + tagName + ", id=" + id + ", x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", rectId=" + rectId + "]";
	}
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((xpath == null) ? 0 : xpath.hashCode());
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
		HtmlElement other = (HtmlElement) obj;
		if (xpath == null)
		{
			if (other.xpath != null)
				return false;
		}
		else if (!xpath.equals(other.xpath))
			return false;
		return true;
	}
}
