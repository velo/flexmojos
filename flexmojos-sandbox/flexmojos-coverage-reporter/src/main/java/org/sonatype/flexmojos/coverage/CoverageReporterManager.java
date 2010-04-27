package org.sonatype.flexmojos.coverage;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

@Component( role = CoverageReporterManager.class )
public class CoverageReporterManager
{

    @Requirement
    private PlexusContainer container;

    public CoverageReporter getReporter( String provider )
        throws CoverageReportException
    {
        try
        {
            return container.lookup( CoverageReporter.class, provider );
        }
        catch ( ComponentLookupException e )
        {
            throw new CoverageReportException( "Invalid coverage provider: " + provider, e );
        }
    }

}
