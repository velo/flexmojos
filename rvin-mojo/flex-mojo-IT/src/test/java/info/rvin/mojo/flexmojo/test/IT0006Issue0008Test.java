package info.rvin.mojo.flexmojo.test;

import java.io.File;

import org.apache.maven.integrationtests.AbstractMavenIntegrationTestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class IT0006Issue0008Test extends AbstractMavenIntegrationTestCase {
	
	public void testWithoutTestFolder() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
		"/issues/issue-0008-1");
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
		verifier.deleteArtifact("info.rvin.itest.issues", "issue-0008-1",
				"1.0-SNAPSHOT", "swf");
		verifier.executeGoal("install");
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();
	}

	public void testFolderWithNoSenseContent() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0008-2");
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
		verifier.deleteArtifact("info.rvin.itest.issues", "issue-0008-2",
				"1.0-SNAPSHOT", "swf");
		verifier.executeGoal("install");
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();
	}
}
