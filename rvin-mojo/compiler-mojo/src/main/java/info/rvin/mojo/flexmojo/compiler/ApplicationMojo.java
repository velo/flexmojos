package info.rvin.mojo.flexmojo.compiler;

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

import flex2.tools.oem.Application;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal compile-swf
 * @requiresDependencyResolution
 * @phase compile
 */
public class ApplicationMojo extends AbstractFlexCompilerMojo<Application> {

	/**
	 * The file to be compiled
	 * 
	 * @parameter
	 */
	private String sourceFile;
	/**
	 * The file to be compiled
	 */
	private File source;

	@Override
	public void setUp() throws MojoExecutionException, MojoFailureException {
		File sourceDirectory = new File(build.getSourceDirectory());
		if (!sourceDirectory.exists()) {
			throw new MojoExecutionException(
					"Unable to found sourceDirectory: " + sourceDirectory);
		}

		source = resolveSourceFile(project, sourceFile);

		if (source == null) {
			throw new MojoExecutionException(
					"Source file not expecified and no default found!");
		}
		if (!source.exists()) {
			throw new MojoFailureException("Unable to find " + sourceFile);
		}

		// need to initialize builder before go super
		try {
			builder = new Application(source);
		} catch (FileNotFoundException e) {
			throw new MojoFailureException("Unable to find " + source);
		}
		super.setUp();

		builder.setOutput(outputFile);

	}

	@Override
	protected void writeResourceBundle(String[] bundles)
			throws MojoExecutionException {
		throw new MojoExecutionException(
				"Can't use flex-mojos to generate localized SWFs.\n"
						+ "Please see https://bugs.adobe.com/jira/browse/SDK-15139 for details");
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
}
