package ifix.approach1;

import edu.gatech.xpert.dom.DomNode;
import edu.usc.cluster.ClustersSorter;
import edu.usc.cluster.ElementsClusterer;
import edu.usc.config.Config;
import edu.usc.gwali.Gwali;
import edu.usc.layoutgraph.LayoutGraph;
import edu.usc.util.CSSParser;
import ifix.fitness.FitnessFunction;
import ifix.fitness.FixTuple;
import ifix.heuristic.Heuristic;
import ifix.input.ReadInput;
import util.Constants;
import util.Util;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class MainIterator
{
	private static Gwali gwali;
	private static LayoutGraph originalGraph;
	private static FitnessFunction originalFitness;
	
	private static double phase1TotalTime;
	private static long clusteringTotalTime;
	
	private static double phase2TotalTime;
	private static int iterationCount;
	
	private FitnessFunction optimalFitness;
	
	private static Solution solution;
	private static Map<String, List<String>> pageClustersMap;
	
	private static Map<String, Map<String,Double>> AvgOriginalValueMap;

	private static Map<String,String> originalCSSValuesCache;
	
	public static Gwali getGwali()
	{
		return gwali;
	}
	public static double getPhase1Time()
	{
		return phase1TotalTime;
	}
	public static long getClusteringTime()
	{
		return clusteringTotalTime;
	}
	public static double getPhase2Time()
	{
		return phase2TotalTime;
	}
	public static int getIterationCount()
	{
		return iterationCount;
	}
	public FitnessFunction getOptimalFitness() {
		return optimalFitness;
	}
	public static FitnessFunction getOriginalFitness()
	{
		return originalFitness;
	}
	public static Solution getSolution()
	{
		return solution;
	}
	public static Map<String, List<String>> getPageClustersMap()
	{
		return pageClustersMap;
	}
	public static double getPhase1TotalTime() {
		return phase1TotalTime;
	}
	public static void setPhase1TotalTime(double phase1TotalTime) {
		MainIterator.phase1TotalTime = phase1TotalTime;
	}
	public static double getPhase2TotalTime() {
		return phase2TotalTime;
	}
	public static void setPhase2TotalTime(double phase2TotalTime) {
		MainIterator.phase2TotalTime = phase2TotalTime;
	}
	public static void setSolution(Solution solution) {
		MainIterator.solution = solution;
	}
	public static void setPageClustersMap(Map<String, List<String>> pageClustersMap) {
		MainIterator.pageClustersMap = pageClustersMap;
	}

	public void runIterator()
	{
		// initialize static variables
		FitnessFunction.setObjectives(new ArrayList<>());
		FitnessFunction.setFitnessScoreCache(new HashMap<Chromosome, FitnessFunction>());
		FitnessFunction.setTotalFitnessFunctionTimeInSec(0.0);
		FitnessFunction.setPhase1FitnessFunctionCalls(0);
		FitnessFunction.setPhase1CachedFitnessFunctionCalls(0);
		FitnessFunction.setPhase2FitnessFunctionCalls(0);
		FitnessFunction.setPhase2CachedFitnessFunctionCalls(0);
		FitnessFunction.setParentFitnessFunctionCalls(0);
		Search.setClusterGenerationFitnessScorePhase1(new ArrayList<>());
		Search.setGenerationFitnessScorePhase2(new TreeMap<String, Double>());
		Search.setFitnessScoreTrendGraphPhase1(new ArrayList<>());
		CSSParser.resetInstance();
		solution = new Solution();
		pageClustersMap = new HashMap<String, List<String>>();
		originalFitness = null;
		originalGraph = null;
		optimalFitness = null;
		phase1TotalTime = 0.0;
		phase2TotalTime = 0.0;
		clusteringTotalTime = 0;
		Util.xpathToElementMap = new HashMap<>();
		originalCSSValuesCache = new HashMap<>();
		
		gwali = new Gwali(ReadInput.getRefDriver(), ReadInput.getTestDriver());

		iterationCount = 0;
		int saturationCount = 0;
		double prevIterationFitnessScore = -1;
		
		System.out.println("---------- START APPROACH 1 ----------");
		
		// get all clusters for page
		long clusteringStart = System.nanoTime();
		System.out.println("Finding page clusters");
		ElementsClusterer clusterer = new ElementsClusterer(gwali.getPutElements());
		clusterer.perfomrClustering();
		ArrayList<ArrayList<String>> pageClusters = clusterer.getClustringResultsXpaths();
		ArrayList<ArrayList<DomNode>> pageClustersDomNodes = clusterer.getClustringResultsDomNodes();
		int clusterId = 1;
		System.out.println("\nPage Clusters (size = " + pageClusters.size() +"):");
		for(ArrayList<String> cluster : pageClusters)
		{
			pageClustersMap.put(Constants.CLUSTER_PREFIX + clusterId, cluster);
			System.out.println(Constants.CLUSTER_PREFIX + clusterId + " -> " + cluster);
			clusterId++;
		}
		long clusteringEnd = System.nanoTime();
		clusteringTotalTime = clusteringEnd - clusteringStart;
		
		//this has to be set after identifying the clusters
		AvgOriginalValueMap = computeAvgValuesForClusters();
		
		while(true)
		{
			iterationCount++;
			System.out.println("\n********* ITERATION " + iterationCount + " *********");

			// run GWALI
			FitnessFunction ff = new FitnessFunction();
			ff.calculateFitnessScore(solution.getFixTuples());
			FitnessFunction.getObjectives().add(new Double[]{Util.round((double) ff.getInconsistenciesObjective()), Util.round(ff.getAestheticObjective())});
			if(originalFitness == null)
			{
				originalFitness = ff.copy();
				optimalFitness = originalFitness.copy();
				System.out.println("GWALI results before: ");
				gwali.printIssues();
				originalGraph = gwali.getPutLG();
				System.out.println();
			}
			
			System.out.println("\nPotentially faulty elements: (size = " + ff.getPotentiallyFaultyElements().size() + ")");
			for(String x : ff.getPotentiallyFaultyElements())
			{
				System.out.println(x);
			}
			ArrayList<String[]> clusterPairs = new ArrayList<>();
			List<String[]> potentiallyFaultyElementPairs = ff.getPotentiallyFaultyElementPairs();
			System.out.println("\nPotentially faulty element pairs: (size = " + potentiallyFaultyElementPairs.size() + ")");
			Map<String, List<DomNode>> relevantClustersDomNodes = new HashMap<>();
			for(String[] e : potentiallyFaultyElementPairs)
			{
				ArrayList<String> c1 = ClustersSorter.findElementsCluster(pageClusters, e[0]);
				ArrayList<String> c2 = ClustersSorter.findElementsCluster(pageClusters, e[1]);
				String c1Id = (String) Util.getKeyFromValue(pageClustersMap, c1);
				String c2Id = (String) Util.getKeyFromValue(pageClustersMap, c2);
				System.out.println(e[0] + " - " + e[1] + " => " + c1Id + " - " + c2Id);
				clusterPairs.add(new String[]{c1Id, c2Id});
				
				if(!relevantClustersDomNodes.containsKey(c1Id))
				{
					relevantClustersDomNodes.put(c1Id, ClustersSorter.findElementsDomNodeCluster(pageClustersDomNodes, e[0]));
				}
				if(!relevantClustersDomNodes.containsKey(c2Id))
				{
					relevantClustersDomNodes.put(c2Id, ClustersSorter.findElementsDomNodeCluster(pageClustersDomNodes, e[1]));
				}
			}
			String imageFilePath = ReadInput.getTestScreenshotBeforePath().replace("test_before", "test_clustering_iter" + iterationCount);
			Util.takeScreenShot(ReadInput.getTestDriver(), imageFilePath);
			Map<String, List<Rectangle>> relevantClusterRectangles = Util.getClusterRectangles(relevantClustersDomNodes);
			try
			{
				Util.drawClusters(imageFilePath, relevantClusterRectangles);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			List<List<String>> dependentRelevantClustersList = Util.getDependencies(clusterPairs);
			List<String> combinedList = new ArrayList<>();
			System.out.println("\nDependent relevant clusters: (size = " + dependentRelevantClustersList.size() + ")");
			for(List<String> list : dependentRelevantClustersList)
			{
				System.out.println(list);
				combinedList.addAll(list);
			}
			dependentRelevantClustersList = new ArrayList<>();
			dependentRelevantClustersList.add(combinedList);
			
			System.out.println("\nNumber of inconsistencies reported by GWALI = " + gwali.getNoOfIncosistancy());
			System.out.println("Amount of inconsistency reported by GWALI = " + gwali.getAmountOfInconsistancy());
			
			prevIterationFitnessScore = ff.getInconsistenciesObjective();
			Search search = new Search();
			/* ------ PHASE 1 ------ */
			System.out.println("\n---------- START PHASE 1 ----------");
			long startTime = System.nanoTime();
			List<Solution> candidateSolutions = new ArrayList<Solution>();
			
			int count = 1;
			/*dependentRelevantClustersList = new ArrayList<List<String>>();
			List<String> temp = new ArrayList<String>();
			temp.add("C10");
			temp.add("C19");
			dependentRelevantClustersList.add(temp);*/
			for(List<String> dependentCluster : dependentRelevantClustersList)
			{
				long startClusterTime = System.nanoTime();
				System.out.println("\n--------- Processing cluster " + dependentCluster + ": " + (count++) + "/" + dependentRelevantClustersList.size() + " ---------");
				System.out.println("cluster = " + dependentCluster + "\n");
				if(dependentCluster.size() == 0)
					continue;
				
				Solution soln = search.phase1Search(dependentCluster);
				long endClusterTime = System.nanoTime();
				if(soln != null)
				{
					candidateSolutions.add(soln);
					System.out.println("\n--------- Solution for this cluster = " + soln + " -----------");
				}
				else
				{
					System.out.println("\n--------- No solution for this cluster found -----------");
				}
				System.out.println("Time for this cluster = " + Util.convertNanosecondsToSeconds(endClusterTime - startClusterTime) + "s");
			}
			long endTime = System.nanoTime();
			
			for(Solution candSolution : candidateSolutions)
			{
				for(FixTuple ft : candSolution.getFixTuples())
				{
					solution.addFixTuple(ft);
				}
			}
			
			search.setOptimalFitness(solution);
			optimalFitness = search.getOptimalFitness().copy();
			if(solution.getFixTuples().size() > 0)
			{
				if(prevIterationFitnessScore == search.getOptimalFitness().getFitnessScore())
				{
					saturationCount++;
				}
				else
				{
					saturationCount = 0;
				}
				prevIterationFitnessScore = search.getOptimalFitness().getFitnessScore();
			}
			
			
			System.out.println("Total time for phase 1 = " + Util.convertNanosecondsToSeconds(endTime - startTime) + "s");
			phase1TotalTime = phase1TotalTime + Util.convertNanosecondsToSeconds(endTime - startTime);

			System.out.println("---------- END PHASE 1 ----------");

			
			
			// check termination conditions
			if(isTerminate(iterationCount, saturationCount))
				break;
			else
			{
				// apply values to test page
				System.out.println("Apply repair values in preparation for next iteration");
				Util.resetPageToOriginalState();
				System.out.println("Solution so far = " + solution);

				Util.applyChangesToPage(solution.getFixTuples(), true);
				gwali.ChangePUT(ReadInput.getTestDriver());
				gwali.runGwali();
				potentiallyFaultyElementPairs = gwali.getPotentiallyFaultyElementPairs();
			}
		}
		System.out.println("---------- END APPROACH 1 ----------");
		System.out.println();
	}
	
	public static LayoutGraph getOriginalGraph() {
		return originalGraph;
	}
	private boolean isTerminate(int iterationCount, int saturationCount)
	{
		boolean terminate = false;
		if(iterationCount >= Constants.MAX_ITERATIONS_APPROACH1){
			System.out.println("Terminating because max iterations reached");
			terminate = true;
		}
		else if(saturationCount >= Constants.SATURATION_POINT_APPROACH1){
			System.out.println("Terminating because saturation point reached");
			terminate = true;
		}
		return terminate;
	}
	public static Map<String,String> getOriginalCSSValuesCache() {
		return originalCSSValuesCache;
	}
	
	
	public static void setOriginalCSSValuesCache(Map<String,String> originalCSSValuesCache) {
		MainIterator.originalCSSValuesCache = originalCSSValuesCache;
	}
	
	public static double getClusterCSSPropAvgValue(String ClusterId, String CSSProperty) {
		Map<String, Double> cssAvgMap = AvgOriginalValueMap.get(ClusterId);
		return cssAvgMap.get(CSSProperty);
	}
	
	public Map<String, Map<String, Double>> computeAvgValuesForClusters(){
		Map<String, Map<String, Double>> avgValuesMap = new HashMap<>();
		for(String cId : pageClustersMap.keySet())
		{
			Map<String, Double> map = new HashMap<>();
			for(String cssProperty : Constants.CSS_PROPERTIES_MASTER_LIST)
			{
				double avgValue = 0;
				if(Constants.SHORTHAND_CSS_PROPERTIES.contains(cssProperty))
				{
					for(String prop : Constants.ACTUAL_CSS_PROPERTIES_FOR_SHORTHANDS.get(cssProperty))
					{
						avgValue = avgValue + computeAvgValue(prop, MainIterator.getPageClustersMap().get(cId));
					}
				}
				else
				{
					avgValue = computeAvgValue(cssProperty, MainIterator.getPageClustersMap().get(cId));
				}
				map.put(cssProperty, avgValue);
			}
			avgValuesMap.put(cId, map);
		}
		return avgValuesMap;
	}
	
	private double computeAvgValue(String cssProperty, List<String> cluster) {
		double count = cluster.size();
		double sumOfValues = 0;
		for(String xpath: cluster){
			String value = "0";
			try {
				value = Util.getOriginalValue(xpath, cssProperty);
			}catch(org.openqa.selenium.InvalidSelectorException e) {
				String parentXPath = Util.getFirstHTMLParentXPath(xpath);
				value = Util.getOriginalValue(parentXPath, cssProperty);
			}
			sumOfValues += Util.getNumbersFromString(value);
			
		}
		if(count != 0)
			return sumOfValues / count;
		else return 0;
	}
}
