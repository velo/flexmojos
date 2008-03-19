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
public class SimpleCompileTest extends AbstractMavenIntegrationTestCase {

	public void testAirLibrary() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/simple-air-library");
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
		verifier.deleteArtifact("info.rvin.itest", "simple-air-library",
				"1.0-SNAPSHOT", "aswc");
		verifier.executeGoal("install");
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();
	}

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

	public void testAirApplication() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/simple-air-application");
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
		verifier.deleteArtifact("info.rvin.itest", "simple-air-application",
				"1.0-SNAPSHOT", "aswf");
		verifier.executeGoal("install");
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();
	}

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
