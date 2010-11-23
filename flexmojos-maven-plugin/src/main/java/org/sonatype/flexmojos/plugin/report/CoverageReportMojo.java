package org.sonatype.flexmojos.plugin.report;

import static org.sonatype.flexmojos.util.PathUtil.files;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.flexmojos.plugin.AbstractMavenMojo;
import org.sonatype.flexmojos.plugin.SourcePathAware;

/**
 * Goal to generate coverage report from unit tests
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 4.0
 * @goal coverage-report
 * @execute lifecycle="coveragecycle" phase="test"
 * @requiresDependencyResolution test
 * @threadSafe
 */
public class CoverageReportMojo
    extends AbstractMavenMojo
    implements SourcePathAware
{

    /**
     * Specifies the destination directory where asdoc saves the generated HTML files.
     * 
     * @parameter expression="${project.reporting.outputDirectory}/coverage"
     * @readonly
     * @required
     */
    protected File coverageReportOutputDirectory;

    /**
     * The description of the AsDoc report.
     * 
     * @parameter expression="${flex.description}" default-value="Flexmojos Test Coverage Report."
     */
    private String description;

    /**
     * The name of the AsDoc report.
     * 
     * @parameter expression="${flex.name}" default-value="Coverage"
     */
    private String name;

    /**
     * The maven compile source roots. List of path elements that form the roots of ActionScript class
     * 
     * @parameter expression="${project.compileSourceRoots}"
     * @required
     * @readonly
     */
    private List<String> sourcePaths;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // nothing to be done, the lifecycle deal with this report generation
    }

    public String getDescription( Locale locale )
    {
        return description;
    }

    public String getName( Locale locale )
    {
        return name;
    }

    public String getOutputName()
    {
        return "coverage/index";
    }

    public File getReportOutputDirectory()
    {
        coverageReportOutputDirectory.mkdirs();
        return coverageReportOutputDirectory;
    }

    public File[] getSourcePath()
    {
        return files( sourcePaths );
    }

}
