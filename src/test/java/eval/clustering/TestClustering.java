package eval.clustering;

import edu.gatech.xpert.dom.DomNode;
import edu.usc.cluster.ClustersSorter;
import edu.usc.cluster.ElementsClusterer;
import edu.usc.config.Config;
import edu.usc.gwali.Gwali;
import edu.usc.util.CSSParser;
import edu.usc.util.Utils;
import eval.Subject;
import ifix.input.ReadInput;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.xml.sax.SAXException;
import util.Constants;
import util.Util;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestClustering
{
	public Map<String, List<Rectangle>> getClustersDBSCAN(List<String> potentiallyFaultyElements, Gwali gwali)
	{
		CSSParser.resetInstance();
		
		System.out.println("Finding page clusters");
		ElementsClusterer clusterer = new ElementsClusterer(gwali.getPutElements());
		clusterer.perfomrClustering();
		ArrayList<ArrayList<DomNode>> pageClusters = clusterer.getClustringResultsDomNodes();
				
		System.out.println("Getting relevant clusters");
		ArrayList<ArrayList<DomNode>> relevantClusterDomNodes = ClustersSorter.getRelevantDomNodeClusters(pageClusters, new ArrayList<>(potentiallyFaultyElements));
		
		System.out.println("\nRelevant clusters: (size = " + relevantClusterDomNodes.size() + ")");
		int count = 1;
		Map<String, List<DomNode>> clusters = new HashMap<>();
		for(ArrayList<DomNode> c : relevantClusterDomNodes)
		{
			System.out.println("\nRelevant cluster " + (count++) + ". (size = " + c.size() + ") = ");
			for(DomNode node : c)
			{
				System.out.println(node.getxPath());
			}
			clusters.put("C" + count, c);
		}
		return Util.getClusterRectangles(clusters);
	}
	
	public List<List<Rectangle>> getClustersRtree(ArrayList<String> potentiallyFaultyElements)
	{
		List<List<Rectangle>> clusterRectangles = new ArrayList<>();
		
		HtmlDomTree rt = null;
		try
		{
			rt = new HtmlDomTree();
		}
		catch (SAXException | IOException e1)
		{
			e1.printStackTrace();
		}
		rt.buildHtmlDomTree();
		
		System.out.println("\nCluster MBRs");
		int i = 1;
		for(String xpath : potentiallyFaultyElements)
		{
			WebElement e = ReadInput.getTestDriver().findElement(By.xpath(xpath));
			List<Rectangle> cluster = rt.getClusterMBR(new com.infomatiq.jsi.Rectangle(e.getLocation().x, e.getLocation().y, e.getLocation().x+e.getSize().width, e.getLocation().y+e.getSize().height));
			
			System.out.print("xpath = " + xpath + " -> C" + i +" (size = " + cluster.size() + "): {");
			int cnt = 0;
			for(Rectangle r : cluster)
			{
				System.out.print("[x=" + r.x + ",y=" + r.y + ",width=" + r.width + ",height=" + r.height + "]");
				cnt++;
				if(cnt < cluster.size())
					System.out.print(", ");
			}
			System.out.println("}");
			
			clusterRectangles.add(cluster);
			i++;
		}
		return clusterRectangles;
	}
	/*
	public List<List<Rectangle>> getClustersVIPS(Subject subject)
	{
		String url = subject.getTestFilepathRelFromBasepath().replace(subject.getBasepath(), "http://alameer.usc.edu:8080/ifix/");
		int VIEWPORT_WIDTH = Math.toIntExact((long) ((JavascriptExecutor) ReadInput.getTestDriver()).executeScript("return Math.max(document.documentElement.clientWidth, window.innerWidth || 0);"));
		int VIEWPORT_HEIGHT = Math.toIntExact((long) ((JavascriptExecutor) ReadInput.getTestDriver()).executeScript("return Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"));
		String imagePath = "";

		List<List<Rectangle>> clusterRectangles = new ArrayList<>();
		try
		{
			Vips vips = new Vips(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, imagePath);
			// disable graphics output
			vips.enableGraphicsOutput(false);
			// disable output to separate folder (no necessary, it's default value is false)
			vips.enableOutputToFolder(false);
			// set permitted degree of coherence
			vips.setPredefinedDoC(8);
			// start segmentation on page
			vips.startSegmentation(url);

			System.out.println("Cluster rectangles = ");
			for(Rectangle r : vips.getClusterRectangles())
			{
				System.out.println("[" + r.x + "," + r.y + "," + r.width + "," + r.height + "]");
				List<Rectangle> clusterMembers = new ArrayList<>();
				clusterMembers.add(r);
				clusterRectangles.add(clusterMembers);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return clusterRectangles;
	}
	*/
	public List<List<Rectangle>> getClustersBlockOMatic(String imageFilePath)
	{
		JavascriptExecutor js = (JavascriptExecutor) ReadInput.getTestDriver();
		for(File jsFile : new File("src/test/resources/blockomatic-js").listFiles())
		{
			js.executeScript("var head = document.getElementsByTagName('head')[0];"
					+ "var scriptTag = document.createElement('script');"
					+ "scriptTag.type = 'text/javascript';"
					+ "scriptTag.src = '" + jsFile.getAbsolutePath() +"';"
					+ "head.appendChild(scriptTag);");
		}
		js.executeScript("startSegmentation(window.self, 3, 50, 'record');");
		
		Util.takeScreenShot(ReadInput.getTestDriver(), imageFilePath);
		
		return new ArrayList<>();
	}
	
	public static void main(String[] args) throws IOException
	{
		Config.applyConfig();
		
		String basepath = "/Users/sonal/USC/search-based-repair/ifix/TestCases/ScrapBook/data";//"/home/ifix/ifix/TestCases/ScrapBook/data";
		String resultsPath = "/Users/sonal/USC/search-based-repair/ifix/eval-results/clustering_dbscan_" + System.nanoTime();//"/home/ifix/ifix/eval-results/clustering_dbscan_" + System.nanoTime();
		
		new File(resultsPath).mkdir();
		
		List<Subject> subjects = new ArrayList<Subject>();
//		subjects.add(new Subject("akamai", basepath, "", ""));
//		subjects.add(new Subject("bestrestaurants", basepath, "", ""));
//		subjects.add(new Subject("calottery", basepath, "", ""));
//		subjects.add(new Subject("designsponge", basepath, "", ""));
//		subjects.add(new Subject("dmv", basepath, "", ""));
//		subjects.add(new Subject("els", basepath, "", ""));
//		subjects.add(new Subject("facebook", basepath, "www.facebook.com/index.html", "bg-bg.facebook.com/index.html"));
//		subjects.add(new Subject("flynas", basepath, "", ""));
//		subjects.add(new Subject("googleearth", basepath, "", ""));
//		subjects.add(new Subject("googlelogin", basepath, "accounts.google.com/index.html", "accounts.google.com/index.html"));
//		subjects.add(new Subject("hightail", basepath, "", ""));
//		subjects.add(new Subject("hotwire", basepath, "www.hotwire.com/index.html", "www.hotwire.com/index.html"));
//		subjects.add(new Subject("ixigo", basepath, "", ""));
//		subjects.add(new Subject("linkedin", basepath, "", ""));
//		subjects.add(new Subject("museum", basepath, "", ""));
//		subjects.add(new Subject("myplay", basepath, "", ""));
//		subjects.add(new Subject("qualitrol", basepath, "", ""));
		subjects.add(new Subject("rentalcars", basepath, "", ""));
//		subjects.add(new Subject("skype", basepath, "", ""));
//		subjects.add(new Subject("skyscanner", basepath, "www.skyscanner.com/index.html", "www.skyscanner.com/index.html"));
//		subjects.add(new Subject("surgeon", basepath, "", ""));
//		subjects.add(new Subject("twitterhelp", basepath, "support.twitter.com/groups/50-welcome-to-twitter.html", "support.twitter.com/groups/50-welcome-to-twitter.html"));
//		subjects.add(new Subject("westin", basepath, "", ""));
		
		for(Subject subject : subjects)
		{
			new File(resultsPath + File.separatorChar + subject.getSubject()).mkdir();
			
			FirefoxDriver refDriver = Utils.getNewFirefoxDriver();
			refDriver.get("file:///" + subject.getRefFilepathRelFromBasepath());
			
			FirefoxDriver testDriver = Utils.getNewFirefoxDriver();
			testDriver.get("file:///" + subject.getTestFilepathRelFromBasepath());
			
			
			String imageFilePath = resultsPath + File.separatorChar + subject.getSubject() + File.separatorChar + "index-clustering.png";
			Util.takeScreenShot(testDriver, imageFilePath);
			
			ReadInput.setRefDriver(refDriver);
			ReadInput.setTestDriver(testDriver);
			ReadInput.setRefFilepath(subject.getRefFilepathRelFromBasepath());
			ReadInput.setTestFilepath(subject.getTestFilepathRelFromBasepath());
			
			if(Constants.RUN_IN_DEBUG_MODE)
			{
				try
				{
					String logPath = resultsPath + File.separatorChar + subject.getSubject() + File.separatorChar + "index-clustering-log.txt";
					System.setOut(new PrintStream(new FileOutputStream(logPath)));
				}
				catch (Exception e)
				{
					System.err.println("Cannot run in debug mode. All log statements will be displayed in the console.");
				}
			}
			
			Gwali gwali = new Gwali(refDriver, testDriver);
			gwali.runGwali();
			ArrayList<String> potentiallyFaultyElements = gwali.getPotentiallyFaultyElements();
			System.out.println("\nPotentially faulty elements (size = " + potentiallyFaultyElements.size() + ")");
			for(String xpath : potentiallyFaultyElements)
			{
				System.out.println(xpath);
			}
			
			TestClustering tc = new TestClustering();
			
			Map<String, List<Rectangle>> clusters = tc.getClustersDBSCAN(potentiallyFaultyElements, gwali);
			//List<List<Rectangle>> clusters = tc.getClustersRtree(potentiallyFaultyElements);
			//List<List<Rectangle>> clusters = tc.getClustersVIPS(subject);
			//List<List<Rectangle>> clusters = tc.getClustersBlockOMatic(imageFilePath);
			
			Util.drawClusters(imageFilePath, clusters);
			
			refDriver.quit();
			testDriver.quit();
		}
	}
}
