package info.rvin.flexmojos.utilities;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

public class MavenUtils {

	private MavenUtils() {
	}

	/**
	 * @param project
	 *            maven project
	 * @param sourceFile
	 *            sugested name on pom
	 * @return
	 */
	public static File resolveSourceFile(MavenProject project, String sourceFile) {

		File sourceDirectory = new File(project.getBuild().getSourceDirectory());

		if (sourceFile != null) {
			return new File(sourceDirectory, sourceFile);
		} else {
			File[] files = sourceDirectory.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isFile();
				}
			});

			if (files.length == 1) {
				return files[0];
			}
			if (files.length > 1) {
				for (File file : files) {
					if (file.getName().equalsIgnoreCase("main.mxml")
							|| file.getName().equalsIgnoreCase("main.as")) {
						return file;
					}
				}
			}
			return null;
		}
	}

	/**
	 * @param project 
	 * @param resolver
	 * @param localRepository
	 * @param remoteRepositories
	 * @param artifactMetadataSource
	 * @return all dependencies from the project
	 * @throws MojoExecutionException
	 */
	@SuppressWarnings("unchecked")
	public static Set<Artifact> getDependencyArtifacts(MavenProject project,
			ArtifactResolver resolver, ArtifactRepository localRepository,
			List remoteRepositories,
			ArtifactMetadataSource artifactMetadataSource)
			throws MojoExecutionException {
		ArtifactResolutionResult arr;
		try {
			arr = resolver
					.resolveTransitively(project.getDependencyArtifacts(),
							project.getArtifact(), remoteRepositories,
							localRepository, artifactMetadataSource);
		} catch (ArtifactResolutionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (ArtifactNotFoundException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
		Set<Artifact> result = arr.getArtifacts();
		return result;
	}

	/**
	 * @param artifact Artifact to be resolved
	 * @param resolver
	 * @param localRepository
	 * @param remoteRepositories
	 * @throws MojoExecutionException
	 */
	@SuppressWarnings("unchecked")
	public static void resolveArtifact(Artifact artifact,
			ArtifactResolver resolver, ArtifactRepository localRepository,
			List remoteRepositories) throws MojoExecutionException {
		try {
			resolver.resolve(artifact, remoteRepositories, localRepository);
		} catch (ArtifactResolutionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (ArtifactNotFoundException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

}
