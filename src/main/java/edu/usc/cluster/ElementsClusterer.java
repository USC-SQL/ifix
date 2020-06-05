package edu.usc.cluster;

import edu.gatech.xpert.dom.DomNode;
import edu.usc.gwali.Gwali;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import util.Util;

import java.util.ArrayList;
import java.util.List;



public class ElementsClusterer {
	private List<Cluster<ElementWrapper>> clusterResults;
	private DBSCANClusterer<ElementWrapper> clusterer;
	private static List<ElementWrapper> wrappedElems;

	// DBSCAN attributes
	private final double EPS = 0.5;
	private final int MIN_POINTS = 0;
	

	// normalize the values using mean and stdDev (standardize),
	// so it is has a mean of 0 and a standard deviation of 1.
	// if this is set to false, the values will be only scaled to [0-1] only and not standardized
	private final boolean standardize = false;
	

	
	//constructor to cluster elements in an ArrayList (used by ifix)
	public ElementsClusterer(ArrayList<DomNode> elementsForClustering){
		
		elementsForClustering = Gwali.removeNonTagNodes(elementsForClustering);
		
		ArrayList<DomNode> visibleNodes = new ArrayList<DomNode>();
		//only cluster visible elements
		for (DomNode domNode : elementsForClustering) {
			if(!domNode.isVisible() || domNode.getTagName().equalsIgnoreCase("option"))
				continue;
			else
				visibleNodes.add(domNode);
		}
		prepareElementsForClustering(visibleNodes);
	}
	
	private void prepareElementsForClustering(List<DomNode> elementsForClustering){
		wrappedElems = new ArrayList<ElementWrapper>();
		for (DomNode elm : elementsForClustering) {
			ElementWrapper elmWraped = new ElementWrapper(elm);
			wrappedElems.add(elmWraped);
			Util.xpathToElementMap.put(elm.getxPath(), elmWraped);
		}
		clusterer = new DBSCANClusterer<ElementWrapper>(EPS,MIN_POINTS,new WebElmDistanceMeasure(wrappedElems));
	}
	
	
	public List<Cluster<ElementWrapper>> getClusterResults()
	{
		return clusterResults;
	}
	
	
	
	public List<Cluster<ElementWrapper>> perfomrClustering()
	{	
		//use either use z-score or use min max normalization..
		//normalize points to [0-1] range..
		/*
		if(standardize == true){
			double[] means = computeMeans(wrappedElems);
			double[] stdDevs = computeStdDevs(wrappedElems);
			for (ElementWrapper elmWraped : wrappedElems){
				elmWraped.standardizePoints(means, stdDevs);
			}
		}
		else{
			double[] mins = computeMins(wrappedElems);
			double[] maxs = computeMaxs(wrappedElems);
			for (ElementWrapper elmWraped : wrappedElems){
				elmWraped.ScalePoints(mins, maxs);
			}
		}*/
		
		List<Cluster<ElementWrapper>> clusterResults = clusterer.cluster(wrappedElems);
		this.clusterResults  = clusterResults;
		return clusterResults;
	}

/*

	private double[] computeMaxs(List<ElementWrapper> wrappedElems) {
		double[] maxs = new double[ElementWrapper.NO_OF_FEATURES];
		//initialize to the extremes to avoid having initial value as min/max..
		for (int i = 0; i < maxs.length; i++)
			maxs[i] = Double.MIN_VALUE;

		for (ElementWrapper elmWraped : wrappedElems) {
			for (int i = 0; i < maxs.length; i++) {
				if(elmWraped.getVectorPoint()[i] > maxs[i])
					maxs[i] = elmWraped.getVectorPoint()[i];
			}
		}
		
		return maxs;
	}


	private double[] computeMins(List<ElementWrapper> wrappedElems) {
		double[] mins = new double[ElementWrapper.NO_OF_FEATURES];
		//initialize to the extremes to avoid having initial value as min/max..
		for (int i = 0; i < mins.length; i++)
			mins[i] = Double.MAX_VALUE;
		
		
		for (ElementWrapper elmWraped : wrappedElems) {
			for (int i = 0; i < mins.length; i++) {
				if(elmWraped.getVectorPoint()[i] < mins[i])
					mins[i] = elmWraped.getVectorPoint()[i];
			}
		}
		return mins;
	}
	
	
	private double[] computeMeans(List<ElementWrapper> wrappedElems) {
		double[] means = new double[ElementWrapper.NO_OF_FEATURES];
		for (int i = 0; i < ElementWrapper.NO_OF_FEATURES; i++) {
			SummaryStatistics stats = new SummaryStatistics();
			for (ElementWrapper elmWraped : wrappedElems) {
				stats.addValue(elmWraped.getVectorPoint()[i]);
			}
			means[i] = stats.getMean();
		}
		return means;
	}


	private double[] computeStdDevs(List<ElementWrapper> wrappedElems) {
		double[] stdDevs = new double[ElementWrapper.NO_OF_FEATURES];
		for (int i = 0; i < ElementWrapper.NO_OF_FEATURES; i++) {
			SummaryStatistics stats = new SummaryStatistics();
			for (ElementWrapper elmWraped : wrappedElems) {
				stats.addValue(elmWraped.getVectorPoint()[i]);
			}
			stdDevs[i] = stats.getStandardDeviation();
		}
		return stdDevs;

	}*/
		
	public void printClusterResults(){
		int i = 0;
		for (Cluster<ElementWrapper> cluster : clusterResults) {
			i++;
			System.out.println("Cluster " + i + ":");
			for (ElementWrapper element : cluster.getPoints()) {
				System.out.println("\t" + element.getDomNode().getxPath());
			}
			
		}
	}

	public ArrayList<ArrayList<DomNode>> getClustringResultsDomNodes(){
		ArrayList<ArrayList<DomNode>> domNodesClusters = new ArrayList<ArrayList<DomNode>>();
		for (Cluster<ElementWrapper> cluster : clusterResults) {
			ArrayList<DomNode> DomNodesCluster = new ArrayList<DomNode>();
			for (ElementWrapper elmWrapper: cluster.getPoints()) {
				DomNodesCluster.add(elmWrapper.getDomNode());
			}
			domNodesClusters.add(DomNodesCluster);
		}
		return domNodesClusters;
	}


	public ArrayList<ArrayList<String>> getClustringResultsXpaths(){
		ArrayList<ArrayList<String>> xPathsClusters = new ArrayList<ArrayList<String>>();
		for (Cluster<ElementWrapper> cluster : clusterResults) {
			ArrayList<String> xPathsCluster = getXpathCluster(cluster);
			xPathsClusters.add(xPathsCluster);
		}
		return xPathsClusters;
	}

	private ArrayList<String> getXpathCluster(Cluster<ElementWrapper> cluster){
		ArrayList<String> Xpathscluster = new ArrayList<String>(cluster.getPoints().size());
		for (ElementWrapper element : cluster.getPoints()) {
			String xpath = element.getDomNode().getxPath();
			Xpathscluster.add(xpath);
		}
		return Xpathscluster;
	}

}
