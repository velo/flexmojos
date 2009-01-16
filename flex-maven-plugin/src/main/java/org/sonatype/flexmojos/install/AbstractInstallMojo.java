/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.install;

import java.io.File;
import java.security.SecureRandom;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.sonatype.flexmojos.components.publisher.FlexSDKPublisher;
import org.sonatype.flexmojos.components.publisher.PublishingException;

public abstract class AbstractInstallMojo
    extends AbstractMojo
    implements Contextualizable
{

    protected Context context;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private ArtifactResolver resolver;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    protected ArtifactRepository localRepository;

    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List<?> remoteRepositories;

    /**
     * File location where targeted Flex SDK is located
     * 
     * @parameter expression="${flex.sdk.folder}"
     * @required
     */
    protected File sdkFolder;

    /**
     * Flex SDK version. Recommend pattern:
     * <ul>
     * Append -FB3 suffix on Flexbuilder sdks
     * </ul>
     * <ul>
     * Append -LCDS suffix on LCDS sdks
     * </ul>
     * <BR>
     * Samples:
     * <ul>
     * 3.0.0.477
     * </ul>
     * <ul>
     * 3.0.0.477-FB3
     * </ul>
     * <ul>
     * 3.0.0.477-LCDS
     * </ul>
     * 
     * @parameter expression="${version}"
     * @required
     */
    protected String version;

    /**
     * @parameter expression="${overwriteLibFolder}"
     */
    protected File overwriteLibFolder;

    /**
     * @parameter expression="${default.player.version}" default-value="9"
     */
    protected int defaultPlayerVersion;

    /**
     * @parameter expression="${overwrite.code}"
     */
    protected String overwriteCode;

    public AbstractInstallMojo()
    {
        super();
    }

    public final void execute()
        throws MojoExecutionException, MojoFailureException
    {
        validateVersion();

        try
        {
            getPublisher().publish( this.sdkFolder, this.version, this.defaultPlayerVersion, this.overwriteLibFolder );
        }
        catch ( PublishingException e )
        {
            throw new MojoFailureException( "Unable to install flex SDK", e );
        }
    }

    private void validateVersion()
        throws MojoFailureException, MojoExecutionException
    {
        Artifact artifact =
            artifactFactory.createArtifact( FlexSDKPublisher.ADOBE_GROUP_ID, "compiler", version, "compile", "pom" );
        try
        {
            resolver.resolve( artifact, remoteRepositories, localRepository );
        }
        catch ( AbstractArtifactResolutionException e )
        {
            // ok, is not already available at any know location
            return;
        }

        SecureRandom random = new SecureRandom( version.getBytes() );
        String generatedCode = Integer.toHexString( random.nextInt() );

        if ( overwriteCode == null )
        {
            showWarn( generatedCode );
            throw new MojoFailureException( "Unable to overwrite an existing FDK!" );
        }

        if ( !generatedCode.equals( overwriteCode ) )
        {
            showWarn( generatedCode );
            throw new MojoFailureException( "Invalid overwrite code!" );
        }

    }

    private void showWarn( String generatedCode )
        throws MojoFailureException
    {
        getLog().warn( "========================================================================" );
        getLog().warn( " ATTENTION:" );
        getLog().warn( "Flex-mojos detected Flex SDK " + version + " is already installed!" );
        getLog().warn(
                       "It is strongly recommend you never overwrite a Flex SDK version released on a public repositories!" );
        getLog().warn( "" );
        getLog().warn(
                       "If you are absolute sure about what you are doing, please, re-run this mojo with the following parameter:" );
        getLog().warn( "-Doverwrite.code=" + generatedCode );
        getLog().warn( "========================================================================" );
    }

    protected abstract FlexSDKPublisher getPublisher()
        throws MojoExecutionException;

    public void contextualize( Context context )
        throws ContextException
    {
        this.context = context;
    }

}