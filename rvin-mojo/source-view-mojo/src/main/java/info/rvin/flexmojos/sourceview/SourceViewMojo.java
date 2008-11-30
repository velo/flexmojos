/**
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.rvin.flexmojos.sourceview;

import info.rvin.flexmojos.utilities.MavenUtils;

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
        getLog().info(
                      "Flex-mojos " + MavenUtils.getFlexMojosVersion( )
                          + " - GNU GPL License (NO WARRANTY) - See COPYRIGHT file" );

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
