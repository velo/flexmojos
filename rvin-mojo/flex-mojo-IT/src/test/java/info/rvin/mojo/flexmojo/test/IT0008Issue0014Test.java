package info.rvin.mojo.flexmojo.test;

import java.io.File;

import org.apache.maven.integrationtests.AbstractMavenIntegrationTestCase;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class IT0008Issue0014Test extends AbstractMavenIntegrationTestCase {

	public void testFailOnErrors() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0014");
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
		verifier.deleteArtifact("info.rvin.itest.issues", "issue-0014",
				"1.0-SNAPSHOT", "swf");
		verifier.executeGoal("install");
		verifier.displayStreamBuffers();
		try {
			verifier.verifyErrorFreeLog();
			fail("This test must throw errors");
		} catch (VerificationException e) {
			//expected exception
			System.out.println("Got required Fail =D");
		}
		verifier.resetStreams();

	}

}
