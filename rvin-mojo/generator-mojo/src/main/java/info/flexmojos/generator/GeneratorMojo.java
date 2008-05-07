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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.granite.generator.GenerationListener;
import org.granite.generator.Generator;
import org.granite.generator.as3.As3TypeFactory;
import org.granite.generator.as3.DefaultAs3TypeFactory;
import org.granite.generator.as3.JavaFileGenerationUnit;

/**
 * Goal which touches a timestamp file.
 *
 * @goal generate
 * @phase package
 */
public class GeneratorMojo extends AbstractMojo {

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

		Collection<String> classes = getClasses();

		FileClassLoader loader = new FileClassLoader(build.getOutputDirectory());

		Generator<Class<?>, JavaFileGenerationUnit> generator = getGenerator();

		int count = 0;
		for (String className : classes) {
			Class<?> clazz = null;
			try {
				clazz = loader.loadClass(className);
				count += generator.generate(clazz);
			} catch (Exception e) {
				getLog().error(
						"Could not generate AS3 beans for: " + className, e);
				throw new MojoExecutionException(
						"Could not generate AS3 beans for: " + clazz, e);
			}
		}
		getLog().info( count + " files generated.");
	}

	private Generator<Class<?>, JavaFileGenerationUnit> getGenerator() {
		As3TypeFactory as3TypeFactory = new DefaultAs3TypeFactory();

		FlexmojosAs3Controller controller =
			new FlexmojosAs3Controller(as3TypeFactory);
		controller.setStyle(style);
		controller.setBeanTemplateUris(beanTemplate);
		controller.setEntityTemplateUris(entityTemplate);
		controller.setEnumTemplateUris(enumTemplate);
		controller.setInterfaceTemplateUris(interfacetemplate);
		controller.setOutputDirectory(outputDirectory);
		controller.setUid(uid);

//			new JavaAs3Controller(
//				new GenLogger(getLog()), as3TypeFactory, outputdir
//						.getAbsolutePath(), uid, translators, null, null, null,
//				null, null, null, null, false);

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
	private List<String> getClasses() {
		Collection<File> includeFiles = FileUtils.listFiles(classesDirectory,
				new WildcardFileFilter(includeClasses),
				DirectoryFileFilter.DIRECTORY);

		if (excludeClasses != null && excludeClasses.length > 0) {
			getLog()
					.debug("excludeTestFiles: " + Arrays.asList(excludeClasses));
			Collection<File> excludedTestFiles = FileUtils.listFiles(
					classesDirectory, new WildcardFileFilter(excludeClasses),
					DirectoryFileFilter.DIRECTORY);
			includeFiles.removeAll(excludedTestFiles);
		}

		List<String> classes = new ArrayList<String>();
		for (File classFile : includeFiles) {
			if (!classFile.getName().endsWith(".class"))
				continue;
			String className = classFile.getAbsolutePath();
			className = className
					.substring(build.getOutputDirectory().length());
			className = className.replace(File.separatorChar, '.');
			className = className.substring(1, className.length() - 6);
			classes.add(className);
		}
		return classes;
	}

	private void setUp() throws MojoExecutionException {
		if (includeClasses == null) {
			includeClasses = new String[] { "*.class" };
		}

		classesDirectory = new File(build.getOutputDirectory());

		if (outputDirectory == null) {
			outputDirectory = new File(build.getDirectory(), "as3-generated");
			outputDirectory.mkdirs();
		}

	}

}
