/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.install;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.sonatype.flexmojos.sandbox.bundlepublisher.BundlePublisher;
import org.sonatype.flexmojos.sandbox.bundlepublisher.PublishingException;

/**
 * @goal deploy-sdk
 * @requiresProject false
 * @requiresDirectInvocation true
 * @author marvin
 */
public class SDKDeployMojo
    extends AbstractInstallMojo
{

    /**
     * Server Id to map on the &lt;id&gt; under &lt;server&gt; section of settings.xml In most cases, this parameter
     * will be required for authentication.
     * 
     * @parameter expression="${repositoryId}" default-value="remote-repository"
     * @required
     */
    private String repositoryId;

    /**
     * The type of remote repository layout to deploy to. Try <i>legacy</i> for a Maven 1.x-style repository layout.
     * 
     * @parameter expression="${repositoryLayout}" default-value="default"
     * @required
     */
    private String repositoryLayout;

    /**
     * Map that contains the layouts
     * 
     * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
     * @required
     * @readonly
     */
    @SuppressWarnings( "unchecked" )
    private Map repositoryLayouts;

    /**
     * URL where the artifact will be deployed. <br/>
     * ie ( file://C:\m2-repo or scp://host.com/path/to/repo )
     * 
     * @parameter expression="${url}"
     * @required
     */
    private String url;

    /**
     * Whether to deploy snapshots with a unique version or not.
     * 
     * @parameter expression="${uniqueVersion}" default-value="true"
     */
    private boolean uniqueVersion;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @component
     * @required
     * @readonly
     */
    private ArtifactRepositoryFactory repositoryFactory;

    @Override
    protected void proceed( BundlePublisher publisher, File sdkBundle, InputStream sdkDescriptor )
        throws PublishingException
    {
        ArtifactRepositoryLayout layout = (ArtifactRepositoryLayout) repositoryLayouts.get( repositoryLayout );

        ArtifactRepository deploymentRepository =
            repositoryFactory.createDeploymentArtifactRepository( repositoryId, url, layout, uniqueVersion );

        publisher.deploy( sdkBundle, sdkDescriptor, deploymentRepository, localRepository );
    }
}
