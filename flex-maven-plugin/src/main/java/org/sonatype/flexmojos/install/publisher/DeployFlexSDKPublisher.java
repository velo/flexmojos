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
package org.sonatype.flexmojos.install.publisher;

import java.io.File;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.flexmojos.components.publisher.AbstractFlexSDKPublisher;
import org.sonatype.flexmojos.components.publisher.FlexSDKPublisher;
import org.sonatype.flexmojos.components.publisher.PublishingException;

@Component( role = FlexSDKPublisher.class, hint = "deploy" )
public class DeployFlexSDKPublisher
    extends AbstractFlexSDKPublisher
    implements FlexSDKPublisher
{
    @Requirement
    private Map<String, ArtifactRepositoryLayout> repositoryLayouts;

    @Requirement
    private ArtifactRepositoryFactory repositoryFactory;

    @Requirement
    private ArtifactDeployer deployer;

    @Override
    protected void publishArtifact( Artifact artifact )
        throws PublishingException
    {
        File file = artifact.getFile();

        try
        {
            String repositoryLayout = (String) context.get( "repositoryLayout" );
            String repositoryId = (String) context.get( "repositoryId" );
            String url = (String) context.get( "url" );
            boolean uniqueVersion = (Boolean) context.get( "uniqueVersion" );
            ArtifactRepository localRepository = (ArtifactRepository) context.get( "localRepository" );

            ArtifactRepositoryLayout layout = repositoryLayouts.get( repositoryLayout );

            ArtifactRepository deploymentRepository =
                repositoryFactory.createDeploymentArtifactRepository( repositoryId, url, layout, uniqueVersion );

            deployer.deploy( file, artifact, deploymentRepository, localRepository );
        }
        catch ( Exception e )
        {
            getLogger().error( "Unable to deploy artifact: " + file.getAbsolutePath(), e );
            throw new PublishingException( "Unable to deploy artifact: " + file.getAbsolutePath(), e );
        }

    }

}
