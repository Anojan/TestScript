package stepDefinition;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.command.CommandFactory;
import org.kie.api.runtime.ExecutionResults;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.runtime.StatelessKnowledgeSession;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.javacodegeeks.drools.DroolsDecisionTableExample;
import com.javacodegeeks.drools.model.Analysis;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.model.HarHeader;
import de.sstoehr.harreader.model.HarQueryParam;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.proxy.CaptureType;

public class AnalyticsTagTester {

	String driverPath = "C:\\Users\\anojans\\Downloads\\chromedriver_win32\\";
	String sFileName = "C:\\Users\\anojans\\Downloads\\SeleniumEasy.har";

	public static WebDriver driver;
	public static BrowserMobProxy proxy;
	private static StatelessKnowledgeSession session;
	String url = "http://www.asiaone.com//?adbypass=skinning_topspecial_topoverlay";

	@Given("^I go to straitstimes Home Page$")
	public void straitstimes_Home_Page() throws Throwable {

		// start theProxy
		proxy = new BrowserMobProxyServer();
		proxy.start(0);

		// get the SeleniumProxy object - org.openqa.selenium.Proxy;
		Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);

		// configure it as a desired capability
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);

		// set chromeDriver system property
		System.setProperty("webdriver.chrome.driver", driverPath + "chromedriver.exe");
		driver = new ChromeDriver(capabilities);

		// enable more detailed HAR capture, if desired (see CaptureType for the
		// complete list)
		proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);

		// create a new HAR with the label "seleniumeasy.com"
		proxy.newHar("straitstimes.com");
		driver.get(url);
		Thread.sleep(10);
	}

	@When("^I get the values from website$")
	public void get_values_from_website() throws Throwable {

		// get the HAR data
		Har har = proxy.getHar();

		List<HarEntry> harEntries = proxy.getHar().getLog().getEntries();
		Set<String> requestedUrls = new HashSet<String>();
		for (HarEntry harEntry : harEntries) {
			requestedUrls.add(harEntry.getRequest().getUrl());
		}
		// Write HAR Data in a File
		File harFile = new File(sFileName);
		try {
			har.writeTo(harFile);
		} catch (IOException ex) {
			System.out.println(ex.toString());
			System.out.println("Could not find file " + sFileName);
		}

		if (driver != null) {
			proxy.stop();
			driver.quit();
		}
	}

	@And("^I compare the values with har entries$")
	public void logRequest() throws Exception {

		Analysis a = new Analysis();
		a.setUrl(url);
		a.setTags(getParameterPairs());

		KnowledgeBase knowledgeBase = DroolsDecisionTableExample.createKnowledgeBaseFromSpreadsheet();
		session = knowledgeBase.newStatelessKnowledgeSession();
		session.execute(a);
		
//		ExecutionResults bresults =
//				session.execute( CommandFactory.newInsert( a, "result" ) );
//				Stilton stilton = bresults.getValue( "result" );
		
		
		
		

	}

	@Then("^I see the values are available as the same$")
	public void verify_values_available() throws Throwable {

	}

	private static Map<String, String> getParameterPairs() throws Exception {
		Map<String, String> paramPairs = new HashMap<String, String>();
		System.out.println("at logRequest");

		// FileOutputStream fos = new
		// FileOutputStream("C:\\Users\\anojans\\Downloads\\SeleniumEasy.har");
		HarReader harReader = new HarReader();
		de.sstoehr.harreader.model.Har har = harReader
				.readFromFile(new File("C:\\Users\\anojans\\Downloads\\SeleniumEasy.har"));

		de.sstoehr.harreader.model.HarLog log = har.getLog();
		List<de.sstoehr.harreader.model.HarEntry> entries = log.getEntries();

		URI uri = null;
		for (de.sstoehr.harreader.model.HarEntry entry : entries) {
			List<HarQueryParam> params = entry.getRequest().getQueryString();

			for (HarQueryParam harQueryParam : params) {
				// if (harQueryParam.getName().equals("debug") ||
				// harQueryParam.getName().equals("dbm_c") ||
				// harQueryParam.getName().equals("anId") ||
				// harQueryParam.getName().equals("creator"))

				if (harQueryParam.getName().equals("vtag"))
					break;

			}
			URL url = new URL(entry.getRequest().getUrl());
		      String nullFragment = null;
		       uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), nullFragment);
			
			if (uri.getHost().contains("logw348.ati-host")) {
				System.out.println(url);

				System.out.println("Request found!");
				List<HarHeader> headers = entry.getRequest().getHeaders();
				System.out.println("Headers : ");
				for (HarHeader pair : headers) {
					System.out.println(pair.getName() + ":" + pair.getValue());
				}

				// System.out.println("status : " +
				// entry.getResponse().getStatus());
				// System.out.println(entry.getRequest().getUrl());
				System.out.println("Parameters : ");
				List<HarQueryParam> parameters = entry.getRequest().getQueryString();
				for (HarQueryParam pair : parameters) {
					paramPairs.put(pair.getName(), pair.getValue());
					System.out.println(pair.getName() + " : " + pair.getValue());
				}
			}
		}
		return paramPairs;
	}

}
