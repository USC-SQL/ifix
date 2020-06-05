package edu.usc.cluster;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by alameer on 3/2/17.
 */
public class XPathDistanceMeasurer {

    private HashMap<String,Character> tagToCharMap;


    public XPathDistanceMeasurer(HashSet<String> Alphabet){
        tagToCharMap = new HashMap<>();
        char i = '\u0000';
        for(String s: Alphabet){
            tagToCharMap.put(s,i);
            i++;
        }
    }

    public int computeDistance(String xpath1, String xpath2){
        String transformedXpath1 = transformXpath(xpath1);
        String transformedXpath2 = transformXpath(xpath2);
        int dist = StringUtils.getLevenshteinDistance(transformedXpath1,transformedXpath2);
        return dist;
    }



    private String transformXpath(String xpath){
        String transformedXpath = "";
        String[] xpathParts = xpath.split("\\/");
        for (String part: xpathParts) {
            transformedXpath += tagToCharMap.get(part);
        }
        return transformedXpath;
    }

    public static HashSet<String> generateAlphabet(String xpaths[]){
        HashSet<String> alphapet = new HashSet<String>();
        for (int i = 0; i < xpaths.length ; i++) {
            String xpathParts[] = xpaths[i].split("\\/");
            for (String part: xpathParts ) {
                alphapet.add(part);
            }
        }
        return alphapet;
    }
}
