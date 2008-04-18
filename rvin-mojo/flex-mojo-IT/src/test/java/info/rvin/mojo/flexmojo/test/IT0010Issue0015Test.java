package info.rvin.mojo.flexmojo.test;

import java.io.File;

import org.apache.maven.integrationtests.AbstractMavenIntegrationTestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class IT0010Issue0015Test extends AbstractMavenIntegrationTestCase {

	public void testFailOnErrors() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0015");
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
		verifier.deleteArtifact("info.rvin.itest.issues", "issue-0015",
				"1.0-SNAPSHOT", "swf");
		verifier.executeGoal("install");
		verifier.displayStreamBuffers();
		try {
			verifier.verifyErrorFreeLog();
			fail("testing error unit, must fail!");
		} catch (Exception e) {
		}
		verifier.resetStreams();
		
		File reportDir = new File(testDir, "target/test-reports");
		assertEquals(2, reportDir.listFiles().length);
	}

}
