package info.flexmojos.it;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.IOUtil;

public class MavenVerifierHelper {

	public static void customTester(File testDir, String goal,
			String... args) throws Exception {
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), false);
		verifier.resetStreams();
		verifier.setCliOptions(Arrays.asList(args));
		verifier.executeGoal(goal);
		try {
			verifier.verifyErrorFreeLog();
		} catch (VerificationException e) {
			System.out.println();
			System.out.println("Test folder " + testDir.getAbsolutePath());
			try {
				File logFile = new File(verifier.getBasedir(), "log.txt");
				String log = IOUtil.toString(new FileReader(logFile));
				System.out.println(log);
			} catch (Throwable t) {
				// skip
				System.out.println("Unable to print maven log");
			}
			throw e;
		}
	}
}
