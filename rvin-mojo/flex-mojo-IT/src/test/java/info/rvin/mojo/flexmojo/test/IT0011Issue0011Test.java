package info.rvin.mojo.flexmojo.test;

import java.io.File;

import org.apache.maven.integrationtests.AbstractMavenIntegrationTestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class IT0011Issue0011Test extends AbstractMavenIntegrationTestCase {

	public void testIssue() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0011");
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
		verifier.deleteArtifact("info.rvin.itest.issues", "issue-0011",
				"1.0-SNAPSHOT", "swf");
		verifier.executeGoal("install");
		verifier.displayStreamBuffers();
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();
	}

}
