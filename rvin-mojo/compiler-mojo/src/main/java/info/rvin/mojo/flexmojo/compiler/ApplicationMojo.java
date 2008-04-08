/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

import static info.rvin.flexmojos.utilities.MavenUtils.resolveSourceFile;
import static java.util.Arrays.asList;
import flex2.tools.oem.Application;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

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
	protected String sourceFile;

	/**
	 * The file to be compiled
	 */
	protected File source;

	@Override
	public void setUp() throws MojoExecutionException, MojoFailureException {
		File sourceDirectory = new File(build.getSourceDirectory());
		if (!sourceDirectory.exists()) {
			throw new MojoExecutionException(
					"Unable to found sourceDirectory: " + sourceDirectory);
		}

		if (source == null) {
			source = resolveSourceFile(project, sourceFile);
		}

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
	protected void writeResourceBundle(String[] bundles, String locale,
			File localePath) throws MojoExecutionException {

		// Dont break this method in parts, is a work around

		File output = new File(build.getDirectory(), project.getArtifactId()
				+ "-" + project.getVersion() + "-" + locale + ".swf");

		/*
		 * mxmlc -locale=en_US -source-path=locale/{locale}
		 * -include-resource-bundles=FlightReservation2,SharedResources,collections,containers,controls,core,effects,formatters,skins,styles
		 * -output=src/Resources_en_US.swf
		 */

		String bundlesString = Arrays.toString(bundles) //
				.replace("[", "") // remove start [
				.replace("]", "") // remove end ]
				.replace(", ", ","); // remove spaces

		ArrayList<File> external = new ArrayList<File>();
		ArrayList<File> internal = new ArrayList<File>();
		ArrayList<File> merged = new ArrayList<File>();

		external.addAll(asList(getDependenciesPath("external")));
		external.addAll(asList(getDependenciesPath("rsl")));

		internal.addAll(asList(getDependenciesPath("internal")));

		merged.addAll(asList(getDependenciesPath("compile")));
		merged.addAll(asList(getDependenciesPath("merged")));
		merged.addAll(asList(getResourcesBundles()));

		Set<String> args = new HashSet<String>();
		// args.addAll(Arrays.asList(configs));
		args.add("-locale=" + locale);
		args.add("-source-path=" + localePath.getAbsolutePath());
		args.add("-include-resource-bundles=" + bundlesString);
		args.add("-output=" + output.getAbsolutePath());
		args.add("-compiler.fonts.local-fonts-snapshot="
				+ getFontsSnapshot().getAbsolutePath());
		args.add("-load-config=" + configFile.getAbsolutePath());
		args.add("-external-library-path=" + toString(external));
		args.add("-include-libraries=" + toString(internal));
		args.add("-library-path=" + toString(merged));

		// Just a work around
		// TODO https://bugs.adobe.com/jira/browse/SDK-15139
		flex2.tools.Compiler.mxmlc(args.toArray(new String[args.size()]));

		projectHelper.attachArtifact(project, "swf", locale, output);
	}

	private String toString(List<File> libs) {
		StringBuilder sb = new StringBuilder();
		for (File lib : libs) {
			if (sb.length() != 0) {
				sb.append(',');
			}

			sb.append(lib.getAbsolutePath());
		}
		return sb.toString();
	}

}
