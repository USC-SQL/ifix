package eval;

import java.io.File;

public class Subject 
{
	private String subject;
	private String basepath;
	private String refFilepathRelFromBasepath;
	private String testFilepathRelFromBasepath;
	
	public Subject(String subject, String basepath, String refFilepathRelFromBasepath, String testFilepathRelFromBasepath) {
		this.subject = subject;
		this.basepath = basepath;
		
		if(refFilepathRelFromBasepath.isEmpty())
			this.refFilepathRelFromBasepath = basepath + File.separatorChar + subject+"-ref" + File.separatorChar + "index.html";
		else
			this.refFilepathRelFromBasepath = basepath + File.separatorChar + subject+"-ref" + File.separatorChar + refFilepathRelFromBasepath;
		
		if(testFilepathRelFromBasepath.isEmpty())
			this.testFilepathRelFromBasepath = basepath + File.separatorChar + subject+"-test" + File.separatorChar + "index.html";
		else
			this.testFilepathRelFromBasepath = basepath + File.separatorChar + subject+"-test" + File.separatorChar + testFilepathRelFromBasepath;
	}
	
	public Subject(String subject, String basepath) {
		this.subject = subject;
		this.basepath = basepath;
		this.refFilepathRelFromBasepath = basepath + File.separatorChar + subject+"-ref" + File.separatorChar + "index.html";
		this.testFilepathRelFromBasepath = basepath + File.separatorChar + subject+"-test" + File.separatorChar + "index.html";
	}

	
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBasepath() {
		return basepath;
	}
	public void setBasepath(String basepath) {
		this.basepath = basepath;
	}
	public String getRefFilepathRelFromBasepath() {
		return refFilepathRelFromBasepath;
	}
	public void setRefFilepathRelFromBasepath(String refFilepathRelFromBasepath) {
		this.refFilepathRelFromBasepath = refFilepathRelFromBasepath;
	}
	public String getTestFilepathRelFromBasepath() {
		return testFilepathRelFromBasepath;
	}
	public void setTestFilepathRelFromBasepath(String testFilepathRelFromBasepath) {
		this.testFilepathRelFromBasepath = testFilepathRelFromBasepath;
	}
	
}
