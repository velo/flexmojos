package org.sonatype.flexmojos.plugin.test.scanners;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.DirectoryScanner;

public abstract class AbstractFlexClassScanner
    extends AbstractLogEnabled
    implements FlexClassScanner
{

    protected List<String> classes;

    public AbstractFlexClassScanner()
    {
        super();
    }

    protected List<String> scan( File dir, String[] exclusions, Map<String, Object> context )
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( dir );
        scanner.setIncludes( new String[] { "**/*.as", "**/*.mxml" } );
        scanner.setExcludes( exclusions );
        scanner.addDefaultExcludes();
        scanner.scan();

        return new ArrayList<String>( Arrays.asList( scanner.getIncludedFiles() ) );
    }

    public List<String> getAs3Classes()
    {
        return classes;
    }

}