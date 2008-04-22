package info.rvin.mojo.flexmojo.test;

import java.io.File;

import org.apache.maven.integrationtests.AbstractMavenIntegrationTestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

/**
 * This is a sample integration test. The IT tests typically operate by having a
 * sample project in the /src/test/resources folder along with a junit test like
 * this one. The junit test uses the verifier (which uses the invoker) to invoke
 * a new instance of Maven on the project in the resources folder. It then
 * checks the results. This is a non-trivial example that shows two phases. See
 * more information inline in the code.
 * 
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 * 
 */
public class IT0001SimpleCompileTest extends AbstractMavenIntegrationTestCase {

//	public void testAirLibrary() throws Exception {
//		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
//				"/simple-air-library");
//		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
//		verifier.deleteArtifact("info.rvin.itest", "simple-air-library",
//				"1.0-SNAPSHOT", "aswc");
//		verifier.executeGoal("install");
//		verifier.verifyErrorFreeLog();
//		verifier.resetStreams();
//	}

	public void testFlexLibrary() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/simple-flex-library");
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
		verifier.deleteArtifact("info.rvin.itest", "simple-flex-library",
				"1.0-SNAPSHOT", "swc");
		verifier.executeGoal("install");
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();
	}

//	public void testAirApplication() throws Exception {
//		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
//				"/simple-air-application");
//		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
//		verifier.deleteArtifact("info.rvin.itest", "simple-air-application",
//				"1.0-SNAPSHOT", "aswf");
//		verifier.executeGoal("install");
//		verifier.verifyErrorFreeLog();
//		verifier.resetStreams();
//	}

	public void testFlexApplication() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/simple-flex-application");
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
		verifier.deleteArtifact("info.rvin.itest", "simple-flex-application",
				"1.0-SNAPSHOT", "swf");
		verifier.executeGoal("install");
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();
	}
}
