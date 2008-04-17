package info.rvin.mojo.flexmojo.test;

import java.io.File;

import org.apache.maven.integrationtests.AbstractMavenIntegrationTestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class IT0007Issue0013Test extends AbstractMavenIntegrationTestCase {
	
	public void testWithoutTestFolder() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
		"/issues/issue-0013");
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
		verifier.deleteArtifact("info.rvin.itest.issues", "issue-0013",
				"1.0-SNAPSHOT", "swf");
		verifier.executeGoal("install");
		verifier.displayStreamBuffers();
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();
		
		File reportDir = new File(testDir, "target/test-reports");
		assertEquals(2, reportDir.listFiles().length);
	}

}
