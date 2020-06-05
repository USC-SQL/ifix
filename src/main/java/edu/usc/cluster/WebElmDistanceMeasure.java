package edu.usc.cluster;

import org.apache.commons.math3.ml.distance.DistanceMeasure;

import java.util.*;

public class WebElmDistanceMeasure implements DistanceMeasure {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8160509456210272565L;
	public static final double UNDEFINED_DISTANCE = -99;
	
	List<ElementWrapper> webElements;
	private XPathDistanceMeasurer xPathDistanceMeasurer;

	private static HashMap<ElementWrapper, Integer> webElemIdxs = new HashMap<ElementWrapper, Integer>(); 

	public WebElmDistanceMeasure(List<ElementWrapper> webElements) {
		this.webElements = webElements;
		String[] xpaths = new String[webElements.size()];
		for (int i = 0; i < webElements.size(); i++) {
			webElemIdxs.put(webElements.get(i), i);
			xpaths[i] = webElements.get(i).getDomNode().getxPath();
		}
		HashSet<String> alphabet = XPathDistanceMeasurer.generateAlphabet(xpaths);
		xPathDistanceMeasurer = new XPathDistanceMeasurer(alphabet);
	}
	
	
	//Here, we are not computing the distance between two vectors.. instead, we
	//are using the just using the first index of the vectors a[] and b[] as
	//a reference of the elements we want to compare, the actual comparison
	//is done inside the ElementWrapper.distance method!
	@Override
	public double compute(double[] a, double[] b) {
		ElementWrapper elmA = webElements.get((int)a[0]);
		ElementWrapper elmB = webElements.get((int)b[0]);
		double distance = elmA.distance(elmB,xPathDistanceMeasurer);
		//System.out.print("distance between " + elmA + " and " + elmB + ": ");
		//System.out.println(distance);
		return distance;
	}
	
	public static double[] getElementIdx(ElementWrapper target) {
		double[] idx = new double[1];
		idx[0] = webElemIdxs.get(target);
		return idx;
	}

	public static double computeXpathDistance(String xpath1, String xpath2, XPathDistanceMeasurer xPathDistanceMeasurer, boolean normalize){
		//LevenshteinDistance between the two XPaths..
		double xPathDistance = (double) xPathDistanceMeasurer.computeDistance(xpath1, xpath2);
		int xpath1Size = xpath1.split("\\/").length;
		int xpath2Size = xpath2.split("\\/").length;
		//normalized by dividing by the max length..
		if(normalize)
			xPathDistance = xPathDistance / Double.max(xpath1Size, xpath2Size) ;
		return xPathDistance;
	}

	public static double computeElementClassDistance(String className1, String className2){
		 double distance = 0;
		 if (className1 == null) // undefined
			className1 = "";
		 if (className2 == null) // undefined
			 className2 = "";
		 else{
			 HashSet<String> elm1Classes = new HashSet<String>(Arrays.asList(className1.split("\\s")));
			 elm1Classes.remove("");
			 HashSet<String> elm2Classes = new HashSet<String>(Arrays.asList(className2.split("\\s")));
			 elm2Classes.remove("");
			 
			 if (elm1Classes.size() == 0 && elm2Classes.size() == 0)
				 distance = WebElmDistanceMeasure.UNDEFINED_DISTANCE;
			 else{
				 double similarity = computeSetSimilarity(elm1Classes, elm2Classes);
				 distance = 1 - similarity;
			 }
		 }
		 return distance;
	}


	public static double computeElementCSSDistance(Map<String, String> cssMapA, Map<String, String> cssMapB)
	{
		//System.out.println("File = " + ReadInput.getTestFilepath() + ". Parsing CSS for " + xpathA + " and " + xpathB);
		double distance = 0;
		

		
		Set<String> cssSetA = new HashSet<>();
		for(String key : cssMapA.keySet())
		{
			cssSetA.add(key + "=" + cssMapA.get(key));
		}
		
		Set<String> cssSetB = new HashSet<>();
		for(String key : cssMapB.keySet())
		{
			cssSetB.add(key + "=" + cssMapB.get(key));
		}
		//System.out.println(xpathA + " CSS A:" + cssSetA);
		//System.out.println(xpathB + " CSS B:" + cssSetB);
		if (cssSetA.size() == 0 && cssSetB.size() == 0)
			 distance = WebElmDistanceMeasure.UNDEFINED_DISTANCE;
		else{
			double similarity = computeSetSimilarity(cssSetA, cssSetB);
			distance = 1 - similarity;
		}
		
		return distance;
	}
	
	private static double computeSetSimilarity(Set<String> setA, Set<String> setB){
		 double setASize = setA.size();
		 double setBSize = setB.size();
		 double similarity = 0;
		 setA.retainAll(setB);
		 double intersectionSize = setA.size();
		 similarity = (intersectionSize) / (setASize + setBSize - intersectionSize);

		 return similarity;
	}
}
