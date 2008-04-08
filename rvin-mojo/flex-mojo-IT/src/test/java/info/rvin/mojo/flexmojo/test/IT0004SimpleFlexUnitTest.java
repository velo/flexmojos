package info.rvin.mojo.flexmojo.test;

import java.io.File;

import org.apache.maven.integrationtests.AbstractMavenIntegrationTestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class IT0004SimpleFlexUnitTest extends AbstractMavenIntegrationTestCase {

	public void testFlexLibrary() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/flexunit-example");
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
		verifier.deleteArtifact("com.adobe.samples", "calculator-api",
				"1.0-SNAPSHOT", "swc");
		verifier.executeGoal("install");
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();
	}
}
