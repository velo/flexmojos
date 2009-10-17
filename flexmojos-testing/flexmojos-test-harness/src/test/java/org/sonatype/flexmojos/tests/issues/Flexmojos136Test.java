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
package org.sonatype.flexmojos.tests.issues;

import java.io.File;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.apache.maven.it.Verifier;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos136Test
    extends AbstractIssueTest
{
    @Test
    public void resolveReleaseVersion()
        throws Exception
    {
        File libTestDir = getProject( "/issues/flexmojos-136" );
        test( libTestDir, "install" );

        // Vladimir Krivosheev ? has not found other easy way to set release = true
        File libArtifactMetadataFile =
            new File( getProperty( "fake-repo" ), "info/rvin/itest/test-special-version/lib/maven-metadata-local.xml" );
        Metadata libArtifactMetadata =
            new MetadataXpp3Reader().read( ReaderFactory.newXmlReader( libArtifactMetadataFile ) );
        libArtifactMetadata.getVersioning().setRelease( "1.0" );
        new MetadataXpp3Writer().write( WriterFactory.newXmlWriter( libArtifactMetadataFile ), libArtifactMetadata );

        File appTestDir = getProject( "/issues/flexmojos-136/app" );
        Verifier appVerifier = test( appTestDir, "compile", "-DconfigurationReport" );

        Xpp3Dom appConfigReportDOM = getFlexConfigReport( appVerifier, "app", "1.0" );
        Xpp3Dom rslPath = appConfigReportDOM.getChild( "runtime-shared-library-path" );
        Assert.assertEquals( new File( rslPath.getChild( "path-element" ).getValue() ).getCanonicalPath(),
                             new File( getProperty( "fake-repo" ),
                                       "/info/rvin/itest/test-special-version/lib/1.0/lib-1.0.swc" ).getCanonicalPath() );
        Assert.assertEquals( rslPath.getChild( "rsl-url" ).getValue(), "rsl/lib-1.0.swf" );
    }
}
