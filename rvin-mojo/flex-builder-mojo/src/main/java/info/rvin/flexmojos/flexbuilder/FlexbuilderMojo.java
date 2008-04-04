package info.rvin.flexmojos.flexbuilder;

import info.rvin.flexmojos.flexbuilder.util.CompileConfigurationLoader;
import info.rvin.flexmojos.utilities.MavenUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.eclipse.EclipsePlugin;
import org.apache.maven.plugin.ide.IdeDependency;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.codehaus.plexus.velocity.VelocityComponent;

/**
 * @extendsPlugin eclipse
 * @extendsGoal eclipse
 * 
 * @goal flexbuilder
 * @phase package
 * @requiresDependencyResolution
 */
public class FlexbuilderMojo extends EclipsePlugin {

	/**
	 * @component
	 */
	private VelocityComponent velocityComponent;

	@Override
	public void writeConfiguration(IdeDependency[] deps)
			throws MojoExecutionException {
		super.writeConfiguration(deps);

		try {
			writeAsProperties();
		} catch (Exception e) {
			throw new MojoExecutionException(
					"Error writting .actionScriptProperties", e);
		}

	}

	/**
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 */
	@SuppressWarnings("unchecked")
	protected List remoteRepositories;
	/**
	 * @component
	 */
	protected ArtifactResolver resolver;

	@SuppressWarnings("unchecked")
	protected Set<Artifact> getDependencyArtifacts()
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

	private void writeAsProperties() throws Exception {
		VelocityContext context = new VelocityContext();
		context.put("dependencies", getDependencyArtifacts());
		context.put("mainApplication", MavenUtils.resolveSourceFile(project,
				CompileConfigurationLoader.getCompilerPluginSetting(project,
						"sourceFile")));

		Template template = velocityComponent.getEngine().getTemplate(
				"/actionScriptProperties.vm");

		Writer writer = null;
		try {
			writer = new FileWriter(new File(project.getBasedir(),
					".actionScriptProperties"));
			template.merge(context, writer);
		} finally {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		}
	}

}
