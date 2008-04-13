package info.rvin.flexmojos.utilities;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Utility class to help get information from Maven objects
 * like files, source paths, resolve dependencies, etc.
 * 
 * @author velo.br
 *
 */
public class MavenUtils {

	private MavenUtils() {
	}

	/**
	 * Resolve a source file in a maven project
	 * 
	 * @param project
	 *            maven project
	 * @param sourceFile
	 *            sugested name on pom
	 * @return
	 * 			source file or null if source not found 
	 */
	public static File resolveSourceFile(MavenProject project, String sourceFile) {

		File sourceDirectory = new File(project.getBuild().getSourceDirectory());

		if (sourceFile != null) {
			return new File(sourceDirectory, sourceFile);
		} else {
			File[] files = sourceDirectory.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isFile()
							&& (pathname.getName().endsWith(".mxml") || pathname
									.getName().endsWith(".as"));
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
	 * Get dependency artifacts for a project using the local and remote repositories
	 * to resolve the artifacts
	 * 
	 * @param project
	 * 				maven project
	 * @param resolver
	 * 				artifact resolver
	 * @param localRepository
	 * 				artifact repository
	 * @param remoteRepositories
	 * 				List of remote repositories
	 * @param artifactMetadataSource
	 * 				artifactMetadataSource
	 * @return all dependencies from the project
	 * @throws MojoExecutionException
	 * 				thrown if an exception occured during artifact resolving
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
	 * Get the file reference of an SWC artifact.<br> 
	 * If the artifact file does not exist in the [build-dir]/libraries/[scope] directory, the artifact
	 * file is copied to that location.
	 * 
	 * @param a
	 * 			artifact for which to retrieve the file reference
	 * @param scope
	 * 			scope of the library
	 * @param build
	 * 			build for which to get the artifact
	 * @return swc artifact file reference
	 * @throws MojoExecutionException thrown if an IOException occurs while
	 * 			copying the file to the [build-dir]/libraries/[scope] directory
	 * 
	 */
	public static File getArtifactFile(Artifact a, String scope, Build build)
			throws MojoExecutionException {
		File dest = new File(build.getDirectory(), "libraries/" + scope + "/"
				+ a.getArtifactId() + ".swc");
		if (!dest.exists()) {
			try {
				FileUtils.copyFile(a.getFile(), dest);
			} catch (IOException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
		}
		return dest;
	}

	/**
	 * Use the resolver to resolve the given artifact in the local or remote
	 * repositories.
	 * 
	 * @param artifact
	 *            Artifact to be resolved
	 * @param resolver
	 * 			ArtifactResolver to use for resolving the artifact
	 * @param localRepository
	 * 			ArtifactRepository
	 * @param remoteRepositories
	 * 			List of remote artifact repositories
	 * @throws MojoExecutionException
	 * 			thrown if an exception occured during artifact resolving
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

	/**
	 * Get the source paths for all resources in the source directory.
	 * 
	 * @param build
	 * 			Build for this to get all source paths
	 * @return
	 * 			Array of source paths for all resources in the source directory
	 */
	@SuppressWarnings("unchecked")
	public static File[] getSourcePaths(Build build) {
		return getFiles(build.getSourceDirectory(), build.getResources());
	}

	/**
	 * Get the test-source paths for all resources in the test-source directory.
	 * 
	 * @param build
	 * 			Build for this to get all test-source paths
	 * @return
	 * 			Array of test-source paths for all resources in the test-source directory
	 */
	@SuppressWarnings("unchecked")
	public static File[] getTestSourcePaths(Build build) {
		return getFiles(build.getTestSourceDirectory(), build.getTestResources());
	}

	/**
	 * Get array of Files for all resources in the resources list.
	 * 
	 * @param sourceDirectory
	 * 			path to source directory
	 * @param resources
	 * 			List of Resources
	 * @return
	 * 			Array of Files for given source directory and resources
	 */
	private static File[] getFiles(String sourceDirectory, List<Resource> resources) {
		List<File> files = new ArrayList<File>();
		
		File source = new File(sourceDirectory);
		if (source.exists()) {
			files.add(source);
		}
		
		for (Resource resource : resources) {
			File resourceFile = new File(resource.getDirectory());
			if (resourceFile.exists()) {
				files.add(resourceFile);
			}
		}
		
		return files.toArray(new File[files.size()]);
		
	}
	
	/**
	 * Returns file reference to config.xml file. Copies the config file to
	 * the build directory.
	 * 
	 * @param build
	 * 			Build for which to get the config.xml file
	 * @return 
	 * 			file reference to config.xml file
	 * @throws MojoExecutionException
	 * 			thrown if the config file could not be copied to the build directory
	 */
	public static File getConfigFile(Build build) throws MojoExecutionException {
		URL url = MavenUtils.class.getResource("/configs/config.xml");
		File configFile = new File(build.getDirectory(), "config.xml");
		try {
			FileUtils.copyURLToFile(url, configFile);
		} catch (IOException e) {
			throw new MojoExecutionException("Error generating config file.", e);
		}
		return configFile;
	}

	/**
	 * Returns the file reference to the fonts file. Depending on the os, the
	 * correct fonts.ser file is used.
	 * The fonts file is copied to the build directory.
	 * 
	 * @param build
	 * 			Build for which to get the fonts file
	 * @return file reference to fonts file
	 * @throws MojoExecutionException
	 * 			thrown if the config file could not be copied to the build directory
	 * 
	 * TODO Implement for linux?
	 */
	public static File getFontsFile(Build build) throws MojoExecutionException {
		String os = System.getProperty("os.name").toLowerCase();
		URL url;
		if (os.contains("mac")) {
			url = MavenUtils.class.getResource("/fonts/macFonts.ser");
		} else {
			// TODO And linux?!
			// if(os.contains("windows")) {
			url = MavenUtils.class.getResource("/fonts/winFonts.ser");
		}
		File fontsSer = new File(build.getDirectory(), "fonts.ser");
		try {
			FileUtils.copyURLToFile(url, fontsSer);
		} catch (IOException e) {
			throw new MojoExecutionException("Error copying fonts file.", e);
		}
		return fontsSer;
	}

	/**
	 * Returns the file reference to a localize resourceBundlePath. Replaces the {locale}
	 * variable in the given resourceBundlePath with given locale.
	 * 
	 * @param resourceBundlePath
	 * 			Path to resource bundle.
	 * @param locale
	 * 			Locale
	 * @throws MojoExecutionException thrown if the resourceBundlePath for
	 * 			given locale can not be found
	 * @return File reference to the resourceBundlePath for given locale
	 */
	public static File getLocaleResourcePath(String resourceBundlePath,
			String locale) throws MojoExecutionException {
		String path = resourceBundlePath.replace("{locale}", locale);
		File localePath = new File(path);
		if (!localePath.exists()) {
			throw new MojoExecutionException("Unable to find locales path: "
					+ path);
		}
		return localePath;
	}


	/**
	 * Extract an plugin setting property from pom.xml
	 * 
	 * @param project
	 * 			Maven project
	 * @param optionName
	 * 			Name of option to lookup
	 * @return
	 * 			Value of optionName
	 */
	@SuppressWarnings("unchecked")
	public static String getCompilerPluginSetting(MavenProject project,
			String optionName) {
		String value = findCompilerPluginSettingInPlugins(project.getModel()
				.getBuild().getPlugins(), optionName);
		if (value == null
				&& project.getModel().getBuild().getPluginManagement() != null) {
			value = findCompilerPluginSettingInPlugins(project.getModel()
					.getBuild().getPluginManagement().getPlugins(), optionName);
		}
		return value;
	}

	/**
	 * Returns a compiler plugin settings from a list of plugins .
	 * 
	 * @param plugins
	 *          List of plugins
	 * @param optionName
	 * 			Name of option to lookup
	 * @return option value (may be null)
	 */
	@SuppressWarnings("unchecked")
	private static String findCompilerPluginSettingInPlugins(
			List<Plugin> plugins, String optionName) {
		String value = null;

		for (Iterator<Plugin> it = plugins.iterator(); it.hasNext();) {
			Plugin plugin = (Plugin) it.next();

			if (plugin.getArtifactId().equals("flex-compiler-mojo")) {
				Xpp3Dom o = (Xpp3Dom) plugin.getConfiguration();

				// this is the default setting
				if (o != null && o.getChild(optionName) != null) {
					value = o.getChild(optionName).getValue();
				}

				List<PluginExecution> executions = plugin.getExecutions();

				// a different source/target version can be configured for test
				// sources compilation
				for (Iterator<PluginExecution> iter = executions.iterator(); iter
						.hasNext();) {
					PluginExecution execution = (PluginExecution) iter.next();
					o = (Xpp3Dom) execution.getConfiguration();

					if (o != null && o.getChild(optionName) != null) {
						value = o.getChild(optionName).getValue();
					}
				}
			}
		}
		return value;
	}
}
