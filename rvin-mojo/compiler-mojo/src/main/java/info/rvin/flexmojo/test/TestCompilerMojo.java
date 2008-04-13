package info.rvin.flexmojo.test;

import info.rvin.flexmojos.utilities.MavenUtils;
import info.rvin.mojo.flexmojo.compiler.ApplicationMojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal to compile the Flex test sources.
 * 
 * @goal test-compile
 * @requiresDependencyResolution
 * @phase test
 */
public class TestCompilerMojo extends ApplicationMojo {

	/**
	 * Set this to 'true' to bypass unit tests entirely. Its use is NOT
	 * RECOMMENDED, but quite convenient on occasion.
	 * 
	 * @parameter expression="${maven.test.skip}"
	 */
	private boolean skipTests;

	/**
	 * @parameter
	 */
	private File testRunnerTemplate;

	/**
	 * File to be tested.
	 * 
	 * If not defined assumes Test*.as and *Test.as
	 * 
	 * @parameter
	 */
	private String[] includeTestFiles;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		File testFolder = new File(build.getTestSourceDirectory());
		if (skipTests) {
			getLog().warn("Skipping test phase.");
		} else if (!testFolder.exists()) {
			getLog().warn("Test folder not found" + testFolder);
		} else {
			super.execute();
		}
	}

	@Override
	public void setUp() throws MojoExecutionException, MojoFailureException {
		if (includeTestFiles == null) {
			includeTestFiles = new String[] { "Test*.as", "*Test.as" };
		}

		File outputFolder = new File(build.getTestOutputDirectory());
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}

		File testFolder = new File(build.getTestSourceDirectory());
		List<String> testClasses = getTestClasses(testFolder);

		File testSourceFile;
		try {
			testSourceFile = generateTester(testClasses);
		} catch (Exception e) {
			throw new MojoExecutionException(
					"Unable to generate tester class.", e);
		}

		sourceFile = null;
		source = testSourceFile;

		outputFile = new File(build.getTestOutputDirectory(), "TestRunner.swf");

		super.setUp();
	}

	@SuppressWarnings("unchecked")
	private List<String> getTestClasses(File testFolder) {
		Collection<File> testFiles = FileUtils.listFiles(testFolder,
				new WildcardFileFilter(includeTestFiles),
				DirectoryFileFilter.DIRECTORY);

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
		return testClasses;
	}

	private File generateTester(List<String> testClasses) throws Exception {
		// can't use velocity, got:
		// java.io.InvalidClassException:
		// org.apache.velocity.runtime.parser.node.ASTprocess; class invalid for
		// deserialization

		StringBuilder imports = new StringBuilder();

		for (String testClass : testClasses) {
			imports.append("import ");
			imports.append(testClass);
			imports.append(";");
			imports.append('\n');
		}

		StringBuilder classes = new StringBuilder();

		for (String testClass : testClasses) {
			testClass = testClass.substring(testClass.lastIndexOf('.') + 1);
			classes.append("testSuite.addTest( new TestSuite(");
			classes.append(testClass);
			classes.append(") );");
			classes.append('\n');
		}

		InputStream templateSource = getTemplate();
		String sourceString = IOUtils.toString(templateSource);
		sourceString = sourceString.replace("$imports", imports);
		sourceString = sourceString.replace("$testClasses", classes);
		File testSourceFile = new File(build.getTestOutputDirectory(),
				"TestRunner.mxml");
		FileWriter fileWriter = new FileWriter(testSourceFile);
		IOUtils.write(sourceString, fileWriter);
		fileWriter.flush();
		fileWriter.close();
		return testSourceFile;
	}

	private InputStream getTemplate() throws MojoExecutionException {
		if (testRunnerTemplate == null) {
			return getClass().getResourceAsStream("/test/TestRunner.vm");
		} else if (!testRunnerTemplate.exists()) {
			throw new MojoExecutionException("Template file not found: "
					+ testRunnerTemplate);
		} else {
			try {
				return new FileInputStream(testRunnerTemplate);
			} catch (FileNotFoundException e) {
				// Never should happen
				throw new MojoExecutionException("Error reading template file",
						e);
			}
		}
	}

	@Override
	protected void configure() throws MojoExecutionException {
		super.configure();

		// Remove all libraries
		configuration.setExternalLibraryPath(null);
		configuration.setIncludes(null);
		configuration.setRuntimeSharedLibraries(null);

		// Set all dependencies as merged
		configuration.setLibraryPath(getDependenciesPath("compile"));
		configuration.addLibraryPath(getDependenciesPath("merged"));
		configuration.addLibraryPath(getDependenciesPath("external"));
		configuration.addLibraryPath(getResourcesBundles());
		configuration.addLibraryPath(getDependenciesPath("internal"));
		configuration.addLibraryPath(getDependenciesPath("rsl"));
		// and add test libraries
		configuration.addLibraryPath(getDependenciesPath("test"));

		configuration.addSourcePath(new File[] { new File(build
				.getTestOutputDirectory()) });
		configuration.addSourcePath(MavenUtils.getTestSourcePaths(build));
		configuration.allowSourcePathOverlap(true);

	}

}
