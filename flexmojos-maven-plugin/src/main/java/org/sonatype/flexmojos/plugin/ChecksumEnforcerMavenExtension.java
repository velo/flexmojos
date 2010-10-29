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
package org.sonatype.flexmojos.plugin;

import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;

@Component( role = AbstractMavenLifecycleParticipant.class, hint = "ChecksumEnforcer" )
public class ChecksumEnforcerMavenExtension
    extends AbstractMavenLifecycleParticipant
{

    public void afterProjectsRead( MavenSession session )
    {
        if ( session.getRequest().getUserProperties().containsKey( "flexmojos.ignore.broken.artifacts" ) )
        {
            return;
        }

        List<ArtifactRepository> repos = session.getRequest().getRemoteRepositories();
        for ( ArtifactRepository repo : repos )
        {
            // flex sdk has many dependencies, and all must be intact in order to get flexmojos working
            repo.getReleases().setChecksumPolicy( ArtifactRepositoryPolicy.CHECKSUM_POLICY_FAIL );
            repo.getSnapshots().setChecksumPolicy( ArtifactRepositoryPolicy.CHECKSUM_POLICY_FAIL );
        }
    }
}
