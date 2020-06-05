package eval;

import edu.usc.cluster.ClustersSorter;
import edu.usc.cluster.ElementsClusterer;
import edu.usc.config.Config;
import edu.usc.gwali.Gwali;
import edu.usc.util.Utils;
import ifix.input.ReadInput;
import org.openqa.selenium.firefox.FirefoxDriver;
import util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestGwaliAPIs
{
	public static void main(String[] args)
	{

		String refPath = "/home/ifix/toypage/toypage-ref/index.html";
		String testPath = "/home/ifix/toypage/toypage-test/index.html";
		
		Config.applyConfig();
		
		FirefoxDriver refDriver = Utils.getNewFirefoxDriver();
		FirefoxDriver testDriver = Utils.getNewFirefoxDriver();
		
		refDriver.get("file://" + refPath);
		testDriver.get("file://" + testPath);
		ReadInput.setTestFilepath(testPath);
		
		Gwali gwali = new Gwali(refDriver, testDriver);
		gwali.runGwali();
		List<String[]> potentiallyFaultyElementPairs = gwali.getPotentiallyFaultyElementPairs();
		
		refDriver.quit();
		testDriver.quit();

		ElementsClusterer clusterer = new ElementsClusterer(gwali.getPutElements());
		clusterer.perfomrClustering();
		ArrayList<ArrayList<String>> pageClusters = clusterer.getClustringResultsXpaths();

		System.out.println("Number of inconsistency = " + gwali.getNoOfIncosistancy());
		System.out.println("Amount of inconsistency = " + gwali.getAmountOfInconsistancy());
		
		
		System.out.println("\nUnfiltered issues reported by GWALI:");
		gwali.printUnfilteredIssues();
		gwali.printIssues();
		
		List<String> potentiallyFaultyElements = gwali.getPotentiallyFaultyElements();
		System.out.println("\nPotentially faulty elements: (size = " + potentiallyFaultyElements.size() + ")");
		for(String x : potentiallyFaultyElements)
		{
			System.out.println(x);
		}
		
		
		
		System.out.println("\nPotentially faulty element pairs: (size = " + potentiallyFaultyElementPairs.size() + ")");
		Map<String, ArrayList<String>> clusterMap = new HashMap<String, ArrayList<String>>();
		ArrayList<String[]> e2Clusters = new ArrayList<>();
		int cnt = 1;
		for(String[] e : potentiallyFaultyElementPairs)
		{
			ArrayList<String> c1 = ClustersSorter.findElementsCluster(pageClusters, e[0]);
			ArrayList<String> c2 = ClustersSorter.findElementsCluster(pageClusters, e[1]);
			String c1String = "";
			String c2String = "";
			if(clusterMap.containsValue(c1))
			{
				for(String c : clusterMap.keySet())
				{
					if(clusterMap.get(c).equals(c1))
					{
						c1String = c;
						break;
					}
				}
			}
			if(clusterMap.containsValue(c2))
			{
				for(String c : clusterMap.keySet())
				{
					if(clusterMap.get(c).equals(c2))
					{
						c2String = c;
						break;
					}
				}
			}
			if(c1String.isEmpty() || c2String.isEmpty())
			{
				if(c1String.isEmpty() && !c2String.isEmpty())
				{
					c1String = "c" + cnt;
					cnt++;
					clusterMap.put(c1String, c1);
				}
				else if(!c1String.isEmpty() && c2String.isEmpty())
				{
					c2String = "c" + cnt;
					cnt++;
					clusterMap.put(c2String, c2);
				}
				else
				{
					c1String = "c" + cnt;
					if(!c1.equals(c2))
					{
						cnt++;
					}
					c2String = "c" + cnt;
					cnt++;
					clusterMap.put(c1String, c1);
					clusterMap.put(c2String, c2);
				}
			}
			e2Clusters.add(new String[]{c1String, c2String});
			System.out.println(e[0] + " - " + e[1] + " => " + c1String + " - " + c2String);
		}
		
		List<List<String>> dependentList = Util.getDependencies(e2Clusters);
		System.out.println("\nDependent element clusters: (size = " + dependentList.size() + ")");
		for(List<String> list : dependentList)
		{
			System.out.println(list);
		}
	}
}
