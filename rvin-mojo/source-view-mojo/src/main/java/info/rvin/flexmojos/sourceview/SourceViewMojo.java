package info.rvin.flexmojos.sourceview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.w3c.dom.html.HTMLCollection;

import de.java2html.Java2Html;
import de.java2html.util.HtmlUtilities;

/**
 * @goal source-view
 * @phase package
 */
public class SourceViewMojo extends AbstractMojo {

	/**
	 * The maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	@SuppressWarnings("unchecked")
	public void execute() throws MojoExecutionException, MojoFailureException {
		File srcDir = new File(project.getBuild().getSourceDirectory());
		Collection<File> testFiles = FileUtils.listFiles(srcDir, new String[]{"as"}, true);
		for (File file : testFiles) {
			String fileContent;
			try {
				fileContent = IOUtils.toString(new FileReader(file));
			} catch (IOException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
			System.out.println(fileContent);
			fileContent = Java2Html.convertToHtmlPage(fileContent);
			System.out.println(fileContent);
			System.exit(0);
		}
	}

}
