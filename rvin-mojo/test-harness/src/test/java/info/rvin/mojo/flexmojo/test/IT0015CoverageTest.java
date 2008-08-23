package info.rvin.mojo.flexmojo.test;

import static info.flexmojos.it.MavenVerifierHelper.customTester;
import info.flexmojos.it.MavenVerifierHelper;

import java.io.File;

import org.apache.maven.integrationtests.AbstractMavenIntegrationTestCase;
import org.apache.maven.it.util.ResourceExtractor;

public class IT0015CoverageTest
    extends AbstractMavenIntegrationTestCase
{

    public static void standardConceptTester( String coverageName )
        throws Exception
    {
        File testDir =
            ResourceExtractor.simpleExtractResources( MavenVerifierHelper.class, "/coverage/" + coverageName );
        customTester( testDir, "install" );
    }

    public void testSourceFileResolver()
        throws Exception
    {
        standardConceptTester( "source-file-resolver" );
    }

    public void testAsdocInclusionExclusion()
        throws Exception
    {
        File testDir =
            ResourceExtractor.simpleExtractResources( MavenVerifierHelper.class, "/coverage/asdoc-inclusion-exclusion" );
        customTester( testDir, "asdoc:asdoc" );
        File vermelho = new File( testDir, "target/asdoc/Vermelho.html" );
        assertFalse( "Should not generate Vermelho.html.", vermelho.exists() );
        File amarelo = new File( testDir, "target/asdoc/Amarelo.html" );
        assertFalse( "Should not generate Amarelo.html.", amarelo.exists() );
    }

    public void testFlexUnitReport()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( MavenVerifierHelper.class, "/flexunit-example" );
        customTester( testDir, "site:site" );
        File asdoc = new File( testDir, "/target/site/asdocs" );
        assertTrue( asdoc.isDirectory() );
    }

    public void testHtmlwrapperTemplates()
        throws Exception
    {
        File testDir =
            ResourceExtractor.simpleExtractResources( MavenVerifierHelper.class, "/coverage/htmlwrapper-templates" );
        customTester( testDir, "package" );
        File folder = new File( testDir, "folder/target/htmlwrapper-templates-folder-1.0-SNAPSHOT.html" );
        assertTrue( "Should generate htmlwrapper" + folder.getAbsolutePath(), folder.exists() );
        File zip = new File( testDir, "zip/target/htmlwrapper-templates-zip-1.0-SNAPSHOT.html" );
        assertTrue( "Should generate htmlwrapper " + zip.getAbsolutePath(), zip.exists() );
    }

    public void testDefines()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/issues/issue-0068" );
        customTester( testDir, "install" );

    }

    public void testCompilationOptions()
        throws Exception
    {
        standardConceptTester( "compilation-options" );
    }

}
