package ifix.heuristic;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.gatech.xpert.dom.DomNode;
import ifix.approach1.MainIterator;
import util.Constants;

import org.openqa.selenium.firefox.FirefoxDriver;


public class Heuristic {

    private static final double POSITIVE_DEGREE_OF_CHANGE = 1;
    private static final double NEGATIVE_DEGREE_OF_CHANGE = -1;

    FirefoxDriver baselineDriver,putDriver;
    Map<DomNode, DomNode> matchedNodes;



    public Heuristic(FirefoxDriver baselineDriver, FirefoxDriver putDriver,
                     Map<DomNode, DomNode> matchedNodes) {
        super();
        this.baselineDriver = baselineDriver;
        this.putDriver = putDriver;
        this.matchedNodes = matchedNodes;
    }

    public int getEstimatedChangeValue(String cssProperty, String clusterID){

        List<String> cluster = MainIterator.getPageClustersMap().get(clusterID);
        double avgValue = MainIterator.getClusterCSSPropAvgValue(clusterID,cssProperty);

        double sumTextExpansion = 0;
        for (String xpath : cluster) {
            double textExpansionRatio = getTextExpansionRatio(xpath);
            if(textExpansionRatio > 1){
                sumTextExpansion += textExpansionRatio;
            }
            else{
            	sumTextExpansion += 1;
            }
        }
        double avgTextExpansion;
        if(cluster.size() == 0)
            avgTextExpansion = 1.5; // 50% increase is default value;
        else{
            avgTextExpansion = sumTextExpansion / (double)cluster.size();
        }

        double newAvgValue, delta;
        //increase width and height
        if(cssProperty.equals("width") || cssProperty.equals("height")){
        	newAvgValue = avgValue * avgTextExpansion;
        }//decrease font-size
        else if(cssProperty.equals("font-size")){
        	newAvgValue = avgValue / avgTextExpansion;
        }
        else if(cssProperty.equals(Constants.PADDING_LEFT_RIGHT)){
        	newAvgValue = avgValue / avgTextExpansion;
        }
        else if(cssProperty.equals(Constants.PADDING_TOP_BOTTOM)){
        	newAvgValue = avgValue / avgTextExpansion;
        }
        else if(cssProperty.equals(Constants.MARGIN_LEFT_RIGHT)){
        	newAvgValue = avgValue / avgTextExpansion;
        }
        else if(cssProperty.equals(Constants.MARGIN_TOP_BOTTOM)){
        	newAvgValue = avgValue / avgTextExpansion;
        }
        else{
            ShowExceptionMessage("Cannot get degree of change from the analytical heuristic approach. Unsupported property: " + cssProperty + "returning -0.5 ");
            return 0;
        }

        delta = (newAvgValue - avgValue) / Constants.CHANGE_FACTORS.get(cssProperty);


        return (int) Math.round(delta);
    }




    private double getTextExpansionRatio(String xpath){

        double ratio = 1.5;

        String xpathInPUT = xpath;
        String xpathInBaseLine = null;


        for (Entry<DomNode, DomNode> node : matchedNodes.entrySet()) {
            if(node != null && node.getValue() != null && node.getValue().getxPath().equals(xpath))
                xpathInBaseLine = node.getKey().getxPath();
        }
        if(xpathInBaseLine == null){
            ShowExceptionMessage("No element is matching in the baseline page!! ");
        }
        else{
            long textSizeBefore = getTextSize(baselineDriver, xpathInBaseLine);
            long textSizeAfter  = getTextSize(putDriver, xpathInPUT);
            ratio = ( (double)textSizeAfter / (double)textSizeBefore );
        }
        return ratio;

    }



    private long getTextSize(FirefoxDriver driver,String xpath){
        String javaScriptCode = "node = document.evaluate(\"%1$s\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"
                + "textSize = 0;"
                + "if(node && node.tagName && node.tagName === 'INPUT'){ "
                + "		if(node.value && node.value.length != 0){"
                + " 		textSize = node.value.length;"
                + "		}"
                + "		else if(node.placeholder && node.placeholder.length != 0){"
                + "			textSize = node.placeholder.length;"
                + "		}"
                + "}"
                + "else if(node.textContent){"
                + "		textSize = node.textContent.length;"
                + "}"
                + "return textSize;";





        String toBeExecuted = String.format(javaScriptCode, xpath);
        long size = (Long) driver.executeScript(toBeExecuted);
        return size;
    }

    private void ShowExceptionMessage(String message){
        try {
            throw new Exception(message);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

}
