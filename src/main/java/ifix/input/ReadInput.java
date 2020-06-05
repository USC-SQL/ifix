package ifix.input;

import org.openqa.selenium.firefox.FirefoxDriver;

public class ReadInput 
{
	private static FirefoxDriver refDriver;
	private static FirefoxDriver testDriver;
	private static String refFilepath;
	private static String testFilepath;
	private static String refScreenshotPath;
	private static String testScreenshotBeforePath;
	private static String testScreenshotAfterPath;
	private static String outputFolderPath;
	
	private static String subjectName;

	
	public static FirefoxDriver getRefDriver()
	{
		return refDriver;
	}
	public static void setRefDriver(FirefoxDriver refDriver)
	{
		ReadInput.refDriver = refDriver;
	}
	public static FirefoxDriver getTestDriver()
	{
		return ReadInput.testDriver;
	}
	public static void setTestDriver(FirefoxDriver testDriver)
	{
		ReadInput.testDriver = testDriver;
	}
	public static String getRefFilepath()
	{
		return refFilepath;
	}
	public static void setRefFilepath(String refFilepath)
	{
		ReadInput.refFilepath = refFilepath;
	}
	public static String getTestFilepath()
	{
		return testFilepath;
	}
	public static void setTestFilepath(String testFilepath)
	{
		ReadInput.testFilepath = testFilepath;
	}
	public static String getRefScreenshotPath()
	{
		return refScreenshotPath;
	}
	public static void setRefScreenshotPath(String refScreenshotPath)
	{
		ReadInput.refScreenshotPath = refScreenshotPath;
	}
	public static String getTestScreenshotBeforePath()
	{
		return testScreenshotBeforePath;
	}
	public static void setTestScreenshotBeforePath(String testScreenshotBeforePath)
	{
		ReadInput.testScreenshotBeforePath = testScreenshotBeforePath;
	}
	public static String getTestScreenshotAfterPath()
	{
		return testScreenshotAfterPath;
	}
	public static void setTestScreenshotAfterPath(String testScreenshotAfterPath)
	{
		ReadInput.testScreenshotAfterPath = testScreenshotAfterPath;
	}
	public static String getOutputFolderPath() 
	{
		return outputFolderPath;
	}
	public static void setOutputFolderPath(String outputFolderPath) 
	{
		ReadInput.outputFolderPath = outputFolderPath;
	}
	public static String getSubjectName() {
		return subjectName;
	}
	public static void setSubjectName(String subjectName) {
		ReadInput.subjectName = subjectName;
	}
}
