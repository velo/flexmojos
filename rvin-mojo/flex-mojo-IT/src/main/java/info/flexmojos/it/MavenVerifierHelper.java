package info.flexmojos.it;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;

public class MavenVerifierHelper {

	public static void customTester(File testDir, String groupId,
			String artifactId, String version, String type, String goal)
			throws Exception {
		customTester(testDir, groupId, artifactId, version, type, goal,
				new ArrayList<String>());
	}

	public static void customTester(File testDir, String groupId,
			String artifactId, String version, String type, String goal,
			List<String> args) throws Exception {
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
		verifier.deleteArtifact(groupId, artifactId, version, type);
		verifier.setCliOptions(args);
		verifier.executeGoal(goal);
		try {
			verifier.verifyErrorFreeLog();
		} catch (VerificationException e) {
			verifier.displayStreamBuffers();
			throw e;
		}
		verifier.resetStreams();
	}
}
