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
public class IT0003FlexSDKCompileTest extends AbstractMavenIntegrationTestCase {

	public void testFlexSDK() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/flex-sdk");
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
		verifier.deleteArtifact("com.adobe.flex.sdk", "projects", "3.0-SNAPSHOT", "pom");
		verifier.deleteArtifact("com.adobe.flex.sdk", "framework", "3.0-SNAPSHOT", "swc");
		verifier.deleteArtifact("com.adobe.flex.sdk", "rpc", "3.0-SNAPSHOT", "swc");
		verifier.deleteArtifact("com.adobe.flex.sdk", "airframework", "3.0-SNAPSHOT", "swc");
		verifier.deleteArtifact("com.adobe.flex.sdk", "flex", "3.0-SNAPSHOT", "swc");
		verifier.deleteArtifact("com.adobe.flex.sdk", "utilities", "3.0-SNAPSHOT", "swc");
		verifier.deleteArtifact("com.adobe.flex.sdk", "haloclassic", "3.0-SNAPSHOT", "swc");
		verifier.deleteArtifact("com.adobe.flex.sdk", "flash-integration", "3.0-SNAPSHOT", "swc");

		verifier.executeGoal("install");
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();
	}
}
