package info.flexmojos.generator;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.granite.generator.GenerationController;
import org.granite.generator.GenerationListener;
import org.granite.generator.Generator;
import org.granite.generator.as3.As3TemplatesType;
import org.granite.generator.as3.JavaAs3GenerationConfiguration;
import org.granite.generator.as3.JavaFileGenerationUnit;

/**
 * Goal which touches a timestamp file.
 *
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution
 */
public class GeneratorMojo extends AbstractMojo {

	/**
	 * The maven project.
	 *
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * File to generate as3 file.
	 *
	 * If not defined assumes all classes must be included
	 *
	 * @parameter
	 */
	private String[] includeClasses;

	/**
	 * File to exclude from as3 generation.
	 *
	 * If not defined, assumes no exclusions
	 *
	 * @parameter
	 */
	private String[] excludeClasses;

	/**
	 * @parameter expression="${project.build}"
	 * @required
	 * @readonly
	 */
	protected Build build;

	/**
	 * Defines the default as3 generation style to use.
	 *
	 * Valid values: <tt>granite-gas3</tt>
	 *
	 * @parameter default-value="granite-gas3"
	 */
	private String style;

	/**
	 * @parameter
	 *            default-value="${project.build.directory}/generated-sources/flex-mojos"
	 */
	private File outputDirectory;

	/**
	 * @parameter
	 */
	private File baseOutputDirectory;

	/**
	 * @parameter
	 */
	private String uid = "uid";

	/**
	 * @parameter
	 */
	private String[] entityTemplate;

	/**
	 * @parameter
	 */
	private String[] interfaceTemplate;

	/**
	 * @parameter
	 */
	private String[] beanTemplate;

	/**
	 * @parameter
	 */
	private String[] enumTemplate;

	/**
	 * @parameter default-value="false"
	 */
	private boolean useTransitiveDependencies;

	/**
	 * @parameter
	 */
	private String[] typeMappings;

	public void execute() throws MojoExecutionException {
		setUp();

		List<File> jarDependencies = getJarDependencies();
		if (jarDependencies.isEmpty()) {
			getLog().warn("No jar dependencies found.");
			return;
		}

		Collection<String> classes;
		try {
			classes = getClasses(jarDependencies);
		} catch (IOException e) {
			throw new MojoExecutionException("Error on classes resolve", e);
		}

		URLClassLoader loader;
		try {
			loader = URLClassLoader.newInstance(getUrls(jarDependencies));
		} catch (MalformedURLException e) {
			throw new MojoExecutionException("Unable to get dependency URL", e);
		}

		Generator<Class<?>, JavaFileGenerationUnit, As3TemplatesType, JavaAs3GenerationConfiguration> generator = getGenerator(loader);

		int count = 0;
		for (String className : classes) {
			Class<?> clazz = null;
			try {
				clazz = loader.loadClass(className);
				count += generator.generate(clazz);
			} catch (Exception e) {
				getLog().warn(
						"Could not generate AS3 beans for: '" + clazz + "'", e);
			}
		}
		getLog().info(count + " files generated.");
	}

	private URL[] getUrls(List<File> jarDependencies)
			throws MalformedURLException {
		URL[] urls = new URL[jarDependencies.size()];
		for (int i = 0; i < jarDependencies.size(); i++) {
			urls[i] = jarDependencies.get(i).toURL();
		}
		return urls;
	}

	@SuppressWarnings("unchecked")
	private List<File> getJarDependencies() {
		List<File> jarDependencies = new ArrayList<File>();
		final Collection<Artifact> artifacts;
		if (useTransitiveDependencies) {
			artifacts = project.getArtifacts();
		} else {
			artifacts = project.getDependencyArtifacts();
		}
		for (Artifact artifact : artifacts) {
			if ("jar".equals(artifact.getType())) {
				File file = artifact.getFile();
				if (file != null && file.exists()) {
					jarDependencies.add(file);
				} else {
					getLog().warn("Dependency file not found: " + artifact);
				}
			}
		}
		return jarDependencies;
	}

	private Generator<Class<?>, JavaFileGenerationUnit, As3TemplatesType, JavaAs3GenerationConfiguration> getGenerator(
			ClassLoader loader) throws MojoExecutionException {

		GenerationListener<JavaFileGenerationUnit> listener = new Gas3Listener(
				getLog());

		JavaAs3GenerationConfiguration configuration = new JavaAs3GenerationConfiguration(
				listener, new Gas3TypeFactory(loader, typeMappings, getLog()),
				outputDirectory.getPath(), baseOutputDirectory.getPath(), uid,
				null, get0(entityTemplate), get1(entityTemplate),
				get0(interfaceTemplate), get1(interfaceTemplate),
				get0(beanTemplate), get1(beanTemplate), get0(enumTemplate),
				false);

		GenerationController<Class<?>, JavaFileGenerationUnit, As3TemplatesType, JavaAs3GenerationConfiguration> controller = new Gas3Controller(
				configuration);

		return new Generator<Class<?>, JavaFileGenerationUnit, As3TemplatesType, JavaAs3GenerationConfiguration>(
				controller, listener);

	}

	private List<String> getClasses(List<File> jarDependencies)
			throws IOException {
		List<String> classes = new ArrayList<String>();
		for (File file : jarDependencies) {
			JarInputStream jar = new JarInputStream(new FileInputStream(file));

			JarEntry jarEntry;
			while (true) {
				jarEntry = jar.getNextJarEntry();

				if (jarEntry == null) {
					break;
				}

				String className = jarEntry.getName();

				if (jarEntry.isDirectory() || !className.endsWith(".class")) {
					continue;
				}

				className = className.replace('/', '.');
				className = className.substring(0, className.length() - 6);

				if (matchWildCard(className, includeClasses)
						&& !matchWildCard(className, excludeClasses)) {
					classes.add(className);
				}
			}
		}

		return classes;
	}

	private boolean matchWildCard(String className, String[] wildCards) {
		if (wildCards == null) {
			return false;
		}

		for (String wildCard : wildCards) {
			if (FilenameUtils.wildcardMatch(className, wildCard))
				return true;
		}

		return false;
	}

	private void setUp() throws MojoExecutionException {
		if (includeClasses == null) {
			includeClasses = new String[] { "*" };
		}

		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}

		if (baseOutputDirectory == null) {
			baseOutputDirectory = outputDirectory;
		} else if (!baseOutputDirectory.exists()) {
			baseOutputDirectory.mkdirs();
		}

	}

	private String get0(String[] a) {
		return a == null ? null : (a.length < 1 ? null : a[0]);
	}

	private String get1(String[] a) {
		return a == null ? null : (a.length < 2 ? null : a[1]);
	}

}
