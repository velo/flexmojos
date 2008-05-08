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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.granite.generator.GenerationListener;
import org.granite.generator.Generator;
import org.granite.generator.as3.As3TypeFactory;
import org.granite.generator.as3.DefaultAs3TypeFactory;
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

	private File classesDirectory;

	/**
	 * @parameter
	 */
	private File outputDirectory;

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
	private String[] interfacetemplate;

	/**
	 * @parameter
	 */
	private String[] beanTemplate;

	/**
	 * @parameter
	 */
	private String[] enumTemplate;

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

		Generator<Class<?>, JavaFileGenerationUnit> generator = getGenerator();

		int count = 0;
		for (String className : classes) {
			Class<?> clazz = null;
			try {
				clazz = loader.loadClass(className);
				count += generator.generate(clazz);
			} catch (Exception e) {
				throw new MojoExecutionException(
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
		Set<Artifact> artifacts = project.getDependencyArtifacts();
		for (Artifact artifact : artifacts) {
			if ("jar".equals(artifact.getType())) {
				jarDependencies.add(artifact.getFile());
			}
		}
		return jarDependencies;
	}

	private Generator<Class<?>, JavaFileGenerationUnit> getGenerator() {
		As3TypeFactory as3TypeFactory = new DefaultAs3TypeFactory();

		FlexmojosAs3Controller controller = new FlexmojosAs3Controller(
				as3TypeFactory);
		controller.setStyle(style);
		controller.setBeanTemplateUris(beanTemplate);
		controller.setEntityTemplateUris(entityTemplate);
		controller.setEnumTemplateUris(enumTemplate);
		controller.setInterfaceTemplateUris(interfacetemplate);
		controller.setOutputDirectory(outputDirectory);
		controller.setUid(uid);

		// new JavaAs3Controller(
		// new GenLogger(getLog()), as3TypeFactory, outputdir
		// .getAbsolutePath(), uid, translators, null, null, null,
		// null, null, null, null, false);

		GenerationListener<JavaFileGenerationUnit> listener = new GenerationListener<JavaFileGenerationUnit>() {
			public void generating(JavaFileGenerationUnit unit) {
				getLog().info("  Generating: " + unit.getOutput());
			}
		};

		Generator<Class<?>, JavaFileGenerationUnit> generator = new Generator<Class<?>, JavaFileGenerationUnit>(
				controller, listener);
		return generator;
	}

	@SuppressWarnings("unchecked")
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

				if (!jarEntry.isDirectory() && className.endsWith(".class")
						&& matchWildCard(className, includeClasses)
						&& !matchWildCard(className, excludeClasses)) {
					className = className.replace('/', '.');
					className = className.substring(0, className.length() - 6);
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
			return FilenameUtils.wildcardMatch(className, wildCard);
		}

		return false;
	}

	private void setUp() throws MojoExecutionException {
		if (includeClasses == null) {
			includeClasses = new String[] { "*.class" };
		}

		classesDirectory = new File(build.getOutputDirectory());

		if (outputDirectory == null) {
			outputDirectory = new File(build.getDirectory(), "generated-sources");
			outputDirectory.mkdirs();
		}

	}

}
