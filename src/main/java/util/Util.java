package util;

import edu.gatech.xpert.dom.DomNode;
import edu.usc.cluster.ElementWrapper;
import edu.usc.util.Utils;
import ifix.approach1.MainIterator;
import ifix.fitness.FixTuple;
import ifix.input.ReadInput;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.w3c.dom.Node;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Util 
{
	
	public static Map<String, ElementWrapper> xpathToElementMap = new HashMap<>();
	
	public static double getRandomDoubleValueInRange(double min, double max)
	{
		if(max - min <= 0)
			return min;
		
		Random r = new Random();
		return min + (max - min) * r.nextDouble();
	}
	
	public static int getRandomIntValueInRange(int min, int max)
	{
		if(max - min <= 0)
			return min;
		
		Random generator = new Random();
		return generator.nextInt(max - min) + min;
	}





	public static void applyChangesToPage(List<FixTuple> tuples, boolean isDebug)
	{			
		String javascriptCode = "";
		for(FixTuple ft : tuples)
		{
			// modify test page with the new values
			String originalValueString = getOriginalValue(ft.getXpath(), ft.getCssProperty());
			double delta = ft.getchangeValue() * Constants.CHANGE_FACTORS.get(ft.getCssProperty());
			String value = computeValueWithDelta(originalValueString, delta);
			ft.setOriginalValue(originalValueString);
			//ft.setNewValue(value);
			if(isDebug)
			{
				System.out.println("Applying " + ft.getCssProperty() + " = " + value + " to xpath = " + ft.getXpath() + " (original value = " + originalValueString + ")\t");
			}
			javascriptCode+="document.evaluate(\""+ft.getXpath()+"\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.style[\"" + ft.getCssProperty() + "\"] = \"" + value +"\";";
			
		}
		
		WebDriver d = ReadInput.getTestDriver();
		((JavascriptExecutor) d).executeScript(javascriptCode);
	}
	

	public static String getOriginalValue(String xpath, String prop)
	{
		String val = MainIterator.getOriginalCSSValuesCache().get(xpath+prop);
		if(val == null){
			WebElement e = ReadInput.getTestDriver().findElement(By.xpath(xpath));
			val = e.getCssValue(prop);
			if( val.equalsIgnoreCase("auto"))
			{
				if(prop.equalsIgnoreCase("width"))
					val = e.getSize().width + "px";
				else if(prop.equalsIgnoreCase("height"))
					val = e.getSize().height + "px";
			}
			MainIterator.getOriginalCSSValuesCache().put(xpath+prop, val);
		}
		return val;
	}
	
	public static String computeValue(String originalValueString, double degreeOfChange)
	{
		double originalValue = getNumbersFromString(originalValueString);
		String unit = Util.getUnitFromStringValue(originalValueString);
		
		// get new value from original value and degree of change
		double newValue = originalValue;
		
		// check if degreeOfChange is within the range of [-1, +1)
		if(degreeOfChange >= -1.0 && degreeOfChange < 1.0)
		{
			// +ve: degree of change > 0.0: New value = original / (1 - degree of change)
			if(degreeOfChange > 0.0)
			{
				if(Math.abs(1-degreeOfChange) > 0.0)	// to avoid divide by zero error
					newValue = newValue / (1-degreeOfChange);
			}
			// -ve: degree of change <= 0.0: New value = original * (1 - |degree of change|)
			else
			{
				newValue = newValue * (1-Math.abs(degreeOfChange));
			}
		}
		return Util.round(newValue) + unit;
	}
	
	public static int computeChangeValue(String originalValueString, String newValueString )
	{
		int originalValue = (int) getNumbersFromString(originalValueString);
		int newValue = (int) getNumbersFromString(newValueString);

		// change Value is the diff between the new value and the original value
		int changeValue = newValue - originalValue;
		
		return changeValue;
	}


	public static String computeValueWithDelta(String originalValueString, double delta)
	{
		double originalValue = getNumbersFromString(originalValueString);
		String unit = Util.getUnitFromStringValue(originalValueString);

		// get new value from original value and degree of change
		double newValue = originalValue + delta;

		return Util.round(newValue) + unit;
	}


	public static double getNumbersFromString(String string)
	{
		Pattern p = Pattern.compile("(\\d+(?:\\.\\d+)?)");
		Matcher m = p.matcher(string);
		if (m.find())
		{
			return Double.valueOf(m.group(1));
		}
		return 0;
	}
	
	public static String getUnitFromStringValue(String string)
	{
		Pattern p = Pattern.compile("[a-zA-Z%]+");
		Matcher m = p.matcher(string);
		String returnValue = "";
		while (m.find())
		{
			returnValue = m.group();
		}
		return returnValue;
	}
	
	public static double getWeightedAverage(double value1, double value2, double weight)
	{
		return ((weight*value1) + ((1-weight)*value2)); 
	}
	
	public static double round(double value) 
	{
		int places = 3;
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	public static double convertNanosecondsToSeconds(long time)
	{
		return round((double) time / 1000000000.0);
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map)
	{
		Map<K, V> result = new LinkedHashMap<>();
		Stream<Map.Entry<K, V>> st = map.entrySet().stream();

		st.sorted(Map.Entry.comparingByValue()).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));

		return result;
	}
	
	public static String getValueFromRegex(String regex, String str)
	{
		Pattern p = Pattern.compile(regex, Pattern.DOTALL);
		Matcher m = p.matcher(str);
		if (m.find())
		{
			return m.group(1);
		}
		return null;
	}
	
	private static int getSiblingIndex(String xPathElement)
	{
		String value = getValueFromRegex("\\[(.+)\\]", xPathElement);
		if (value == null)
			return -1;
		return Integer.parseInt(value);
	}

	private static String getElementId(String xPathElement)
	{
		return getValueFromRegex("\\*\\[@id=['|\"]?(.+[^'\"])['|\"]?\\]", xPathElement);
	}
	
	public static org.w3c.dom.Element getW3CElementFromXPathJava(String xPath, org.w3c.dom.Document doc) throws IOException
	{
		String xPathArray[] = xPath.split("/");
		ArrayList<String> xPathList = new ArrayList<String>();

		for (int i = 0; i < xPathArray.length; i++)
		{
			if (!xPathArray[i].isEmpty())
			{
				xPathList.add(xPathArray[i]);
			}
		}

		org.w3c.dom.Element foundElement = null;
		org.w3c.dom.NodeList elements;
		int startIndex = 0;

		String id = getElementId(xPathList.get(0));
		if (id != null && !id.isEmpty())
		{
			foundElement = doc.getElementById(id);
			if (foundElement == null)
				return null;
			elements = foundElement.getChildNodes();
			startIndex = 1;
		}
		else
		{
			elements = doc.getElementsByTagName(xPathList.get(0).replaceFirst("\\[(.+)\\]", ""));
		}
		for (int i = startIndex; i < xPathList.size(); i++)
		{
			String xPathFragment = xPathList.get(i);
			int index = getSiblingIndex(xPathFragment);
			boolean found = false;

			// strip off sibling index in square brackets
			xPathFragment = xPathFragment.replaceFirst("\\[(.+)\\]", "");

			for (int j = 0; j < elements.getLength(); j++)
			{
				if (elements.item(j).getNodeType() != Node.ELEMENT_NODE)
				{
					continue;
				}

				org.w3c.dom.Element element = (org.w3c.dom.Element) elements.item(j);

				if (found == false && xPathFragment.equalsIgnoreCase(element.getTagName()))
				{
					// check if sibling index present
					if (index > 1)
					{
						int siblingCount = 0;

						for (org.w3c.dom.Node siblingNode = element.getParentNode().getFirstChild(); siblingNode != null; siblingNode = siblingNode.getNextSibling())
						{
							if (siblingNode.getNodeType() != Node.ELEMENT_NODE)
							{
								continue;
							}

							org.w3c.dom.Element siblingElement = (org.w3c.dom.Element) siblingNode;
							if ((siblingElement.getTagName().equalsIgnoreCase(xPathFragment)))
							{
								siblingCount++;
								if (index == siblingCount)
								{
									foundElement = siblingElement;
									found = true;
									break;
								}
							}
						}
						// invalid element (sibling index does not exist)
						if (found == false)
							return null;
					}
					else
					{
						foundElement = element;
						found = true;
					}
					break;
				}
			}

			// element not found
			if (found == false)
			{
				return null;
			}

			elements = foundElement.getChildNodes();
		}
		return foundElement;
	}
	
	public static String getXPathOfElementJava(Element element)
	{
		/*
		 * if(element != null && !element.id().isEmpty()) { return "//*[@id=\""
		 * + element.id() + "\"]"; } else {
		 */
		return getElementTreeXPathJava(element);
		// }
	}

	private static String getElementTreeXPathJava(Element element)
	{
		ArrayList<String> paths = new ArrayList<String>();
		for (; element != null && !element.tagName().equals("#root"); element = element.parent())
		{
			int index = 0;
			/*
			 * if(!element.id().isEmpty()) { paths.add("/*[@id=\"" +
			 * element.id() + "\"]"); break; }
			 */

			for (Element sibling = element.previousElementSibling(); sibling != null && !sibling.tagName().equals("#root"); sibling = sibling.previousElementSibling())
			{
				if (sibling.tagName().equals(element.tagName()))
				{
					++index;
				}
			}
			String tagName = element.tagName().toLowerCase();
			String pathIndex = "[" + (index + 1) + "]";
			paths.add(tagName + pathIndex);
		}

		String result = null;
		if (paths.size() > 0)
		{
			result = "/";
			for (int i = paths.size() - 1; i > 0; i--)
			{
				result = result + paths.get(i) + "/";
			}
			result = result + paths.get(0);
		}

		return result;
	}
	
	public static void takeScreenShot(FirefoxDriver driver, String distPath){
		
		File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
		// Now you can do whatever you need to do with it, for example copy somewhere
		try
		{
			FileUtils.moveFile(scrFile, new File(distPath));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		// take screenshot for newer versions of firefox
		/*Screenshot ashotScreenshot = new AShot()
		  .shootingStrategy(ShootingStrategies.viewportPasting(100))
		  .takeScreenshot(driver);
		BufferedImage img = ashotScreenshot.getImage();
		File screenshotFile = null;
		try
		{
			screenshotFile = new File(distPath);
			ImageIO.write(img, "png", screenshotFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		// reload the page to eliminate any effects of scrolling
		String code = "window.scrollTo(0, 0)";
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript(code);*/
	}
	
	public static double sigmoid(double x)
	{
		return ((Math.atan(x) + Math.PI/2.0) / Math.PI);
	}
	
	public static Map<String, List<Rectangle>> getClusterRectangles(Map<String, List<DomNode>> clusters)
	{
		Map<String, List<Rectangle>> clusterRectangles = new HashMap<String, List<Rectangle>>();
		for(String cId : clusters.keySet())
		{
			List<DomNode> c = clusters.get(cId);
			int minX, minY, maxX, maxY;
			minX = minY = Integer.MAX_VALUE;
			maxX = maxY = Integer.MIN_VALUE;
			for(DomNode node : c)
			{
				minX = (int) Math.min(node.getCoords()[0], minX);
				minY = (int) Math.min(node.getCoords()[1], minY);
				maxX = (int) Math.max(node.getCoords()[2], maxX);
				maxY = (int) Math.max(node.getCoords()[3], maxY);
			}
			Rectangle r = new Rectangle();
			r.x = minX; r.y = minY; r.width = (maxX - minX); r.height = (maxY - minY);
			
			List<Rectangle> clusterMembers = new ArrayList<>();
			clusterMembers.add(r);
			
			for(DomNode node : c)
			{
				Rectangle member = new Rectangle((int)node.getCoords()[0], (int)node.getCoords()[1], 
					((int)node.getCoords()[2] - (int)node.getCoords()[0]), ((int)node.getCoords()[3] - (int)node.getCoords()[1]));
				clusterMembers.add(member);
			}
			clusterRectangles.put(cId, clusterMembers);
		}
		return clusterRectangles;
	}
	
	public static Color hex2Rgb(String colorStr) {
	    return new Color(
	            Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
	            Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
	            Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
	}
	
	private static Color getRandomColor(List<String> visitedColors)
	{
		Random rand = new Random();
		int cnt = 0;
		while(cnt < 50)
		{
			int r = rand.nextInt(255);
			int g = rand.nextInt(255);
			int b = rand.nextInt(255);
			Color randomColor = new Color(r, g, b);
			String color = String.format("#%02x%02x%02x", r, g, b);
			if(!visitedColors.contains(color))
			{
				visitedColors.add(color);
				return randomColor;
			}
			cnt++;
		}
		return hex2Rgb(visitedColors.get(0));
	}
	
	public static void drawClusters(String imagePath, Map<String, List<Rectangle>> clusterRectangles) throws IOException
	{
		List<String> visitedColors = new ArrayList<String>();
		
		// clusterRectangles first rect: outermost cluster rect
		// other rectangles: cluster elements
		
		BufferedImage bi = ImageIO.read(new File(imagePath));

		Random rand = new Random();
		for (String cId : clusterRectangles.keySet())
		{
			List<Rectangle> rects = clusterRectangles.get(cId);
			if(rects.size() == 0)
				continue;
			
			Graphics graphics = bi.getGraphics();
			graphics.setColor(Color.RED);
			graphics.setFont(new Font("Arial Black", Font.BOLD, 14));
			
			Rectangle rect = rects.get(0);
			int x = rect.x + 10 + rand.nextInt(11);
			int y = rect.y + 10 + rand.nextInt(11);
			graphics.drawString(cId, x, y);

			Graphics2D g2D = (Graphics2D) graphics;
			Color color = getRandomColor(visitedColors);
			g2D.setColor(color);
			g2D.setStroke(new BasicStroke(3F));
			g2D.drawRect(rect.x, rect.y, rect.width, rect.height);

			if(rects.size() > 1)
			{
				// draw dashed rectangles around individual cluster elements
				float dash[] = { 10.0f };
				g2D.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
				int alpha = 50;
				Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
				g2D.setPaint(c);
				for(int m = 1; m < rects.size(); m++)
				{
					g2D.fill(rects.get(m));
				}
			}
		}
		ImageIO.write(bi, "png", new File(imagePath));
	}
	
	public static Object getKeyFromValue(Map<?, ?> hm, Object value)
	{
		for (Object o : hm.keySet())
		{
			if (hm.get(o).equals(value))
			{
				return o;
			}
		}
		return null;
	}
	
	public static List<List<String>> getDependencies(ArrayList<String[]> elements)
	{
		List<List<String>> fullListOfDependentElements = new ArrayList<>();
		List<Integer> processedIndices = new ArrayList<>();
		for (int i = 0; i < elements.size(); i++)
		{
			if(processedIndices.contains(i))
				continue;
			
			String[] e = elements.get(i);
			List<String> list = new ArrayList<>();
			list.add(e[0]);
			if(!e[0].equalsIgnoreCase(e[1]))
			{
				list.add(e[1]);
			}
			processedIndices.add(i);
			boolean isNewElement = false;
			do
			{
				isNewElement = false;
				for (int j = 0; j < elements.size(); j++)
				{
					if(processedIndices.contains(j))
						continue;
					
					String[] nextE = elements.get(j);
					if(list.contains(nextE[0]) && !list.contains(nextE[1]))
					{
						list.add(nextE[1]);
						processedIndices.add(j);
						isNewElement = true;
					}
					else if(!list.contains(nextE[0]) && list.contains(nextE[1]))
					{
						list.add(nextE[0]);
						processedIndices.add(j);
						isNewElement = true;
					}
					else if(list.contains(nextE[0]) && list.contains(nextE[1]))
					{
						processedIndices.add(j);
						isNewElement = false;
					}
					else
					{
						isNewElement = false;
					}
				}
			} while(isNewElement);
			fullListOfDependentElements.add(list);
		}
		return fullListOfDependentElements;
	}	
	
	public static void restartFirefoxDrivers(){
		
		FirefoxDriver testDriver = ReadInput.getTestDriver();
		/*testDriver.get("about:memory");
		testDriver.findElementsByTagName("button").get(6).click();*/


		ReadInput.getRefDriver().quit();
		ReadInput.getTestDriver().quit();
		
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		testDriver = Utils.getNewFirefoxDriver();
		testDriver.get("file:///" + ReadInput.getTestFilepath());
		ReadInput.setTestDriver(testDriver);
		
		FirefoxDriver refDriver = Utils.getNewFirefoxDriver();
		refDriver.get("file:///" + ReadInput.getRefFilepath());
		ReadInput.setRefDriver(refDriver);
		
		
	}

	public static void resetPageToOriginalState() {
		WebDriver d = ReadInput.getTestDriver();
		d.get("file:///" + ReadInput.getTestFilepath());
	}

	public static String getFirstHTMLParentXPath(String xpath) {
		while(xpath.contains(":")) {
			int lastSlashIdx = xpath.lastIndexOf("/");
			xpath = xpath.substring(0, lastSlashIdx);
		}
		return xpath;
	}
	
}
