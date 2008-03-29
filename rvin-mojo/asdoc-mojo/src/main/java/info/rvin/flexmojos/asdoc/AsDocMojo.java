package info.rvin.flexmojos.asdoc;

import flex2.tools.ASDoc;
import info.rvin.mojo.flexmojo.AbstractIrvinMojo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal asdoc
 * @requiresDependencyResolution
 */
public class AsDocMojo extends AbstractIrvinMojo {

	/**
	 * A list of classes to document. These classes must be in the source path.
	 * This is the default option.
	 * 
	 * This option works the same way as does the -include-classes option for
	 * the compc component compiler. For more information, see Using compc, the
	 * component compiler.
	 * 
	 * @parameter
	 */
	private String[] docClasses;

	/**
	 * A list of URIs whose classes should be documented. The classes must be in
	 * the source path.
	 * 
	 * You must include a URI and the location of the manifest file that defines
	 * the contents of this namespace.
	 * 
	 * This option works the same way as does the -include-namespaces option for
	 * the compc component compiler. For more information, see Using compc, the
	 * component compiler.
	 * 
	 * @parameter
	 */
	private Object[] docNamespaces;

	/**
	 * A list of files that should be documented. If a directory name is in the
	 * list, it is recursively searched.
	 * 
	 * This option works the same way as does the -include-sources option for
	 * the compc component compiler. For more information, see Using compc, the
	 * component compiler.
	 * 
	 * @parameter
	 * 
	 */
	private File[] docSources;

	/**
	 * A list of classes that should not be documented. You must specify
	 * individual class names. Alternatively, if the ASDoc comment for the class
	 * contains the
	 * 
	 * @private tag, is not documented.
	 * 
	 * @parameter
	 */
	private String[] excludeClasses;

	/**
	 * Whether all dependencies found by the compiler are documented. If true,
	 * the dependencies of the input classes are not documented.
	 * 
	 * The default value is false.
	 * 
	 * @parameter default-value="false"
	 */
	private boolean excludeDependencies;

	/**
	 * The text that appears at the bottom of the HTML pages in the output
	 * documentation.
	 * 
	 * @parameter
	 */
	private String footer;

	/**
	 * An integer that changes the width of the left frameset of the
	 * documentation. You can change this size to accommodate the length of your
	 * package names.
	 * 
	 * The default value is 210 pixels.
	 * 
	 * @parameter default-value="120"
	 * 
	 */
	private int leftFramesetWidth;

	/**
	 * The text that appears at the top of the HTML pages in the output
	 * documentation.
	 * 
	 * The default value is "API Documentation".
	 * 
	 * @parameter default-value="API Documentation"
	 */
	private String mainTitle;

	/**
	 * The output directory for the generated documentation. The default value
	 * is "asdoc-output".
	 * 
	 * @parameter
	 */
	private File output;

	/**
	 * The descriptions to use when describing a package in the documentation.
	 * You can specify more than one package option.
	 * 
	 * @parameter
	 */
	private Map<String, String> packageDescriptions;

	/**
	 * The path to the ASDoc template directory. The default is the
	 * asdoc/templates directory in the ASDoc installation directory. This
	 * directory contains all the HTML, CSS, XSL, and image files used for
	 * generating the output.
	 * 
	 * @parameter
	 */
	private File templatesPath;

	/**
	 * The text that appears in the browser window in the output documentation.
	 * 
	 * The default value is "API Documentation".
	 * 
	 * @parameter default-value="API Documentation"
	 */
	private String windowTitle;

	/**
	 * 
	 */
	private List<File> libraries;

	/**
	 * Load a file containing configuration options
	 * 
	 * @parameter
	 */
	private File configFile;

	private File fontsSnapshot;

	@Override
	protected void setUp() throws MojoExecutionException, MojoFailureException {
		if (docSources == null) {
			docSources = getSourcePaths();
		}

		libraries = new ArrayList<File>();
		for (Artifact artifact : getDependencyArtifacts()) {
			libraries.add(artifact.getFile());
		}

		if (output == null) {
			output = new File(build.getDirectory(), "asdoc");
			if (!output.exists()) {
				output.mkdirs();
			}
		}

		if (configFile == null) {
			List<Resource> resources = getResources();
			for (Resource resource : resources) {
				File cfg = new File(resource.getDirectory(),
						getConfigFileName());
				if (cfg.exists()) {
					configFile = cfg;
					break;
				}
			}
		}
		if (configFile == null) {
			URL url = getClass().getResource("/configs/" + getConfigFileName());
			configFile = new File(build.getDirectory(), getConfigFileName());
			urlToFile(url, configFile);
		}
		if (!configFile.exists()) {
			throw new MojoExecutionException("Unable to find " + configFile);
		}

		if (fontsSnapshot == null) {
			String os = System.getProperty("os.name").toLowerCase();
			URL url;
			if (os.contains("mac")) {
				url = getClass().getResource("/fonts/macFonts.ser");
			} else {
				// And linux?!
				// if(os.contains("windows")) {
				url = getClass().getResource("/fonts/winFonts.ser");
			}
			File fontsSer = new File(build.getDirectory(), "fonts.ser");
			urlToFile(url, fontsSer);
			fontsSnapshot = fontsSer;
		}

		if (templatesPath == null) {
			templatesPath = new File(build.getDirectory(), "templates");
			templatesPath.mkdirs();
			try {
				UnzipUtils.unzip(getClass().getResourceAsStream(
						"/asdoc/templates.zip"), templatesPath);
			} catch (IOException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
		}
	}

	private String getConfigFileName() {
		return "config.xml";
	}

	/*
	 * c:\flex\flex_sdk_3.0.0.477_mpl\bin\asdoc -doc-sources=./ -source-path=./
	 * -library-path+="c:\Program Files\Adobe\Flex Builder 3
	 * Plug-in\sdks\3.0.0\frameworks\libs\datavisualization.swc"
	 */
	@Override
	protected void run() throws MojoExecutionException, MojoFailureException {
		List<String> args = new ArrayList<String>();

		addDocSources(args);
		addSourcePath(args);
		addLibraries(args);
		addConfigFile(args);
		addFontsSnapshot(args);
		addTemplates(args);
		addOutput(args);

		System.out.println(args);

		// I hate this, waiting for asdoc-oem
		ASDoc.asdoc(args.toArray(new String[args.size()]));
	}

	private void addTemplates(List<String> args) {
		args.add("-templates-path=" + templatesPath.getAbsolutePath());
	}

	private void addOutput(List<String> args) {
		args.add("-output=" + output.getAbsolutePath());
	}

	private void addFontsSnapshot(List<String> args) {
		args.add("-compiler.fonts.local-fonts-snapshot="
				+ fontsSnapshot.getAbsolutePath());
	}

	private void addConfigFile(List<String> args) {
		args.add("-load-config=" + configFile.getAbsolutePath());
	}

	private void addLibraries(List<String> args) {
		StringBuilder sb = new StringBuilder();
		for (File lib : libraries) {
			if (sb.length() == 0) {
				sb.append("-library-path=");
			} else {
				sb.append(',');
			}

			sb.append(lib.getAbsolutePath());
		}

		args.add(sb.toString());
	}

	private void addSourcePath(List<String> args) {
		StringBuilder sb = new StringBuilder();
		for (File docs : docSources) {
			if (sb.length() == 0) {
				sb.append("-source-path=");
			} else {
				sb.append(',');
			}

			sb.append(docs.getAbsolutePath());
		}
		args.add(sb.toString());
	}

	private void addDocSources(List<String> args) {
		StringBuilder sb = new StringBuilder();
		for (File docs : docSources) {
			if (sb.length() == 0) {
				sb.append("-doc-sources=");
			} else {
				sb.append(',');
			}

			sb.append(docs.getAbsolutePath());
		}
		args.add(sb.toString());
	}

	@Override
	protected void tearDown() throws MojoExecutionException,
			MojoFailureException {

	}

}
