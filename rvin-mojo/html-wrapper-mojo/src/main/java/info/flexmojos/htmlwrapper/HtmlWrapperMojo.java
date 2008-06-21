package info.flexmojos.htmlwrapper;

import info.rvin.flexmojos.utilities.CompileConfigurationLoader;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import eu.cedarsoft.utils.ZipExtractor;

/**
 *
 * @phase generate-resources
 * @goal wrapper
 *
 * @author marvin
 *
 */
public class HtmlWrapperMojo extends AbstractMojo {

	/**
	 * The maven project.
	 *
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * @parameter expression="${project.build}"
	 * @required
	 * @readonly
	 */
	protected Build build;

	/**
	 * The template URI.
	 * <p>
	 * This mojos embed the following URIs:
	 * <ul>
	 * class:client-side-detection
	 * </ul>
	 * <ul>
	 * class:client-side-detection-with-history
	 * </ul>
	 * <ul>
	 * class:express-installation
	 * </ul>
	 * <ul>
	 * class:express-installation-with-history
	 * </ul>
	 * <ul>
	 * class:no-player-detection
	 * </ul>
	 * <ul>
	 * class:no-player-detection-with-history
	 * </ul>
	 *
	 * To point to file system you must use a URI like this:
	 * file:/myTemplateFolder/template.zip
	 * <p>
	 * This mojo will look for <tt>index.template.html</tt> for replace
	 * parameters
	 *
	 * @parameter default-value="class:client-side-detection-with-history.zip"
	 */
	private String templateURI;

	/**
	 * Used to define parameters that will be replaced.
	 *
	 * Usage:
	 *
	 * <pre>
	 *  &lt;parameters&gt;
	 *  	&lt;swf&gt;${build.finalName}&lt;/swf&gt;
	 *  	&lt;width&gt;100%&lt;/width&gt;
	 *  	&lt;height&gt;100%&lt;/height&gt;
	 *  &lt;/parameters&gt;
	 * </pre>
	 *
	 * The following prameters wil be injected if not defined:
	 * <ul>
	 * title
	 * </ul>
	 * <ul>
	 * version_major
	 * </ul>
	 * <ul>
	 * version_minor
	 * </ul>
	 * <ul>
	 * version_revision
	 * </ul>
	 * <ul>
	 * swf
	 * </ul>
	 * <ul>
	 * width
	 * </ul>
	 * <ul>
	 * height
	 * </ul>
	 * <ul>
	 * bgcolor
	 * </ul>
	 * <ul>
	 * application
	 * </ul>
	 *
	 * If you are using a custom template, and wanna some extra parameters, this
	 * is the right place to define it.
	 *
	 * @parameter
	 */
	private Map<String, String> parameters;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			getLog().error(
					getClass().getResource(
							"/client-side-detection-with-history.zip").toURI()
							.toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		init();

		File templateFolder = extractTemplate();
		File targetFolder = new File(build.getDirectory());
		copyTemplates(templateFolder, targetFolder);
		copyIndex(templateFolder, targetFolder);
	}

	private void copyIndex(File templateFolder, File targetFolder)
			throws MojoExecutionException {
		File indexTemplate = new File(templateFolder, "index.template.html");
		String template;
		try {
			template = FileUtils.readFileToString(indexTemplate);
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to read " + indexTemplate,
					e);
		}

		for (String key : parameters.keySet()) {
			String value = parameters.get(key);
			template = template.replace("${" + key + "}", value);
		}

		File index = new File(targetFolder, build.getFinalName() + ".html");

		try {
			FileUtils.writeStringToFile(index, template);
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to write " + index, e);
		}
	}

	private void copyTemplates(File templateFolder, File targetFolder)
			throws MojoExecutionException {
		try {
			FileUtils.copyDirectory(templateFolder, targetFolder,
					new FileFilter() {
						public boolean accept(File pathname) {
							return !"index.template.html".equals(pathname
									.getName());
						}
					});
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to create templates.", e);
		}
	}

	private File extractTemplate() throws MojoExecutionException {
		getLog().info("Extracting template");
		File template = new File(build.getOutputDirectory(), "template.zip");
		URL url;
		try {
			URI uri = new URI(templateURI);
			url = getUrl(uri);
		} catch (Exception e) {
			throw new MojoExecutionException("Invalid template URI.", e);
		}

		try {
			FileUtils.copyURLToFile(url, template);
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to copy template to: "
					+ template, e);
		}

		File outputDir = new File(build.getOutputDirectory(), "html-template");
		outputDir.mkdirs();

		try {
			ZipExtractor ze = new ZipExtractor(template);
			ze.extract(outputDir);
		} catch (IOException e) {
			throw new MojoExecutionException(
					"An error happens when trying to extract html-template.", e);
		}

		return outputDir;
	}

	/**
	 *
	 * Based on URIUtil from Gas3 Franck WOLFF
	 *
	 * @throws IOException
	 *
	 */
	private URL getUrl(URI uri) throws IOException {
		URL url;
		String scheme = uri.getScheme();
		if ("class".equals(scheme)) {
			url = Thread.currentThread().getContextClassLoader().getResource(
					uri.getSchemeSpecificPart());
			if (url == null)
				throw new IOException("Resource not found exception: " + uri);
		} else
		// scheme.length() == 1 -> assume drive letter.
		if (scheme == null || scheme.length() <= 1)
			url = new URL(uri.toString());
		else
			url = uri.toURL();

		return url;
	}

	private void init() {
		if (parameters == null) {
			parameters = new HashMap<String, String>();
		}

		if (!parameters.containsKey("title")) {
			parameters.put("title", project.getName());
		}

		String targetPlayer = CompileConfigurationLoader
				.getCompilerPluginSetting(project, "targetPlayer");
		String[] nodes = targetPlayer != null ? targetPlayer.split("\\.")
				: new String[] { "9", "0", "0" };
		if (!parameters.containsKey("version_major")) {
			parameters.put("version_major", nodes[0]);
		}
		if (!parameters.containsKey("version_minor")) {
			parameters.put("version_minor", nodes[1]);
		}
		if (!parameters.containsKey("version_revision")) {
			parameters.put("version_revision", nodes[2]);
		}
		if (!parameters.containsKey("swf")) {
			parameters.put("swf", project.getBuild().getFinalName());
		}
		if (!parameters.containsKey("width")) {
			parameters.put("width", "100%");
		}
		if (!parameters.containsKey("height")) {
			parameters.put("height", "100%");
		}
		if (!parameters.containsKey("application")) {
			parameters.put("application", project.getArtifactId());
		}
		if (!parameters.containsKey("bgcolor")) {
			parameters.put("bgcolor", "#869ca7");
		}
	}

}
