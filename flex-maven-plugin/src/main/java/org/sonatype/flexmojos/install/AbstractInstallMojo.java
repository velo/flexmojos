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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.flexmojos.sandbox.bundlepublisher.BundlePublisher;
import org.sonatype.flexmojos.sandbox.bundlepublisher.PublishingException;
import org.sonatype.flexmojos.sandbox.bundlepublisher.model.BundleArtifact;
import org.sonatype.flexmojos.sandbox.bundlepublisher.model.BundleDescriptor;

public abstract class AbstractInstallMojo
    extends AbstractMojo
{

    /**
     * @component
     */
    private BundlePublisher publisher;

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
     */
    private List<?> remoteRepositories;

    /**
     * File location where targeted Flex SDK is located
     * 
     * @parameter expression="${flex.sdk.bundle}"
     * @required
     */
    private File sdkBundle;

    /**
     * File location where targeted Flex SDK is located
     * 
     * @parameter expression="${flex.sdk.descriptor}"
     * @required
     */
    private File sdkDescriptor;

    /**
     * Security code to make sure nobody will overwrite FDK version by accident
     * 
     * @parameter expression=${overwrite.code}
     */
    private String overwriteCode;

    public AbstractInstallMojo()
    {
        super();
    }

    public final void execute()
        throws MojoExecutionException, MojoFailureException
    {

        InputStream in = null;
        try
        {
            in = new FileInputStream( sdkDescriptor );
            publisher.validate( sdkBundle, in );
            IOUtil.close( in );

            validateVersion();

            in = new FileInputStream( sdkDescriptor );
            proceed( publisher, sdkBundle, in );
            IOUtil.close( in );
        }
        catch ( PublishingException e )
        {
            throw new MojoFailureException( "Unable to install flex SDK: " + e.getMessage(), e );
        }
        catch ( FileNotFoundException e )
        {
            throw new MojoFailureException( "Flex SDK descriptor not found", e );
        }
        finally
        {
            IOUtil.close( in );
        }
    }

    protected abstract void proceed( BundlePublisher publisher, File sdkBundle, InputStream sdkDescriptor )
        throws PublishingException;

    private void validateVersion()
        throws MojoFailureException, MojoExecutionException
    {
        BundleDescriptor descriptor;
        try
        {
            descriptor = BundleDescriptor.read( this.sdkDescriptor );
        }
        catch ( Exception e )
        {
            throw new MojoFailureException( "Fail to parse bundle descriptor", e );
        }

        BundleArtifact bundle = descriptor.getArtifacts().get( 0 );

        String groupId = bundle.getGroupId() != null ? bundle.getGroupId() : descriptor.getDefaults().getGroupId();
        String artifactId = bundle.getArtifactId();
        String version = bundle.getVersion() != null ? bundle.getVersion() : descriptor.getDefaults().getVersion();
        String type = bundle.getType() != null ? bundle.getType() : "jar";
        String classifier = bundle.getClassifier();

        Artifact artifact =
            artifactFactory.createArtifactWithClassifier( groupId, artifactId, version, type, classifier );
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
        String generatedCode = Integer.toHexString( random.nextInt( 4 ) );

        if ( overwriteCode == null )
        {
            showWarn( generatedCode, version );
        }

        if ( !generatedCode.equals( overwriteCode ) )
        {
            showWarn( generatedCode, version );
        }

    }

    private void showWarn( String generatedCode, String version )
        throws MojoFailureException
    {
        getLog().warn( "------------------------------------------------------------------------" );
        getLog().warn( " ATTENTION:" );
        getLog().warn( "Flex-mojos detected Flex SDK " + version + " is already installed!" );
        getLog().warn( "It is strongly recommend you never overwrite Flex SDK version released on" );
        getLog().warn( "public repositories!" );
        getLog().warn( "If you are sure about this, run this mojo with the followinf parameter:" );
        getLog().warn( "-Doverwrite.code=" + generatedCode );
        getLog().warn( "------------------------------------------------------------------------" );
        throw new MojoFailureException( "Unable to an existing FDK!" );
    }

}