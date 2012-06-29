package net.flexmojos.oss.tests.concept;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import java.io.File;

import net.flexmojos.oss.matcher.file.FileMatcher;

import org.testng.annotations.Test;

public class PlModulesTest
    extends AbstractConceptTest
{

    @Test
    public void plModules()
        throws Exception
    {
        String dir = standardConceptTester( "plmodules", "-DplModules=Module4" ).getBasedir();

        File target = new File( dir, "target" );

        assertThat( new File( target, "plmodules-0.0.1-SNAPSHOT-module4.swf" ), FileMatcher.exists() );
        assertThat( new File( target, "plmodules-0.0.1-SNAPSHOT.swf" ), FileMatcher.exists() );
        assertThat( new File( target, "plmodules-0.0.1-SNAPSHOT-module1.swf" ), not( FileMatcher.exists() ) );
        assertThat( new File( target, "plmodules-0.0.1-SNAPSHOT-module2.swf" ), not( FileMatcher.exists() ) );
        assertThat( new File( target, "plmodules-0.0.1-SNAPSHOT-module3.swf" ), not( FileMatcher.exists() ) );
        assertThat( new File( target, "plmodules-0.0.1-SNAPSHOT-module5.swf" ), not( FileMatcher.exists() ) );
        assertThat( new File( target, "plmodules-0.0.1-SNAPSHOT-module6.swf" ), not( FileMatcher.exists() ) );
    }

}
