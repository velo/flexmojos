package org.sonatype.flexmojos.plugin.report;

import java.io.File;
import java.util.Locale;

import org.sonatype.flexmojos.plugin.compiler.AsdocMojo;
import org.sonatype.flexmojos.util.PathUtil;

/**
 * <p>
 * Goal which generates documentation from the ActionScript sources.
 * </p>
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 1.0
 * @goal asdoc-report
 * @requiresDependencyResolution compile
 * @phase process-sources
 * @configurator flexmojos
 * @threadSafe
 */
public class AsdocReportMojo
    extends AsdocMojo
{

    /**
     * Specifies the destination directory where asdoc saves the generated HTML files.
     * 
     * @parameter expression="${project.reporting.outputDirectory}/asdoc"
     * @readonly
     * @required
     */
    protected File asdocOutputDirectory;

    /**
     * The description of the AsDoc report.
     * 
     * @parameter expression="${flex.description}" default-value="ASDoc API documentation."
     */
    private String description;

    /**
     * The name of the AsDoc report.
     * 
     * @parameter expression="${flex.name}" default-value="ASDocs"
     */
    private String name;

    public String getDescription( Locale locale )
    {
        return description;
    }

    public String getName( Locale locale )
    {
        return name;
    }

    @Override
    public String getOutput()
    {
        return PathUtil.path( getReportOutputDirectory() );
    }

    public String getOutputName()
    {
        return "asdoc/index";
    }

    public File getReportOutputDirectory()
    {
        return asdocOutputDirectory;
    }

}
