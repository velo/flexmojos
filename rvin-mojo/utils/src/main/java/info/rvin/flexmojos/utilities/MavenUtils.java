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
	 * @param artifact
	 *            Artifact to be resolved
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

	@SuppressWarnings("unchecked")
	public static File[] getSourcePaths(Build build) {
		return getFiles(build.getSourceDirectory(), build.getResources());
	}

	@SuppressWarnings("unchecked")
	public static File[] getTestSourcePaths(Build build) {
		return getFiles(build.getTestSourceDirectory(), build.getTestResources());
	}
	
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
	 * Extract an property from pom.xml
	 * 
	 * @param project
	 * @param optionName
	 * @return
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
	 * @param project
	 *            maven project
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
