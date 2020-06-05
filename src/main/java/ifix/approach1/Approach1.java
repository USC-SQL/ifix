package ifix.approach1;

import ifix.fitness.FitnessFunction;
import ifix.fitness.FixTuple;
import ifix.input.ReadInput;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;

import util.Constants;
import util.Util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Approach1
{
	private String outputFolder;
	public static long approachStartTime;
	public void runApproach()
	{
		String pattern = "MM-dd-yyyy-hh-mm-ss-a";
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		String timestampForOutputFolderName = format.format(new Date());
		outputFolder = new File(ReadInput.getTestFilepath()).getParent() + File.separatorChar + "output_" + timestampForOutputFolderName;
		new File(outputFolder).mkdir();
		ReadInput.setOutputFolderPath(outputFolder);
		
		if(Constants.RUN_IN_DEBUG_MODE)
		{
			try
			{
				String logPath = outputFolder + File.separatorChar + "log.txt";
				System.setOut(new PrintStream(new FileOutputStream(logPath)));
			}
			catch (Exception e)
			{
				System.err.println("Cannot run in debug mode. All log statements will be displayed on the console.");
			}
		}
		Constants.printConfig();
		
		ReadInput.getTestDriver().get("file:///" + ReadInput.getTestFilepath());
		
		// take screenshot of test page before applying fixes
		String testBeforeDest = outputFolder + File.separatorChar + "test_before.png";
		Util.takeScreenShot(ReadInput.getTestDriver(), testBeforeDest);
		ReadInput.setTestScreenshotBeforePath(testBeforeDest);
		
		MainIterator mi = new MainIterator();
		long startTime = System.nanoTime();
		approachStartTime = startTime;
		mi.runIterator();
		long endTime = System.nanoTime();
		System.out.println("\n===========================================================================");
		
		// --------- APPLY REPAIR TO PAGE --------
		int count = 1;
		Util.resetPageToOriginalState();
		
		for(FixTuple ft : MainIterator.getSolution().getFixTuples())
		{
			System.out.println(count + ". " + ft);
			count++;
		}
		System.out.println();
		Util.applyChangesToPage(MainIterator.getSolution().getFixTuples(), true);
		System.out.println();
		
		String html = ReadInput.getTestDriver().getPageSource();
		String fixedTestFile = outputFolder + File.separatorChar + "test_fixed.html";
		// save the page
		try 
		{
			FileUtils.writeStringToFile(new File(fixedTestFile), html, "UTF-8");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		// take screenshot of baseline page
		String refDest = outputFolder + File.separatorChar + "baseline.png";
		Util.takeScreenShot(ReadInput.getRefDriver(), refDest);
		
		// take screenshot of test page after applying fixes
		String testAfterDest = outputFolder + File.separatorChar + "test_after.png";
		Util.takeScreenShot(ReadInput.getTestDriver(), testAfterDest);
		
		MainIterator.getGwali().ChangePUT(ReadInput.getTestDriver());
		MainIterator.getGwali().runGwali();
		System.out.println("GWALI results after: ");
		MainIterator.getGwali().printIssues();
		ArrayList<String> potentiallyFaultyElementsAfter = MainIterator.getGwali().getPotentiallyFaultyElements();
		//int inconsistenciesObjectiveValueAfter = MainIterator.getGwali().getNoOfIncosistancy();
		int inconsistenciesObjectiveValueAfter = MainIterator.getGwali().getAmountOfInconsistancy();
		System.out.println("\nGWALI reported amount of inconsistency after repair = " + inconsistenciesObjectiveValueAfter);
		if(inconsistenciesObjectiveValueAfter > 0)
		{
			System.out.println("GWALI reported potentially faulty elements after repair = " + potentiallyFaultyElementsAfter);
		}
		
		System.out.println("\n===========================================================================");
		double reductionInInconsistencies = 0.0;
		if(MainIterator.getOriginalFitness().getInconsistenciesObjective() > 0)
		{
			reductionInInconsistencies = Util.round((double)(MainIterator.getOriginalFitness().getInconsistenciesObjective() - inconsistenciesObjectiveValueAfter)/(double)MainIterator.getOriginalFitness().getInconsistenciesObjective());
		}
		System.out.println("Reduction in amount of inconsistency = (original - after)/original = (" + MainIterator.getOriginalFitness().getInconsistenciesObjective() + 
				" - " + inconsistenciesObjectiveValueAfter + ")/" + MainIterator.getOriginalFitness().getInconsistenciesObjective() + " = " + reductionInInconsistencies);
		System.out.println("After number of inconsistencies reported by GWALI = " + MainIterator.getGwali().getNoOfIncosistancy());
		System.out.println("Optimal fitness score = " + mi.getOptimalFitness());
		int totalFitnessCallsPhase1 = FitnessFunction.getPhase1FitnessFunctionCalls() + FitnessFunction.getPhase1CachedFitnessFunctionCalls();
		int totalFitnessCallsPhase2 = FitnessFunction.getPhase2FitnessFunctionCalls() + FitnessFunction.getPhase2CachedFitnessFunctionCalls();
		int totalFitnessCalls = totalFitnessCallsPhase1 + totalFitnessCallsPhase2 + FitnessFunction.getParentFitnessFunctionCalls();
		System.out.println("Phase1 fitness calls = " + totalFitnessCallsPhase1 + " (new = " + FitnessFunction.getPhase1FitnessFunctionCalls() + ", cached = " + FitnessFunction.getPhase1CachedFitnessFunctionCalls() + ")");
		System.out.println("Phase2 fitness calls = " + totalFitnessCallsPhase2 + " (new = " + FitnessFunction.getPhase2FitnessFunctionCalls() + ", cached = " + FitnessFunction.getPhase2CachedFitnessFunctionCalls() + ")");
		System.out.println("Parent fitness calls = " + FitnessFunction.getParentFitnessFunctionCalls());
		System.out.println("Total number of fitness calls = " + totalFitnessCalls);
		System.out.println("Total fitness function time = " + FitnessFunction.getTotalFitnessFunctionTimeInSec() + "s");
		System.out.println("Average fitness function time = " + (FitnessFunction.getTotalFitnessFunctionTimeInSec() / (double)totalFitnessCalls) + "s");
		System.out.println("Average time for phase 1 = " + (MainIterator.getPhase1Time() / MainIterator.getIterationCount()));
		System.out.println("Average time for phase 2 = " + (MainIterator.getPhase2Time() / MainIterator.getIterationCount()));
		System.out.println("Total time = " + Util.convertNanosecondsToSeconds(endTime - startTime) + "s");


		//output results for this test case in summary file
		if(Constants.RUN_IN_DEBUG_MODE) {
			outputResultsToSummaryFile(ReadInput.getTestFilepath(),
				MainIterator.getOriginalFitness().getInconsistenciesObjective(),
				inconsistenciesObjectiveValueAfter, Util.convertNanosecondsToSeconds(endTime - startTime), mi.getOptimalFitness().getAestheticObjective(),Util.convertNanosecondsToSeconds(MainIterator.getClusteringTime()));
		}

		// output all fitness function graph related information in new csv files
		// Graph 1: Fitness trend graph: x-axis: fitness call no., y-axis: fitness score
		String fitnessTrendGraphPath = outputFolder + File.separatorChar + "fitness-trend-graph.csv";
		File fitnessTrendGraphFile = new File(fitnessTrendGraphPath);
		PrintWriter pw = null;
		try
		{
			pw = new PrintWriter(fitnessTrendGraphFile);
		}
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
		}
		pw.println(GraphPlotting.getHeader());
		
		int overallCnt = 1;
		int clusterCnt = 1;
		String prevClusterId = "";
		for(GraphPlotting gp : Search.getFitnessScoreTrendGraphPhase1())
		{
			if(!gp.getClusterId().equalsIgnoreCase(prevClusterId))
			{
				clusterCnt = 1;
				prevClusterId = gp.getClusterId();
			}
			pw.println(overallCnt + "," + clusterCnt + "," + gp);
			overallCnt++;
			clusterCnt++;
		}
		pw.close();
		
		// Graph 2: Objectives graph: x-axis: inconsistencies, y-axis: aesthetic
		String objectivesGraphPath = outputFolder + File.separatorChar + "objectives-graph.csv";
		File objectivesGraphFile = new File(objectivesGraphPath);
		try
		{
			pw = new PrintWriter(objectivesGraphFile);
		}
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
		}
		pw.println("inconsistenciesObjective,aestheticObjective");
		for(Double[] obj : FitnessFunction.getObjectives())
		{
			pw.println(obj[0] + "," + obj[1]);
		}
		pw.close();
		
		// Graph 3: Fitness graph: x-axis: generations, y-axis: fitness score
		/*try
		{
			String logPath = outputFolder + File.separatorChar + "fitness-over-generations-graph.csv";
			System.setOut(new PrintStream(new FileOutputStream(logPath)));
		}
		catch (Exception e)
		{
			System.err.println("Cannot run in debug mode. All log statements will be displayed on the console.");
		}
		System.out.println("phase,clusterGroup,iterationNumber,generationNumber,fitnessScore");
		count = 1;
		for(Map<String, Double> clusterGenFS : Search.getClusterGenerationFitnessScorePhase1())
		{
			System.out.println("------ Cluster " + count + " -------");
			for(String gen : clusterGenFS.keySet())
			{
				System.out.println(gen + "," + clusterGenFS.get(gen));
			}
			count++;
		}
		System.out.println("\n******* Phase 2: *******");
		for(String gen : Search.getGenerationFitnessScorePhase2().keySet())
		{
			System.out.println(gen + "," + Search.getGenerationFitnessScorePhase2().get(gen));
		}*/
	}

	private void outputResultsToSummaryFile(String testFilepath, int beforeInconsistency, int afterInconsistency, double totalTime, double aestheticObjective, double clusteringTime) {

		File f = new File(Constants.ALL_RESULTS_FILE);

		PrintWriter out = null;
		try {
		if ( f.exists() && !f.isDirectory() ) {
			out = new PrintWriter(new FileOutputStream(f, true));
		}
		else {
			out = new PrintWriter(Constants.ALL_RESULTS_FILE);
		}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String[] retValues = {"", "", ""};	// [0] and [1]: before and after number of inconsistencies, [2]: origin traces
		try
		{
			retValues = getValuesFromlog();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		out.append(testFilepath + ","+beforeInconsistency + ","+afterInconsistency + ","
					+ retValues[0] + ","+retValues[1] + "," + totalTime + "," + retValues[2] + "," + aestheticObjective + "," + clusteringTime + "\n");
		out.close();
	}
	
	private String[] getValuesFromlog() throws IOException
	{
		String[] retValues = {"", "", ""};	// [0] and [1]: before and after number of inconsistencies, [2]: origin traces
		
		String originTraces = "";
		
		BufferedReader in = new BufferedReader(new FileReader(new File(outputFolder + File.separatorChar + "log.txt")));
		String line = "";
		while((line = in.readLine()) != null)
		{
			if(line.startsWith("Number of inconsistencies reported by GWALI = "))
			{
				retValues[0] = (int) Util.getNumbersFromString(line) + "";
			}
			else if(line.startsWith("After number of inconsistencies reported by GWALI = "))
			{
				retValues[1] = (int) Util.getNumbersFromString(line) + "";
			}
			else if(line.contains("Best Chromosome"))
			{
				originTraces = originTraces + "{" + Util.getValueFromRegex("\\{([^\\}]*)\\}", line) + "}, ";
			}
		}
		in.close();
		
		retValues[2] = "\"" + originTraces + "\"";
		
		return retValues;
	}
}
