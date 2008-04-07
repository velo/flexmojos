package info.rvin.flexmojo.test;

import info.rvin.mojo.flexmojo.AbstractIrvinMojo;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.codehaus.plexus.velocity.VelocityComponent;

/**
 * @goal test-compile
 * @requiresDependencyResolution
 * @phase test
 */
public class TestCompilerMojo extends AbstractIrvinMojo {

	/**
	 * @component
	 */
	private VelocityComponent velocityComponent;

	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws MojoExecutionException, MojoFailureException {
		File testFolder = new File(build.getTestSourceDirectory());
		if (!testFolder.exists()) {
			getLog().warn("Test folder not found" + testFolder);
		}

		Collection<File> testFiles = FileUtils.listFiles(testFolder,
				new FileFileFilter() {
					@Override
					public boolean accept(File file) {
						String name = file.getName();
						if (name.startsWith("Test")
								&& (name.endsWith(".as") || name
										.endsWith(".mxml")))
							return true;
						return false;
					}
				}, DirectoryFileFilter.DIRECTORY);

		List<String> testClasses = new ArrayList<String>();

		int trimPoint = testFolder.getAbsolutePath().length() + 1;

		for (File testFile : testFiles) {
			String testClass = testFile.getAbsolutePath();
			int endPoint = testClass.lastIndexOf('.');
			testClass = testClass.substring(trimPoint, endPoint);
			testClass = testClass.replace('/', '.'); // Unix OS
			testClass = testClass.replace('\\', '.'); // Windows OS
			testClasses.add(testClass);
		}

		try {
			generateTester(testClasses);
		} catch (Exception e) {
			throw new MojoExecutionException(
					"Unable to generate tester class.", e);
		}
	}

	private void generateTester(List<String> testClasses) throws Exception {
		VelocityContext context = new VelocityContext();
		context.put("testClasses", testClasses);

		Template template = velocityComponent.getEngine().getTemplate(
				"/test/TestRunner.vm");

		Writer writer = null;
		try {
			File testOutputDirectory = new File(build.getTestOutputDirectory());
			if(!testOutputDirectory.exists()) {
				testOutputDirectory.mkdirs();
			}
			writer = new FileWriter(new File(testOutputDirectory,
					project.getArtifactId() + "TestRunner"
							+ project.getVersion() + ".mxml"));
			template.merge(context, writer);
		} finally {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		}
	}

	@Override
	protected void run() throws MojoExecutionException, MojoFailureException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void tearDown() throws MojoExecutionException,
			MojoFailureException {
		// TODO Auto-generated method stub

	}

}
