/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.install;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;

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
     * URL where the artifact will be deployed. <br/>
     * ie ( file://C:\m2-repo or scp://host.com/path/to/repo )
     * 
     * @parameter expression="${url}"
     * @required
     */
    private String url;

    @Override
    protected Repository getRepository()
        throws RepositoryException, MojoExecutionException
    {
        URL serverUrl;
        try
        {
            serverUrl = new URL( url );
        }
        catch ( MalformedURLException e )
        {
            throw new MojoExecutionException( "Invalid Url: " + url, e );
        }

        Server server = session.getSettings().getServer( repositoryId );
        String username = null;
        String userpassword = null;
        if ( server != null )
        {
            username = server.getUsername();
            userpassword = server.getPassword();
        }

        RemoteRepositoryM2 repo =
            mercury.constructRemoteRepositoryM2( repositoryId, serverUrl, username, userpassword, null, null, null,
                                                 null, null, null, null );
        return repo;
    }

    // ----------------------------------------------------------------
    /** @parameter expression="${session}" */
    private MavenSession session;

    /**
     * Remote repositories declared in the project pom
     * 
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    protected List<ArtifactRepository> remoteRepositories;

}
