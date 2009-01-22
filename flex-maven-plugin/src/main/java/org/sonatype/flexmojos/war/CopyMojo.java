package org.sonatype.flexmojos.war;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * @goal copy-flex-resources
 * @phase process-resources
 * @requiresDependencyResolution compile
 * @author Marvin Froeder
 */
public class CopyMojo
    extends AbstractMojo
{

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The directory where the webapp is built.
     * 
     * @parameter expression="${project.build.directory}/${project.build.finalName}"
     * @required
     */
    private File webappDirectory;

    /**
     * Strip artifact version during copy
     * 
     * @parameter default-value="false"
     */
    private boolean stripVersion;

    /**
     * @parameter default-value="true"
     */
    private boolean copyRSL;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        String packaging = project.getPackaging();

        if ( !"war".equals( packaging ) )
        {
            getLog().warn( "Unable to copy flex resources to a non war project" );
            return;
        }

        webappDirectory.mkdirs();

        List<Artifact> swfDependencies = getSwfArtifacts();

        for ( Artifact artifact : swfDependencies )
        {
            File sourceFile = artifact.getFile();
            File destFile;
            if ( stripVersion )
            {
                destFile = new File( webappDirectory, artifact.getArtifactId() + ".swf" );
            }
            else
            {
                destFile = new File( webappDirectory, artifact.getArtifactId() + "-" + artifact.getVersion() + ".swf" );
            }

            try
            {
                FileUtils.copyFile( sourceFile, destFile );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Failed to copy " + artifact, e );
            }
        }
    }

    @SuppressWarnings( "unchecked" )
    private List<Artifact> getSwfArtifacts()
    {
        List<Artifact> swfArtifacts = new ArrayList<Artifact>();
        Set<Artifact> artifacts = project.getArtifacts();
        for ( Artifact artifact : artifacts )
        {
            if ( "swf".equals( artifact.getType() ) )
            {
                swfArtifacts.add( artifact );
            }
        }
        return swfArtifacts;
    }
}
