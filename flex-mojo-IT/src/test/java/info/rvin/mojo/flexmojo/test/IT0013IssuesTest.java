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
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class IT0013IssuesTest extends AbstractMavenIntegrationTestCase {

	private void standardIssueTester(String issueNumber) throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/" + issueNumber);
		customIssueTester(testDir, "info.rvin.itest.issues", issueNumber,
				"1.0-SNAPSHOT", "swf", "install");
	}

	private void customIssueTester(File testDir, String groupId,
			String artifactId, String version, String type, String goal)
			throws Exception {
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
		verifier.deleteArtifact(groupId, artifactId, version, type);
		verifier.executeGoal(goal);
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();
	}

	public void testIssue8_1() throws Exception {
		standardIssueTester("issue-0008-1");
	}

	public void testIssue8_2() throws Exception {
		standardIssueTester("issue-0008-2");
	}

	public void testIssue11() throws Exception {
		standardIssueTester("issue-0011");
	}

	public void testIssue13() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0013");
		customIssueTester(testDir, "info.rvin.itest.issues", "issue-0013",
				"1.0-SNAPSHOT", "swf", "install");

		File reportDir = new File(testDir, "target/surefire-reports");
		assertEquals(2, reportDir.listFiles().length);
	}

	public void testIssue14() throws Exception {
		try {
			standardIssueTester("issue-0014");
			fail("This test must throw errors");
		} catch (VerificationException e) {
			// expected exception
			// System.out.println("Got required Fail =D");
		}
	}

	public void testIssue15() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0015");
		try {
			customIssueTester(testDir, "info.rvin.itest.issues", "issue-0015",
					"1.0-SNAPSHOT", "swf", "install");
			fail("testing error unit, must fail!");
		} catch (Exception e) {
			// expected exception
		}

		File reportDir = new File(testDir, "target/surefire-reports");
		assertEquals(2, reportDir.listFiles().length);
	}

	public void testIssue17() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0017");
		customIssueTester(testDir, "info.rvin.itest.issues", "issue-0017",
				"1.0-SNAPSHOT", "swf", "asdoc:asdoc");

		File asdoc = new File(testDir, "target/asdocs");
		assertTrue("asdoc directory must exist", asdoc.isDirectory());
	}

	public void testIssue27() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0027");
		customIssueTester(testDir, "info.rvin.itest.issues", "issue-0027",
				"1.0-SNAPSHOT", "swf", "asdoc:asdoc");
	}

	public void testIssue29() throws Exception {
		standardIssueTester("issue-0029");
	}

	public void testIssue32() throws Exception {
		standardIssueTester("issue-0032");
	}

	public void testIssue39() throws Exception {
		standardIssueTester("issue-0039");
	}

	public void testIssue44() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0044");
		try {
			customIssueTester(testDir, "info.rvin.itest.issues", "issue-0044",
					"1.0-SNAPSHOT", "swf", "asdoc:asdoc");
			fail("testing error unit, must fail!");
		} catch (Exception e) {
			// expected exception
		}
	}

	public void testIssue61() throws Exception {
		standardIssueTester("issue-0061");
	}

	public void testIssue67() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0067");
		customIssueTester(testDir, "info.rvin.itest.issues", "issue-0067",
				"1.0-SNAPSHOT", "swf", "asdoc:asdoc");
	}

	public void testIssue68() throws Exception {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/issues/issue-0068");
		customIssueTester(testDir, "info.rvin.itest.issues", "issue-0068",
				"1.0-SNAPSHOT", "swf", "asdoc:asdoc");
	}

	public void testIssue69() throws Exception {
		standardIssueTester("issue-0069");
	}

}
