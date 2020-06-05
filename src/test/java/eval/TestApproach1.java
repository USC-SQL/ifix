package eval;

import edu.usc.config.Config;
import edu.usc.gwali.Gwali;
import edu.usc.util.Utils;
import ifix.approach1.Approach1;
import ifix.input.ReadInput;
import util.Constants;
import util.Util;

import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TestApproach1
{
	public static void main(String[] args)
	{

		Config.applyConfig();

		String basepath = "../ifix-journal-subjects";

		List<Subject> subjects = new ArrayList<Subject>();

		subjects.add(new Subject("hotwire", basepath));
		subjects.add(new Subject("westin", basepath));
		subjects.add(new Subject("hightail", basepath));
		subjects.add(new Subject("akamai", basepath));
		subjects.add(new Subject("caLottery", basepath));
		subjects.add(new Subject("designSponge", basepath));
		subjects.add(new Subject("dmv", basepath));
		subjects.add(new Subject("els", basepath));
		subjects.add(new Subject("facebookLogin", basepath));
		subjects.add(new Subject("flynas", basepath));
		subjects.add(new Subject("googleEarth", basepath));
		subjects.add(new Subject("googleLogin", basepath));
		subjects.add(new Subject("ixigo", basepath));
		subjects.add(new Subject("linkedin", basepath));
		subjects.add(new Subject("museum", basepath));
		subjects.add(new Subject("mplay", basepath));
		subjects.add(new Subject("qualitrol", basepath));
		subjects.add(new Subject("rentalCars", basepath));
		subjects.add(new Subject("skype", basepath));
		subjects.add(new Subject("skyScanner", basepath));
		subjects.add(new Subject("doctor", basepath));
		subjects.add(new Subject("twitterHelp", basepath));
		subjects.add(new Subject("worldsBest", basepath));

		subjects.add(new Subject("deptOfEducation", basepath));
		subjects.add(new Subject("essex", basepath));
		subjects.add(new Subject("marionCounty", basepath));
		subjects.add(new Subject("namibia", basepath));
		subjects.add(new Subject("nashville", basepath));
		subjects.add(new Subject("nevadaGoverner", basepath));
		subjects.add(new Subject("princeGeorgeCity", basepath));
		subjects.add(new Subject("puteaux", basepath));
		subjects.add(new Subject("saintMalo", basepath));
		subjects.add(new Subject("thunderbird", basepath));
		subjects.add(new Subject("visitSweden", basepath));
		subjects.add(new Subject("weva", basepath));
		subjects.add(new Subject("familySearch", basepath));
		subjects.add(new Subject("limburg", basepath));
		subjects.add(new Subject("skyteam", basepath));
		subjects.add(new Subject("worldCustoms", basepath));
		subjects.add(new Subject("bookCrossing", basepath));
		subjects.add(new Subject("hattrick", basepath));
		subjects.add(new Subject("googleGroups", basepath));
		subjects.add(new Subject("ValleedAoste", basepath));
		subjects.add(new Subject("ashikagaCity", basepath));
		subjects.add(new Subject("portland", basepath));
		subjects.add(new Subject("denham", basepath));
		subjects.add(new Subject("hauteLoire", basepath));
		subjects.add(new Subject("sbc", basepath));
		subjects.add(new Subject("hachijojima", basepath));
		subjects.add(new Subject("geneva", basepath));


		int numberOfRuns = 1;

		writeSummaryFileHeader();


		for(Subject subject : subjects)
		{
			for(int i = 0; i < numberOfRuns; i++)
			{

				FirefoxDriver refDriver = Utils.getNewFirefoxDriver();
				refDriver.get("file:///" + subject.getRefFilepathRelFromBasepath());

				FirefoxDriver testDriver = Utils.getNewFirefoxDriver();
				testDriver.get("file:///" + subject.getTestFilepathRelFromBasepath());

				ReadInput.setRefDriver(refDriver);
				ReadInput.setTestDriver(testDriver);
				ReadInput.setRefFilepath(subject.getRefFilepathRelFromBasepath());
				ReadInput.setTestFilepath(subject.getTestFilepathRelFromBasepath());
				ReadInput.setSubjectName(subject.getSubject());


				Approach1 approach1 = new Approach1();
				approach1.runApproach();


				refDriver.close();
				testDriver.close();
				refDriver.quit();
				testDriver.quit();



			}
		}
	}

	private static void writeSummaryFileHeader() {
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
		out.append("subject,beforeInconsistency,afterInconsistency,beforeNumberOfInconsistencies,afterNumberOfInconsistencies,totalTime,originTraces,aestheticObjective,clustering\n");
		out.close();
	}
}
