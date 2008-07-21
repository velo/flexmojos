/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
