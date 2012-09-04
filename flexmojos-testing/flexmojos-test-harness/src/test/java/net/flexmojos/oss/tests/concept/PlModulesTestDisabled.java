package net.flexmojos.oss.tests.concept;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import java.io.File;

import com.marvinformatics.kiss.matchers.file.FileMatchers;
import org.testng.annotations.Test;

/**
 * Currently there seems to be no functionality in flexmojos that allows to filter which modules are built.
 * Therefore this test is doomed to fail. I renamed it by adding "Disabled" to the Classname in order to keep
 * the code, but have it disabled in the testsuite.
 */
public class PlModulesTestDisabled
    extends AbstractConceptTest
{

    @Test
    public void plModules()
        throws Exception
    {
        String dir = standardConceptTester( "plmodules", "-DplModules=Module4" ).getBasedir();

        File target = new File( dir, "target" );

        assertThat( new File( target, "plmodules-0.0.1-SNAPSHOT-module4.swf" ), FileMatchers.exists() );
        assertThat( new File( target, "plmodules-0.0.1-SNAPSHOT.swf" ), FileMatchers.exists() );
        assertThat( new File( target, "plmodules-0.0.1-SNAPSHOT-module1.swf" ), not( FileMatchers.exists() ) );
        assertThat( new File( target, "plmodules-0.0.1-SNAPSHOT-module2.swf" ), not( FileMatchers.exists() ) );
        assertThat( new File( target, "plmodules-0.0.1-SNAPSHOT-module3.swf" ), not( FileMatchers.exists() ) );
        assertThat( new File( target, "plmodules-0.0.1-SNAPSHOT-module5.swf" ), not( FileMatchers.exists() ) );
        assertThat( new File( target, "plmodules-0.0.1-SNAPSHOT-module6.swf" ), not( FileMatchers.exists() ) );
    }

}
