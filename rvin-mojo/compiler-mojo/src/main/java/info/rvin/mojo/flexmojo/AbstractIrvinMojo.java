package info.rvin.mojo.flexmojo;

import info.rvin.flexmojos.utilities.MavenUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.MavenProjectHelper;

/**
 * 
 * Encapsulate the access to Maven API. Some times just to hide Java 5 warnings
 * 
 */
public abstract class AbstractIrvinMojo extends AbstractMojo {

	/**
	 * The maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * @parameter expression="${project.build}"
	 * @required
	 * @readonly
	 */
	protected Build build;

	/**
	 * @component
	 */
	protected MavenProjectHelper projectHelper;

	/**
	 * @component
	 */
	protected ArtifactFactory artifactFactory;

	/**
	 * @component
	 */
	protected ArtifactResolver resolver;

	/**
	 * @component
	 */
	protected ArtifactMetadataSource artifactMetadataSource;

	/**
	 * @component
	 */
	protected MavenProjectBuilder mavenProjectBuilder;

	/**
	 * Local repository to be used by the plugin to resolve dependencies.
	 * 
	 * @parameter expression="${localRepository}"
	 */
	protected ArtifactRepository localRepository;

	/**
	 * List of remote repositories to be used by the plugin to resolve dependencies.
	 * 
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 */
	@SuppressWarnings("unchecked")
	protected List remoteRepositories;

	/**
	 * Construct Mojo instance
	 */
	public AbstractIrvinMojo() {
		super();
	}

	// dependency artifactes
	private Set<Artifact> dependencyArtifacts;

	/**
	 * Returns Set of dependency artifacts which are resolved for the project.
	 * @return Set of dependency artifacts.
	 * @throws MojoExecutionException
	 */
	protected Set<Artifact> getDependencyArtifacts()
			throws MojoExecutionException {
		if (dependencyArtifacts == null) {
			dependencyArtifacts = MavenUtils.getDependencyArtifacts(project,
					resolver, localRepository, remoteRepositories,
					artifactMetadataSource);
		}
		return dependencyArtifacts;
	}

	/**
	 * Get dependency artifacts for given scope
	 * @param scope for which to get artifacts
	 * @return List of artifacts
	 * @throws MojoExecutionException
	 */
	protected List<Artifact> getDependencyArtifacts(String scope)
			throws MojoExecutionException {
		if (scope == null)
			return null;

		List<Artifact> artifacts = new ArrayList<Artifact>();
		for (Artifact artifact : getDependencyArtifacts()) {
			if ("swc".equals(artifact.getType())
					&& scope.equals(artifact.getScope())) {
				artifacts.add(artifact);
			}
		}
		return artifacts;
	}

	/**
	 * Executes plugin
	 */
	public void execute() throws MojoExecutionException,
			MojoFailureException {
		setUp();
		run();
		tearDown();
	}

	/**
	 * Perform setup before plugin is run.
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	protected abstract void setUp() throws MojoExecutionException,
			MojoFailureException;

	/**
	 * Perform plugin functionality
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	protected abstract void run() throws MojoExecutionException,
			MojoFailureException;

	/**
	 * Perform (cleanup) actions after plugin has run
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	protected abstract void tearDown() throws MojoExecutionException,
			MojoFailureException;

}