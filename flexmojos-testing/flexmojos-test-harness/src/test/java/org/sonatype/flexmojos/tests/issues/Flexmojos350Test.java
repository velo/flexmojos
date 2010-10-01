package org.sonatype.flexmojos.tests.issues;

import java.io.File;

import org.apache.maven.it.Verifier;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.matcher.file.FileMatcher;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.flexmojos.util.PathUtil.*;

public class Flexmojos350Test
    extends AbstractIssueTest
{
    @Test
    public void orgFlexunit()
        throws Exception
    {
        File basedir = getProject( "issues/flexmojos-350" );
        Verifier verifier = getVerifier( basedir );

        // TODO remove this once flexunit is released!
        File or = getFile( verifier.getArtifactPath( "com.adobe.flexunit", "flexunit", "4.0-beta-2", "swc" ) );
        File fk = getFile( verifier.getArtifactPath( "org.flexunit", "flexunit", "4.1", "swc" ) );

        assertThat( or, FileMatcher.exists() );
        fk.getParentFile().mkdirs();

        FileUtils.copyFile( or, fk );

        verifier.executeGoal( "install" );

        assertThat( new File( basedir, "target/test-classes/TestRunner.swf" ), FileMatcher.exists() );
        assertThat( new File( basedir, "target/surefire-reports/TEST-AnnotatedTest.dummyTest.AnnotatedTest.xml" ),
                    FileMatcher.exists() );
    }
}
