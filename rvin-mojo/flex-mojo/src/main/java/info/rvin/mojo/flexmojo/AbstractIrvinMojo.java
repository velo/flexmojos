package info.rvin.mojo.flexmojo;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * 
 * Encapsulate the access to Maven API. Some times just to hide Java 5 warnings
 * 
 * @author tech35212
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
	 * @parameter expression="${localRepository}"
	 */
	protected ArtifactRepository localRepository;

	/**
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 */
	@SuppressWarnings("unchecked")
	protected List remoteRepositories;

	public AbstractIrvinMojo() {
		super();
	}

	@SuppressWarnings("unchecked")
	protected List<Resource> getResources() {
		// I wanna maven on Java 5
		return build.getResources();
	}

	@SuppressWarnings("unchecked")
	protected List<Dependency> getDependencies() {
		return project.getDependencies();
	}

	protected List<Dependency> getDependencies(String scope)
			throws MojoExecutionException {
		if (scope == null)
			return null;

		List<Dependency> dependencies = new ArrayList<Dependency>();
		for (Dependency d : getDependencies()) {
			if (scope.equals(d.getScope())) {
				dependencies.add(d);
			}
		}
		return dependencies;
	}

	protected Artifact getArtifact(Dependency dependency)
			throws MojoExecutionException {
		Artifact artifact = artifactFactory.createArtifactWithClassifier(
				dependency.getGroupId(), dependency.getArtifactId(), dependency
						.getVersion(), dependency.getType(), dependency
						.getClassifier());
		resolveArtifact(artifact);
		return artifact;
	}

	protected void resolveArtifact(Artifact artifact)
			throws MojoExecutionException {
		try {
			resolver.resolve(artifact, remoteRepositories, localRepository);
		} catch (ArtifactResolutionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (ArtifactNotFoundException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	public final void execute() throws MojoExecutionException,
			MojoFailureException {
		setUp();
		run();
		tearDown();
	}

	protected abstract void setUp() throws MojoExecutionException,
			MojoFailureException;

	protected abstract void run() throws MojoExecutionException,
			MojoFailureException;

	protected abstract void tearDown() throws MojoExecutionException,
			MojoFailureException;

}