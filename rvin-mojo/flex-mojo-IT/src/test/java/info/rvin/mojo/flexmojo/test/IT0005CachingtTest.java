package info.rvin.mojo.flexmojo.test;

import java.io.File;

import org.apache.maven.integrationtests.AbstractMavenIntegrationTestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class IT0005CachingtTest extends AbstractMavenIntegrationTestCase {

	public void testCachingt() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/hello-cachingframework");
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
		verifier.deleteArtifact("info.rvin.itest", "simple-cahing",
				"1.0-SNAPSHOT", "swf");
		verifier.executeGoal("install");
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();
	}
}
