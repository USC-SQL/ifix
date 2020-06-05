package edu.usc.cluster;

import edu.gatech.xpert.dom.DomNode;
import edu.usc.util.CSSParser;
import ifix.input.ReadInput;
import org.apache.commons.math3.ml.clustering.Clusterable;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ElementWrapper implements Clusterable{


	private DomNode domNode;

	//store the lists of parents with fixed CSS value for caching
	private Map<String,ArrayList<DomNode>> parentsWithFixedCSSValue;


	public ElementWrapper(DomNode node) {
		domNode = node;
		computeCSSValues();
		parentsWithFixedCSSValue = new HashMap<>();
	}


	public void computeCSSValues(){
		CSSParser cp = CSSParser.getInstance(ReadInput.getTestFilepath());
		Map<String, String> cssMap = new HashMap<>();
		try
		{
			cssMap = cp.getCSSPropertiesForElement(domNode.getxPath());
		}
		catch (XPathExpressionException | IOException e)
		{
			e.printStackTrace();
		}
		domNode.setExplicitCSS(cssMap);
	}


	public DomNode getDomNode() {
		return domNode;
	}
	
	
	/*
	//subtract by mean and divide by standards deviation.
	public void standardizePoints(double[] means, double[] stDevs){
		//Scaling by using mean and standard deviation 
		//(useful when min and max are unknown or when there are outliers): 
		//V'=(V-Mean)/StDev
		for (int i = 0; i < points.length; i++) {
			points[i] = (points[i] - means[i]) / stDevs[i];
		}
		isVectorNormalized = true;
	}
	
	public void ScalePoints(double[] mins, double[] maxs){
		//to transform V in [min, max] to V' in [0,1], apply V'=(V-Min)/(Max-Min)
		for (int i = 0; i < points.length; i++) {
			points[i] = (points[i] - mins[i]) / (maxs[i] - mins[i]);
		}
		
		isVectorNormalized = true;
	}

	public double[] getVectorPoint() {
		return points;
	}
	
	public boolean isVectorNormalized(){
		return isVectorNormalized;
	}
	
	public String getXpath(){
		return domNode.getxPath();
	}


	@Override
	public String toString() {
		return  "[" + xpath
				+ " - " + Arrays.toString(points) + "]" ;
	}
	*/
	// decide the weight Waka.. cross-validation..

	public double distance(ElementWrapper elmB, XPathDistanceMeasurer xPathDistanceMeasurer) {

		// feature names
		final String CLASS = "CLASS",
				XPATH = "XPATH", DOM_LEVEL = "DOM_LEVEL" , ALIGNMENT = "ALIGNMENT" , TAG = "TAG",
						HEIGHT = "HEIGHT" , WIDTH = "WIDTH", CSS_SIMILARITY = "CSS_SIMILARITY", PARENT_CHILD = "PARENT_CHILD";
		
		HashMap<String, Double> featureWeights = new HashMap<String, Double>();
		
		featureWeights.put(CLASS, 0.2);
		featureWeights.put(XPATH, 0.3);
		featureWeights.put(DOM_LEVEL, 0.3);
		featureWeights.put(ALIGNMENT, 0.1);
		featureWeights.put(TAG, 0.4);
		featureWeights.put(HEIGHT, 0.1);
		featureWeights.put(WIDTH, 0.1);
		featureWeights.put(CSS_SIMILARITY, 0.3);
		featureWeights.put(PARENT_CHILD, 0.0);

/*final double COLOR_WEIGHT = 0.0;
final double FONT_WEIGHT = 0.0;
final double CLASS_WEIGHT = 0.2;

final double XPATH_WEIGHT = 0.3;
final double XPATH_DEPTH_WEIGHT = 0.1;
final double ALIGNMENT_WEIGHT = 0.1;
final double TAG_WEIGHT = 0.4;
		
final double HEIGHT_WEIGHT = 0.1;
final double WIDTH_WEIGHT = 0.1;
		
final double CSS_SIMILARITY_WEIGHT = 0.3;
*/
		
		
		HashMap<String, Double> featureDistances = new HashMap<String, Double>();


		// right aligned, left aligned, bottom aligned, top aligned ?
		// same font ?
		// same font size?
		// same color ?
		// same xpath depth?
		// same tag ?
		// same class name ?
		// same background color?
		// same CSS Properties ?
		
		

		int leftAligned = (this.getDomNode().getCoords()[0] == elmB.getDomNode().getCoords()[0]) ? 1 : 0;
		int topAligned = (this.getDomNode().getCoords()[1] == elmB.getDomNode().getCoords()[1]) ? 1 : 0;
		int rightAligned = (this.getDomNode().getCoords()[2] == elmB.getDomNode().getCoords()[2]) ? 1 : 0;
		int botmAligned = (this.getDomNode().getCoords()[3] == elmB.getDomNode().getCoords()[3]) ? 1 : 0;
		int totalAlignment = leftAligned + rightAligned + topAligned + botmAligned;
		
		if(totalAlignment == 0)
			featureDistances.put(ALIGNMENT, 1.0);
		else if (totalAlignment == 1)
			featureDistances.put(ALIGNMENT, 0.5);
		else
			featureDistances.put(ALIGNMENT, 0.0);


		double widthA = this.getDomNode().getCoords()[2] - this.getDomNode().getCoords()[0];
		double widthB = elmB.getDomNode().getCoords()[2] - elmB.getDomNode().getCoords()[0];
		double widthDistance = (widthA == widthB) ? 0 : 1;
		featureDistances.put(WIDTH, widthDistance);
		
		double heightA = this.getDomNode().getCoords()[3] - this.getDomNode().getCoords()[1];
		double heightB = elmB.getDomNode().getCoords()[3] - elmB.getDomNode().getCoords()[1];
		double heightDistance = (heightA == heightB) ? 0 : 1;
		featureDistances.put(HEIGHT, heightDistance);
		

		// tag exactly match ?
		String tagA = this.getDomNode().getTagName();
		String tagB = elmB.getDomNode().getTagName();
		double tagDistance = (tagA != null && tagA.equalsIgnoreCase(tagB)) ? 0 : 1;
		featureDistances.put(TAG,tagDistance);
		
				
		// Jackard similarity in class names sets ?
		String classA = this.getDomNode().getClassName();
		String classB = elmB.getDomNode().getClassName();
		double classDistance = WebElmDistanceMeasure.computeElementClassDistance(classA,classB);
		featureDistances.put(CLASS, classDistance);

		String xpathA = this.getDomNode().getxPath();
		String xpathB = elmB.getDomNode().getxPath();
		double xPathDistance =
				WebElmDistanceMeasure.computeXpathDistance(xpathA,xpathB,xPathDistanceMeasurer,true);
		featureDistances.put(XPATH,xPathDistance);



		double CSSDistance = WebElmDistanceMeasure
				.computeElementCSSDistance(this.domNode.getExplicitCSS(), elmB.domNode.getExplicitCSS());

		featureDistances.put(CSS_SIMILARITY,CSSDistance);
		
		
		double parentChildDistances = (xpathA.contains(xpathB) || xpathB.contains(xpathA)) ? 1.0 : 0.0;
		featureDistances.put(PARENT_CHILD,parentChildDistances);
		
		double domLevel;
		int depthA = this.getDomNode().getLevel();
		int depthB = elmB.getDomNode().getLevel();
		domLevel = (double) Math.abs(depthA - depthB) ;/*/ Math.max(depthA , depthB)*/
		featureDistances.put(DOM_LEVEL,domLevel);
		
		
		
		

		double distance = computeDistances(featureDistances, featureWeights);
		
		
		/*System.out.println("-----------------------");
		System.out.println("distances between " + xpathA + " - " + xpathB + " =");
		for (String feature : featureDistances.keySet()) {
			System.out.println(feature + " : " + featureDistances.get(feature) + " * " + featureWeights.get(feature));
		}
		System.out.println("total = " + distance);*/

		return distance;
	}
	

	@Override
	public double[] getPoint() {
		return WebElmDistanceMeasure.getElementIdx(this);
	}
	
	
	private double computeDistances(HashMap<String, Double> featureDistances, HashMap<String, Double> featureWeights) {
		// adjust the weights for undefined distances
		for (String feature : featureDistances.keySet()) {
			double distance = featureDistances.get(feature);
			// if distance is undefined re-adjust the weights
			if (distance == WebElmDistanceMeasure.UNDEFINED_DISTANCE) {
				double oldWeight = featureWeights.get(feature);
				double distribution = oldWeight / (double) (featureWeights.size() - 1);
				// for each feature weight make it 0 for the feature with
				// undefined distance, and distribute the weight to the other
				// features
				for (String featureWeight : featureWeights.keySet()) {
					if (featureWeight.equals(feature))
						featureWeights.put(featureWeight, 0.0);
					else {
						double adjustedWeight = featureWeights.get(featureWeight) + distribution;
						featureWeights.put(featureWeight, adjustedWeight);
					}
				}

			}
		}
		
		
		double distance = 0;
		for (String feature : featureDistances.keySet()) {
			distance += featureDistances.get(feature) * featureWeights.get(feature);
		}
		return distance;
		
	}


	public ArrayList<DomNode> getParentsWithFixedCSS(String cssProperty){
		ArrayList<DomNode> parentsFixed = parentsWithFixedCSSValue.get(cssProperty);
			
		//if the parents with fixed CSS are not previously computed, then compute them now and store them
		if (parentsFixed == null) {
			parentsFixed = new ArrayList<>();
			DomNode parent = domNode.getParent();
			while (parent != null && ! parent.getTagName().equalsIgnoreCase("HTML")) {
				Map<String, String> parentCSS = parent.getExplicitCSS();
				String cssValue = parentCSS.get(cssProperty);
				// if the parent has a fixed value.. then add it
				if (cssValue != null
						&& !cssValue.equalsIgnoreCase("auto")
						&& !cssValue.equalsIgnoreCase("inherit")
						&& !cssValue.equalsIgnoreCase("initial")) {

					parentsFixed.add(parent);

				}

				parent = parent.getParent();
			}
			parentsWithFixedCSSValue.put(cssProperty,parentsFixed);
		}


		return parentsFixed;
	}



}
