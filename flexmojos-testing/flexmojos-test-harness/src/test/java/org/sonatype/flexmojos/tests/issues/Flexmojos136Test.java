package org.sonatype.flexmojos.tests.issues;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.annotations.Test;

public class Flexmojos136Test extends AbstractIssueTest {
	@Test
	public void resolveReleaseVersion() throws Exception {
		File libTestDir = getProject("/issues/flexmojos-136");
		test(libTestDir, "install");

		// Vladimir Krivosheev ? has not found other easy way to set release =
		// true
		File libArtifactMetadataFile = new File(getProperty("fake-repo"),
				"info/rvin/itest/test-special-version/lib/maven-metadata-local.xml");
		Metadata libArtifactMetadata = new MetadataXpp3Reader()
				.read(ReaderFactory.newXmlReader(libArtifactMetadataFile));
		libArtifactMetadata.getVersioning().setRelease("1.0");
		new MetadataXpp3Writer().write(
				WriterFactory.newXmlWriter(libArtifactMetadataFile),
				libArtifactMetadata);

		File appTestDir = getProject("/issues/flexmojos-136/app");
		FMVerifier appVerifier = test(appTestDir, "compile",
				"-DconfigurationReport");

		Xpp3Dom appConfigReportDOM = getFlexConfigReport(appVerifier, "app",
				"1.0");
		Xpp3Dom rslPath = appConfigReportDOM
				.getChild("runtime-shared-library-path");
		assertThat(
				new File(rslPath.getChild("path-element").getValue())
						.getCanonicalPath(),
				equalTo(new File(getProperty("fake-repo"),
						"/info/rvin/itest/test-special-version/lib/1.0/lib-1.0.swc")
						.getCanonicalPath()));
		assertThat(rslPath.getChild("rsl-url").getValue(),
				equalTo("/rsl/lib-1.0.swf"));
	}
}
