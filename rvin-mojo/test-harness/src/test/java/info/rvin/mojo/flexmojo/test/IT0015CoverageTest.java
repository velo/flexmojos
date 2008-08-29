package info.rvin.mojo.flexmojo.test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import info.flexmojos.tests.AbstractFlexMojosTests;

import java.io.File;

import org.junit.Test;

public class IT0015CoverageTest
    extends AbstractFlexMojosTests
{

    public void standardConceptTester( String coverageName )
        throws Exception
    {
        File testDir = getProject( "/coverage/" + coverageName );
        test( testDir, "install" );
    }

    @Test
    public void testSourceFileResolver()
        throws Exception
    {
        standardConceptTester( "source-file-resolver" );
    }

    @Test
    public void testAsdocInclusionExclusion()
        throws Exception
    {
        File testDir = getProject( "/coverage/asdoc-inclusion-exclusion" );
        test( testDir, "asdoc:asdoc" );
        File vermelho = new File( testDir, "target/asdoc/Vermelho.html" );
        assertFalse( "Should not generate Vermelho.html.", vermelho.exists() );
        File amarelo = new File( testDir, "target/asdoc/Amarelo.html" );
        assertFalse( "Should not generate Amarelo.html.", amarelo.exists() );
    }

    @Test
    public void testFlexUnitReport()
        throws Exception
    {
        File testDir = getProject( "/flexunit-example" );
        test( testDir, "site:site" );
        File asdoc = new File( testDir, "/target/site/asdocs" );
        assertTrue( asdoc.isDirectory() );
    }

    @Test
    public void testHtmlwrapperTemplates()
        throws Exception
    {
        File testDir = getProject( "/coverage/htmlwrapper-templates" );
        test( testDir, "package" );
        File folder = new File( testDir, "folder/target/htmlwrapper-templates-folder-1.0-SNAPSHOT.html" );
        assertTrue( "Should generate htmlwrapper" + folder.getAbsolutePath(), folder.exists() );
        File zip = new File( testDir, "zip/target/htmlwrapper-templates-zip-1.0-SNAPSHOT.html" );
        assertTrue( "Should generate htmlwrapper " + zip.getAbsolutePath(), zip.exists() );
    }

    @Test
    public void testDefines()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0068" );
        test( testDir, "install" );

    }

    @Test
    public void testCompilationOptions()
        throws Exception
    {
        standardConceptTester( "compilation-options" );
    }

}
