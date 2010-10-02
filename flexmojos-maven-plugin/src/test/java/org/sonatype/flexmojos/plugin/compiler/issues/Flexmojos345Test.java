package org.sonatype.flexmojos.plugin.compiler.issues;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.sonatype.flexmojos.compiler.IIncludeFile;
import org.sonatype.flexmojos.plugin.compiler.CompcMojo;
import org.sonatype.flexmojos.plugin.compiler.attributes.converter.SimplifiablePattern;
import org.testng.annotations.Test;

public class Flexmojos345Test
{

    @Test
    public void checkFileInclusions()
        throws Exception
    {
        CompcMojo m = new CompcMojo()
        {
            @Override
            public IIncludeFile[] getIncludeFile()
            {
                includeFiles = new SimplifiablePattern();
                includeFiles.addInclude( "abc\\cba\\test" );
                return super.getIncludeFile();
            }

            @Override
            protected List<File> getResourcesTargetDirectories()
            {
                return Arrays.asList( new File( "." ) );
            }
        };

        IIncludeFile[] files = m.getIncludeFile();

        for ( IIncludeFile file : files )
        {
            assertThat( file.name(), not( containsString( "\\" ) ) );
        }
    }

}
