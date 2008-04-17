package info.rvin.flexmojos.asdoc;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.doxia.siterenderer.RendererException;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
import org.codehaus.plexus.util.StringUtils;

/**
 * Generates documentation for the <code>ActionScript code</code> in the
 * project using the standard Asdoc tool
 * 
 * @author <a href="mailto:justin.edelson@mtvstaff.com">Justin Edelson</a>
 * @goal asdoc-report
 * 
 */
public class AsDocReport extends AsDocMojo implements MavenReport {

	/**
	 * Generates the site report
	 * 
	 * @component
	 */
	private Renderer siteRenderer;

	/**
	 * The name of the destination directory.
	 * 
	 * @parameter expression="${destDir}" default-value="asdocs"
	 */
	private String destDir;

	/**
	 * The name of the AsDoc report.
	 * 
	 * @parameter expression="${name}"
	 */
	private String name;

	/**
	 * The description of the AsDoc report.
	 * 
	 * @parameter expression="${description}"
	 */
	private String description;

	/**
	 * Specifies the destination directory where javadoc saves the generated
	 * HTML files.
	 * 
	 * @parameter expression="${project.reporting.outputDirectory}/asdocs"
	 * @required
	 */
	protected File reportOutputDirectory;

	public boolean canGenerateReport() {
		return true;
	}

	/**
	 * @see org.apache.maven.reporting.AbstractMavenReport#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			SiteRendererSink sink = siteRenderer.createSink(
					getReportOutputDirectory(), getOutputName() + ".html");

			generate(sink, Locale.getDefault());
		} catch (RendererException e) {
			throw new MojoExecutionException("An error has occurred in "
					+ getName(Locale.ENGLISH) + " report generation:"
					+ e.getMessage(), e);
		} catch (IOException e) {
			throw new MojoExecutionException("An error has occurred in "
					+ getName(Locale.ENGLISH) + " report generation:"
					+ e.getMessage(), e);
		} catch (MavenReportException e) {
			throw new MojoExecutionException("An error has occurred in "
					+ getName(Locale.ENGLISH) + " report generation:"
					+ e.getMessage(), e);
		}
	}

	public void generate(Sink sink, Locale locale) throws MavenReportException {
		output = getReportOutputDirectory();
		try {
			// Not really sure why this class loader switching
			// is necessary. But it is.
			Thread currentThread = Thread.currentThread();
			ClassLoader savedCL = currentThread.getContextClassLoader();
			currentThread.setContextClassLoader(getClass().getClassLoader());
			super.execute();
			currentThread.setContextClassLoader(savedCL);
		} catch (MojoExecutionException e) {
			throw new MavenReportException("Unable to generate report", e);
		} catch (MojoFailureException e) {
			throw new MavenReportException("Unable to generate report", e);
		}

	}

	/**
	 * Gets the resource bundle for the specified locale.
	 * 
	 * @param locale
	 *            The locale of the currently generated report.
	 * @return The resource bundle for the requested locale.
	 */
	private ResourceBundle getBundle(Locale locale) {
		return ResourceBundle.getBundle("asdoc-report", locale, getClass()
				.getClassLoader());
	}

	public String getCategoryName() {
		return CATEGORY_PROJECT_REPORTS;
	}

	public String getDescription(Locale locale) {
		if (StringUtils.isEmpty(description)) {
			return getBundle(locale).getString("report.asdoc.description");
		}

		return description;
	}

	public String getName(Locale locale) {
		if (StringUtils.isEmpty(name)) {
			return getBundle(locale).getString("report.asdoc.name");
		}

		return name;
	}

	public String getOutputName() {
		return destDir + "/index";
	}

	public File getReportOutputDirectory() {
		if (reportOutputDirectory == null) {
			return output;
		}

		return reportOutputDirectory;
	}

	public boolean isExternalReport() {
		return true;
	}

	public void setReportOutputDirectory(File outputDirectory) {
		if ((outputDirectory != null)
				&& (!outputDirectory.getAbsolutePath().endsWith(destDir))) {
			this.reportOutputDirectory = new File(outputDirectory, destDir);
		} else {
			this.reportOutputDirectory = outputDirectory;
		}

	}

}
