package org.sonatype.flexmojos.tests.issues;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.flexmojos.util.PathUtil.file;

import java.io.File;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.matcher.file.FileMatcher;
import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.annotations.Test;

public class Flexmojos350Test
    extends AbstractIssueTest
{
    @Test
    public void orgFlexunit()
        throws Exception
    {
        File basedir = getProject( "issues/flexmojos-350" );
        FMVerifier verifier = getVerifier( basedir );

        // TODO remove this once flexunit is released!
        File or = file( verifier.getArtifactPath( "com.adobe.flexunit", "flexunit", "4.0-beta-2", "swc" ) );
        File fk = file( verifier.getArtifactPath( "org.flexunit", "flexunit", "4.1", "swc" ) );

        assertThat( or, FileMatcher.exists() );
        fk.getParentFile().mkdirs();

        FileUtils.copyFile( or, fk );

        verifier.executeGoal( "install" );

        assertThat( new File( basedir, "target/test-classes/TestRunner.swf" ), FileMatcher.exists() );
        assertThat( new File( basedir, "target/surefire-reports/TEST-AnnotatedTest.dummyTest.AnnotatedTest.xml" ),
                    FileMatcher.exists() );
    }
}
