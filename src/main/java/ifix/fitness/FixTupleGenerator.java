package ifix.fitness;

import edu.gatech.xpert.dom.DomNode;
import edu.usc.cluster.ElementWrapper;
import edu.usc.layoutgraph.LayoutGraph;
import edu.usc.layoutgraph.edge.NeighborEdge;
import ifix.approach1.Gene;
import ifix.approach1.MainIterator;
import ifix.input.ReadInput;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import util.Constants;
import util.Util;

import java.util.*;

/**
 * Created by alameer on 5/3/17.
 * A helper class to find fixtuples for an element we want to fix
 * T
 *
 */


public class FixTupleGenerator {
	


    public static List<FixTuple> generateGenesFixTuple(Gene gene, boolean isDebug){
        // generate a fixtuple for each of the elements in the gene's cluster
        List<FixTuple> childrenFixTuples = generateClusterFixTuple(gene);
        String cssPropertyToFix = gene.getCssProperty();
        List<FixTuple> allTuples = childrenFixTuples;

        if(cssPropertyToFix.equalsIgnoreCase("width") || cssPropertyToFix.equalsIgnoreCase("height")){
            List<FixTuple> parentsTuple = getParentsFixTuples(childrenFixTuples,cssPropertyToFix);

            allTuples.addAll(parentsTuple);
        }

        //for font-size property, we only consider children (i.e. no parents added)
        if(cssPropertyToFix.equalsIgnoreCase("font-size")){
            ; // do nothing
        }
        //
        if (cssPropertyToFix.equals(Constants.PADDING_LEFT_RIGHT) || cssPropertyToFix.equals(Constants.PADDING_TOP_BOTTOM) ||
        		cssPropertyToFix.equals(Constants.MARGIN_LEFT_RIGHT) || cssPropertyToFix.equals(Constants.MARGIN_TOP_BOTTOM)){
            
        }
        
        return allTuples;

    }

    private static ArrayList<FixTuple> getParentsFixTuples(List<FixTuple> childrenFixTuples, String cssPropertyToFix) {

        Set<FixTuple> parentsFixTuples = new HashSet<>();
        for (FixTuple tuple: childrenFixTuples) {

            int elmOldEdgePosition = getElementEndEdgePosition(tuple.getXpath(),cssPropertyToFix);
            LayoutGraph layoutGraph = MainIterator.getOriginalGraph();
        	
            int comulativePushAmount = getCumulativePushAmount(tuple,childrenFixTuples,layoutGraph);
            int elmNewEdgePosition = elmOldEdgePosition + comulativePushAmount;
            ElementWrapper elm = getElementWrapperFromXpath(tuple.getXpath());
	        ArrayList<DomNode> elmFixedParents = elm.getParentsWithFixedCSS(cssPropertyToFix);

            Set<FixTuple> newParentsFixTuple = getElementsParentFixTuple(elmNewEdgePosition,elmFixedParents,cssPropertyToFix);

            
            parentsFixTuples = combineParentFixTupleSets(parentsFixTuples,newParentsFixTuple);

        }

        return new ArrayList<FixTuple>(parentsFixTuples);

    }

    private static Set<FixTuple> combineParentFixTupleSets(Set<FixTuple> parentsFixTuples, Set<FixTuple> newParentsFixTuple) {

        for (FixTuple newTuple: newParentsFixTuple) {
            FixTuple old = null;
            for (FixTuple parentFixTuple: parentsFixTuples )
            {
                if(newTuple.getXpath().equals(parentFixTuple.getXpath())) {
                    old = parentFixTuple;
                    break;
                }
            }
            if (old == null)
                parentsFixTuples.add(newTuple);
            else
                updateFixTuple(old,newTuple);
        }
        return parentsFixTuples;
    }

    //update changeValue for tuple if new tuple has larger changeValue
    private static void updateFixTuple(FixTuple tuple, FixTuple newTuple) {
        if(newTuple.getchangeValue() > tuple.getchangeValue())
            tuple.setNewValue(newTuple.getNewValue());
    }


    private static Set<FixTuple> getElementsParentFixTuple(int elmNewEdgePosition, ArrayList<DomNode> elmFixedParents, String cssPropertyToFix) {
        Set<FixTuple> parentsOverflowed = new HashSet<>();
        int coord_idx = 2;
        if (cssPropertyToFix.equalsIgnoreCase("width")){
            coord_idx = 2; // x2 of the element
        }
        else if(cssPropertyToFix.equalsIgnoreCase("height")){
            coord_idx = 3; // y2 of the element
        }

        for (DomNode parent: elmFixedParents) {
            int overflowAmount = elmNewEdgePosition - (int)parent.getCoords()[coord_idx];
            if (overflowAmount > 0){
                String OriginalValue = Util.getOriginalValue(parent.getxPath(),cssPropertyToFix);
                String newValue = Util.computeValueWithDelta(OriginalValue,overflowAmount);
                FixTuple ft = new FixTuple(parent.getxPath(),cssPropertyToFix,OriginalValue,newValue,true);
                parentsOverflowed.add(ft);
            }
        }
        return parentsOverflowed;
    }

    private static int getCumulativePushAmount(FixTuple tuple, List<FixTuple> allOtherTuples, LayoutGraph layoutGraph) {
        int cumulativePushAmount = 0;

        //first add the push caused by the expansion of the element itself..
        cumulativePushAmount += getFixExpansionAmount(tuple);

        //then add the push caused by the expansion of the elements in its left or in its bottom
        ArrayList<FixTuple> influencingTuples = getInfluencingFixTuples(tuple, allOtherTuples, layoutGraph);
        for (FixTuple influencingTuple: influencingTuples) {
            cumulativePushAmount += getFixExpansionAmount(influencingTuple);
        }

        return cumulativePushAmount;
    }


    //this will return the list of fix tuples that are strictly to the left in case of width property
    // or tuples that are strictly on top in case of height property.
    private static ArrayList<FixTuple> getInfluencingFixTuples(FixTuple tuple, List<FixTuple> allOtherTuples, LayoutGraph layoutGraph){
        String cssProperty = tuple.getCssProperty();
        ArrayList<FixTuple> influencingTuples = new ArrayList<>();
        for (FixTuple neighborTuple: allOtherTuples) {
            NeighborEdge edge = layoutGraph.findEdge(tuple.getXpath(),neighborTuple.getXpath());
            if (edge == null) continue;

            if(cssProperty.equalsIgnoreCase("width")){
                if(edge.isRightLeft() && !edge.isTopBottom() && !edge.isBottomTop())
                    influencingTuples.add(neighborTuple);
            }
            else if (cssProperty.equalsIgnoreCase("height")){
                if(edge.isBottomTop() && !edge.isRightLeft() && !edge.isLeftRight())
                    influencingTuples.add(neighborTuple);
            }

        }
        return influencingTuples;
    }




    private static ElementWrapper getElementWrapperFromXpath(String xpath) {
        return Util.xpathToElementMap.get(xpath);
    }



    private static int getElementRightEdgePosition(String xpath){
    	ElementWrapper element = getElementWrapperFromXpath(xpath);
    	if(element != null){
    		return (int)element.getDomNode().getCoords()[2];
    	}
    	
        WebDriver d = ReadInput.getTestDriver();
        d.get("file:///" + ReadInput.getTestFilepath());
        WebElement e = d.findElement(By.xpath(xpath));
        return e.getRect().getX() + e.getRect().getWidth();
    }

    private static int getElementBottomEdgePosition(String xpath){
    	ElementWrapper element = getElementWrapperFromXpath(xpath);
    	if(element != null){
    		return (int)element.getDomNode().getCoords()[3];
    	}

    	
        WebDriver d = ReadInput.getTestDriver();
        d.get("file:///" + ReadInput.getTestFilepath());
        WebElement e = d.findElement(By.xpath(xpath));
        return e.getRect().getY() + e.getRect().getHeight();
    }

    private static int getElementEndEdgePosition(String xpath, String cssPropertyToFix){
        int endEdgePosition = 0;
        if (cssPropertyToFix.equalsIgnoreCase("width")) {
            endEdgePosition = getElementRightEdgePosition(xpath);
        }
        else if (cssPropertyToFix.equalsIgnoreCase("height")) {
            endEdgePosition = getElementBottomEdgePosition(xpath);
        }
        else {
            System.err.println("ERROR: only width and height are supported!");
        }
        return endEdgePosition;
    }


    private static double getFixExpansionAmount(FixTuple fixTuple){
        String originalValueString = Util.getOriginalValue(fixTuple.getXpath(), fixTuple.getCssProperty());
        String value = Util.computeValueWithDelta(originalValueString, fixTuple.getchangeValue());
        double absoluteChange = Util.getNumbersFromString(value) - Util.getNumbersFromString(originalValueString);
        return absoluteChange;
    }

    private static List<FixTuple> generateClusterFixTuple(Gene gene){
        List<FixTuple> childrenFixTuples = new ArrayList<>();
        // if change value is zero, return empty fix tuple (nothing happens)
        if(gene.getChangeValue() == 0){
            return childrenFixTuples;
        }

        // generate a fixtuple for each of the elements in the gene's cluster
        List<String> cluster = MainIterator.getPageClustersMap().get(gene.getClusterId());
        for(String clusterElement : cluster)
        {
        	if(Constants.SHORTHAND_CSS_PROPERTIES.contains(gene.getCssProperty()))
        	{
        		for(String prop : Constants.ACTUAL_CSS_PROPERTIES_FOR_SHORTHANDS.get(gene.getCssProperty()))
        		{
        			FixTuple t = new FixTuple(clusterElement, prop, gene.getChangeValue(), gene.getClusterId());
        			childrenFixTuples.add(t);
        		}
        	}
        	else
        	{
        		FixTuple t = new FixTuple(clusterElement, gene.getCssProperty(), gene.getChangeValue(), gene.getClusterId());
        		childrenFixTuples.add(t);
        	}
        }
        return childrenFixTuples;
    }

}

