package ifix.approach1;

import edu.usc.config.Config;
import edu.usc.util.Utils;
import ifix.input.ReadInput;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Main {
    public static void main(String [] args){

    	String refPath = args[0];
    	String testPath = args[1];

        Config.applyConfig();
        FirefoxDriver refDriver = Utils.getNewFirefoxDriver();
        refDriver.get("file://" + refPath);

        FirefoxDriver testDriver = Utils.getNewFirefoxDriver();
        testDriver.get("file://" + testPath);

        ReadInput.setRefDriver(refDriver);
        ReadInput.setTestDriver(testDriver);
        ReadInput.setRefFilepath(refPath);
        ReadInput.setTestFilepath(testPath);

        Approach1 approach1 = new Approach1();
        approach1.runApproach();

        refDriver.close();
        testDriver.close();
        refDriver.quit();
        testDriver.quit();


    }
}
