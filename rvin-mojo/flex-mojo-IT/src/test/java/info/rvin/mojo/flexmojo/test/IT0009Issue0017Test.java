package info.rvin.mojo.flexmojo.test;

import java.io.File;

import org.apache.maven.integrationtests.AbstractMavenIntegrationTestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class IT0009Issue0017Test extends AbstractMavenIntegrationTestCase {

	public void testFailOnErrors() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0017");
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
		verifier.deleteArtifact("info.rvin.itest.issues", "issue-0017",
				"1.0-SNAPSHOT", "swf");
		verifier.executeGoal("site:site");
		verifier.displayStreamBuffers();
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();
		
		File asdoc = new File(testDir, "target/site/asdocs");
		assertTrue("asdoc directory must exist", asdoc.isDirectory());
	}

}
