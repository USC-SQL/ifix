package edu.usc.cluster;

import edu.gatech.xpert.dom.DomNode;

import java.util.ArrayList;

public class ClustersSorter {
	
	//This method returns a sorted list of clusters given a set of clusters and a list of sorted xpaths
	public static ArrayList<ArrayList<String>> getRelevantClusters(ArrayList<ArrayList<String>> clusters, ArrayList<String> sortedXpaths){
		ArrayList<ArrayList<String>> sortedClusters = new ArrayList<ArrayList<String>>();
		//iterate over the xpaths in order
		for (String xpath : sortedXpaths) {
			ArrayList<String> xpathsCluster = findElementsCluster(clusters,xpath);
			//if the cluster is not already there in the list, add it to the end
			if(!sortedClusters.contains(xpathsCluster)){
				sortedClusters.add(xpathsCluster);
			}
		}
		return sortedClusters;
	}

	//This method returns a sorted list of clusters of DomNodes given a set of clusters and a list of sorted xpaths
	public static ArrayList<ArrayList<DomNode>> getRelevantDomNodeClusters(ArrayList<ArrayList<DomNode>> clusters, ArrayList<String> sortedXpaths){
		ArrayList<ArrayList<DomNode>> sortedClusters = new ArrayList<ArrayList<DomNode>>();
		//iterate over the xpaths in order
		for (String xpath : sortedXpaths) {
			ArrayList<DomNode> xpathsCluster = findElementsDomNodeCluster(clusters,xpath);
			//if the cluster is not already there in the list, add it to the end
			if(!sortedClusters.contains(xpathsCluster)){
				sortedClusters.add(xpathsCluster);
			}
		}
		return sortedClusters;
	}
	
	public static ArrayList<String> findElementsCluster(ArrayList<ArrayList<String>> clusters, String xpath){
		ArrayList<String> foundCluster = new ArrayList<String>();
		for (ArrayList<String> cluster : clusters) {
			if(cluster.contains(xpath)){
				foundCluster = cluster;
				break;
			}
		}
		return foundCluster;
	}

	public static ArrayList<DomNode> findElementsDomNodeCluster(ArrayList<ArrayList<DomNode>> clusters, String xpath){
		ArrayList<DomNode> foundCluster = new ArrayList<>();
		for (ArrayList<DomNode> cluster : clusters) {
			for (DomNode node: cluster) {
				if(node.getxPath().equalsIgnoreCase(xpath)) {
					foundCluster = cluster;
					break;
				}
				if (foundCluster.size() != 0)
					break;
			}
		}
		if(foundCluster.size() == 0)
			System.out.println("couldn't find cluster for: "+xpath);
		return foundCluster;
	}
	

}
