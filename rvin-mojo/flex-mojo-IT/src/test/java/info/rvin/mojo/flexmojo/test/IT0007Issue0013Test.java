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
package info.rvin.mojo.flexmojo.test;

import java.io.File;

import org.apache.maven.integrationtests.AbstractMavenIntegrationTestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class IT0007Issue0013Test extends AbstractMavenIntegrationTestCase {
	
	public void testWithoutTestFolder() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
		"/issues/issue-0013");
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
		verifier.deleteArtifact("info.rvin.itest.issues", "issue-0013",
				"1.0-SNAPSHOT", "swf");
		verifier.executeGoal("install");
		verifier.displayStreamBuffers();
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();
		
		File reportDir = new File(testDir, "target/test-reports");
		assertEquals(2, reportDir.listFiles().length);
	}

}
