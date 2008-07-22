package info.rvin.mojo.flexmojo.test;

import static info.flexmojos.it.MavenVerifierHelper.customTester;
import info.flexmojos.it.MavenVerifierHelper;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.integrationtests.AbstractMavenIntegrationTestCase;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.util.ResourceExtractor;
import org.codehaus.plexus.util.IOUtil;

public class IT0013IssuesTest extends AbstractMavenIntegrationTestCase {

	public static void standardIssueTester(String issueNumber) throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(
				MavenVerifierHelper.class, "/issues/" + issueNumber);
		customTester(testDir, "info.rvin.itest.issues", issueNumber,
				"1.0-SNAPSHOT", "swf", "install");
	}

	public void testIssue8() throws Exception {
		standardIssueTester("issue-0008-1");
		standardIssueTester("issue-0008-2");
	}

	public void testIssue11() throws Exception {
		standardIssueTester("issue-0011");
	}

	public void testIssue13() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0013");
		customTester(testDir, "info.rvin.itest.issues", "issue-0013",
				"1.0-SNAPSHOT", "swf", "install");

		File reportDir = new File(testDir, "target/surefire-reports");
		assertEquals(2, reportDir.listFiles().length);
	}

	public void testIssue14() throws Exception {
		try {
			standardIssueTester("issue-0014");
			fail("This test must throw errors");
		} catch (VerificationException e) {
			// expected exception
			// System.out.println("Got required Fail =D");
		}
	}

	public void testIssue15() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0015");
		try {
			customTester(testDir, "info.rvin.itest.issues", "issue-0015",
					"1.0-SNAPSHOT", "swf", "install");
			fail("testing error unit, must fail!");
		} catch (Exception e) {
			// expected exception
		}

		File reportDir = new File(testDir, "target/surefire-reports");
		assertEquals(2, reportDir.listFiles().length);
	}

	public void testIssue17() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0017");
		customTester(testDir, "info.rvin.itest.issues", "issue-0017",
				"1.0-SNAPSHOT", "swf", "asdoc:asdoc");

		File asdoc = new File(testDir, "target/asdoc");
		assertTrue("asdoc directory must exist", asdoc.isDirectory());
	}

	public void testIssue27() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0027");
		try {
			customTester(testDir, "info.rvin.itest.issues", "issue-0027",
					"1.0-SNAPSHOT", "swf", "asdoc:asdoc");
			fail("No source code.  Should throw an error!");
		} catch (Exception e) {
			// expected exception
		}
	}

	public void testIssue29() throws Exception {
		standardIssueTester("issue-0029");
	}

	public void testIssue32() throws Exception {
		standardIssueTester("issue-0032");
	}

	public void testIssue39() throws Exception {
		standardIssueTester("issue-0039");
	}

	// A wierd but on this tests
	// public void testIssue43() throws Exception {
	// File testDir = ResourceExtractor.simpleExtractResources(getClass(),
	// "/issues/issue-0014");
	// List<String> args = new ArrayList<String>();
	// args.add("-Dmaven.test.failure.ignore=true");
	// customTester(testDir, "info.rvin.itest.issues", "issue-0014",
	// "1.0-SNAPSHOT", "swf", "install", args);
	// }

	public void testIssue44() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0044");
		try {
			customTester(testDir, "info.rvin.itest.issues", "issue-0044",
					"1.0-SNAPSHOT", "swf", "asdoc:asdoc");
			fail("testing error unit, must fail!");
		} catch (Exception e) {
			// expected exception
		}
	}

	public void testIssue53() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0014");
		List<String> args = new ArrayList<String>();
		args.add("-Dmaven.test.skip=true");
		customTester(testDir, "info.rvin.itest.issues", "issue-0014",
				"1.0-SNAPSHOT", "swf", "install", args);

		args = new ArrayList<String>();
		args.add("-DskipTests=true");
		customTester(testDir, "info.rvin.itest.issues", "issue-0014",
				"1.0-SNAPSHOT", "swf", "install", args);
	}

	public void testIssue61() throws Exception {
		standardIssueTester("issue-0061");
	}

	public void testIssue66() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
		"/issues/issue-0066");
		standardIssueTester("issue-0066");

		//Issue 62 test
		File another = new File(testDir, "flex/src/main/flex/info/flexmojos/generator/AnotherPojo.as");
		assertFalse(another.exists());

		//Issue 65 test
		File pojo = new File(testDir, "flex/src/main/flex/info/flexmojos/generator/SimplePojo.as");
		assertTrue(pojo.exists());
		File base = new File(testDir, "flex/target/generated-sources/flex-mojos/info/flexmojos/generator/SimplePojoBase.as");
		assertTrue(base.exists());
	}

	public void testIssue67() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0067");
		customTester(testDir, "info.rvin.itest.issues", "issue-0067",
				"1.0-SNAPSHOT", "swf", "asdoc:asdoc");
	}

	public void testIssue68() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0068");
		customTester(testDir, "info.rvin.itest.issues", "issue-0068",
				"1.0-SNAPSHOT", "swf", "asdoc:asdoc");
	}

	public void testIssue69() throws Exception {
		final String[] trusts = new String[] {
				"AppData/Roaming/Macromedia/Flash Player/#Security/FlashPlayerTrust",
				"Application Data/Macromedia/Flash Player/#Security/FlashPlayerTrust",
				".macromedia/Flash_Player/#Security/FlashPlayerTrust",
				"Library/Preferences/Macromedia/Flash Player/#Security/FlashPlayerTrust" };

		File userHome = new File(System.getProperty("user.home"));

		File mavenCfg = null;
		for (String folder : trusts) {
			File fpTrustFolder = new File(userHome, folder);
			if (fpTrustFolder.exists() && fpTrustFolder.isDirectory()) {
				mavenCfg = new File(fpTrustFolder, "maven.cfg");
				if (mavenCfg.exists()) {
					mavenCfg.delete();
				}
				break;
			}
		}

		standardIssueTester("issue-0069");

		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0069");

		assertTrue("Flex-mojos should generate maven.cfg: "
				+ mavenCfg.getAbsolutePath(), mavenCfg.exists());

		String cfg = IOUtil.toString(new FileReader(mavenCfg));

		assertTrue("Flex-mojos should write trust localtion", cfg
				.contains(testDir.getAbsolutePath()));
	}

	public void testIssue70() throws Exception {
		standardIssueTester("issue-0070");
	}

}
