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
