package info.rvin.mojo.flexmojo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
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

	private Set<Artifact> dependencyArtifacts;

	@SuppressWarnings("unchecked")
	protected Set<Artifact> getDependencyArtifacts()
			throws MojoExecutionException {
		if (dependencyArtifacts == null) {
			ArtifactResolutionResult arr;
			try {
				arr = resolver.resolveTransitively(project
						.getDependencyArtifacts(), project.getArtifact(),
						remoteRepositories, localRepository,
						artifactMetadataSource);
			} catch (ArtifactResolutionException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			} catch (ArtifactNotFoundException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
			Set<Artifact> result = arr.getArtifacts();
			dependencyArtifacts = result;
		}
		return dependencyArtifacts;
	}

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

	protected File[] getSourcePaths() {
		getLog()
				.info(
						"sourcePaths CoC, using source directory plus resources directory!");
		List<File> files = new ArrayList<File>();

		File source = new File(build.getSourceDirectory());
		if (source.exists()) {
			files.add(source);
		}

		List<Resource> resources = getResources();
		for (Resource resource : resources) {
			File resourceFile = new File(resource.getDirectory());
			if (resourceFile.exists()) {
				files.add(resourceFile);
			}
		}

		return files.toArray(new File[files.size()]);
	}

	protected void urlToFile(URL url, File file) throws MojoExecutionException {
		try {
			FileUtils.copyURLToFile(url, file);
		} catch (IOException e) {
			throw new MojoExecutionException("?!");
		}
	}

}