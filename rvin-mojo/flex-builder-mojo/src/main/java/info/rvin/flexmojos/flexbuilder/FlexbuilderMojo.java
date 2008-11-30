/**
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.rvin.flexmojos.flexbuilder;

import info.rvin.flexmojos.utilities.CompileConfigurationLoader;
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
