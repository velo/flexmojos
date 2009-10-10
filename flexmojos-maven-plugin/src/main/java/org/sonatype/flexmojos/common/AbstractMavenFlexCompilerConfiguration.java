/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sonatype.flexmojos.common;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.maven.model.Contributor;
import org.apache.maven.model.Developer;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.sonatype.flexmojos.compiler.FrameLabel;
import org.sonatype.flexmojos.compiler.ICommandLineConfiguration;
import org.sonatype.flexmojos.compiler.ICompcConfiguration;
import org.sonatype.flexmojos.compiler.ICompilerConfiguration;
import org.sonatype.flexmojos.compiler.IDefaultScriptLimits;
import org.sonatype.flexmojos.compiler.IDefaultSize;
import org.sonatype.flexmojos.compiler.IDefine;
import org.sonatype.flexmojos.compiler.IFontsConfiguration;
import org.sonatype.flexmojos.compiler.IFramesConfiguration;
import org.sonatype.flexmojos.compiler.ILicense;
import org.sonatype.flexmojos.compiler.ILicensesConfiguration;
import org.sonatype.flexmojos.compiler.ILocalizedDescription;
import org.sonatype.flexmojos.compiler.ILocalizedTitle;
import org.sonatype.flexmojos.compiler.IMetadataConfiguration;
import org.sonatype.flexmojos.compiler.IMxmlConfiguration;
import org.sonatype.flexmojos.compiler.INamespacesConfiguration;
import org.sonatype.flexmojos.test.util.PathUtil;

public class AbstractMavenFlexCompilerConfiguration
    implements ICompcConfiguration, ICommandLineConfiguration, ICompilerConfiguration, IFramesConfiguration,
    ILicensesConfiguration, IMetadataConfiguration
{

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat();

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The maven resources
     * 
     * @parameter expression="${project.build.resources}"
     * @required
     * @readonly
     */
    protected List<Resource> resources;

    /**
     * The maven configuration directory
     * 
     * @parameter expression="${basedir}/src/main/config"
     * @required
     * @readonly
     */
    protected File configDirectory;

    /**
     * Output performance benchmark
     * <p>
     * Equivalent to -benchmark
     * </p>
     * 
     * @parameter expression="${flex.benchmark}"
     */
    protected Boolean benchmark;

    /**
     * Writes a digest to the catalog.xml of a library. This is required when the library will be used as runtime shared
     * libraries
     * <p>
     * Equivalent to -compute-digest
     * </p>
     * 
     * @parameter expression="${flex.computeDigest}"
     */
    protected Boolean computeDigest;

    /**
     * The password to include in debuggable SWFs
     * <p>
     * Equivalent to -debug-password
     * </p>
     * 
     * @parameter expression="${flex.debugPassword}"
     */
    protected String debugPassword;

    /**
     * Default background color (may be overridden by the application code)
     * <p>
     * Equivalent to -default-background-color
     * </p>
     * 
     * @parameter expression="${flex.defaultBackgroundColor}"
     */
    private Integer defaultBackgroundColor;

    /**
     * Default frame rate to be used in the SWF
     * <p>
     * Equivalent to -default-frame-rate
     * </p>
     * 
     * @parameter expression="${flex.defaultFrameRate}"
     */
    private Integer defaultFrameRate;

    /**
     * Default script execution limits (may be overridden by root attributes)
     * <p>
     * Equivalent to -default-script-limits
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;defaultScriptLimits&gt;
     *   &lt;maxExecutionTime&gt;???&lt;/maxExecutionTime&gt;
     *   &lt;maxRecursionDepth&gt;???&lt;/maxRecursionDepth&gt;
     * &lt;/defaultScriptLimits&gt;
     * </pre>
     * 
     * @parameter
     */
    private MavenDefaultScriptLimits defaultScriptLimits;

    /**
     * Default application size (may be overridden by root attributes in the application)
     * <p>
     * Equivalent to -default-size
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;defaultSize&gt;
     *   &lt;height&gt;???&lt;/height&gt;
     *   &lt;width&gt;???&lt;/width&gt;
     * &lt;/defaultSize&gt;
     * </pre>
     * 
     * @parameter
     */
    private MavenDefaultSize defaultSize;

    /**
     * Output the library as an open directory instead of a SWC file
     * <p>
     * Equivalent to -directory
     * </p>
     * 
     * @parameter expression="${flex.directory}"
     */
    private Boolean directory;

    /**
     * Write a file containing all currently set configuration values in a format suitable for use as a flex config file
     * <p>
     * Equivalent to -dump-config
     * </p>
     * 
     * @parameter expression="${flex.dumpConfig}"
     */
    private File dumpConfig;

    /**
     * A list of symbols to omit from linking when building a SWF
     * <p>
     * Equivalent to -externs
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;externs&gt;
     *   &lt;extern&gt;???&lt;/extern&gt;
     *   &lt;extern&gt;???&lt;/extern&gt;
     * &lt;/externs&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] externs;

    /**
     * A SWF frame label with a sequence of classnames that will be linked onto the frame
     * <p>
     * Equivalent to -frames.frame
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;frames&gt;
     *   &lt;frame&gt;
     *     &lt;label&gt;???&lt;/label&gt;
     *     &lt;classNames&gt;
     *       &lt;className&gt;???&lt;/className&gt;
     *       &lt;className&gt;???&lt;/className&gt;
     *     &lt;/classNames&gt;
     *   &lt;/frame&gt;
     * &lt;/frames&gt;
     * </pre>
     * 
     * @parameter
     */
    private List<FrameLabel> frames;

    /**
     * FIXME Undocumented by adobe
     * <p>
     * Equivalent to -generated-frame-loader
     * </p>
     * 
     * @parameter expression="${flex.generateFrameLoader}"
     */
    private Boolean generateFrameLoader;

    /**
     * If true, manifest entries with lookupOnly=true are included in SWC catalog
     * <p>
     * Equivalent to -include-lookup-only
     * </p>
     * 
     * @parameter expression="${flex.includeLookupOnly}"
     */
    private Boolean includeLookupOnly;

    /**
     * A list of symbols to always link in when building a SWF
     * <p>
     * Equivalent to -includes
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includes&gt;
     *   &lt;include&gt;???&lt;/include&gt;
     *   &lt;include&gt;???&lt;/include&gt;
     * &lt;/includes&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] includes;

    /**
     * FIXME Undocumented by adobe
     * <p>
     * Equivalent to -lazy-init
     * </p>
     * 
     * @parameter expression="${flex.lazyInit}"
     */
    private Boolean lazyInit;

    /**
     * Specifies a product and a serial number
     * <p>
     * Equivalent to -licenses.license
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;licenses&gt;
     *   &lt;flexbuilder3&gt;xxxx-xxxx-xxxx-xxxx&lt;/flexbuilder3&gt;
     * &lt;/licenses&gt;
     * </pre>
     * 
     * @parameter
     */
    private Map<String, String> licenses;

    /**
     * Output a XML-formatted report of all definitions linked into the application
     * <p>
     * Equivalent to -link-report
     * </p>
     * 
     * @parameter expression="${flex.linkReport}"
     */
    private File linkReport;

    /**
     * Load a file containing configuration options.
     * <p>
     * Equivalent to -load-config
     * </p>
     * Overwrite loadConfigs when defined!
     * 
     * @parameter expression="${flex.loadConfig}"
     */
    private File loadConfig;

    /**
     * Load a file containing configuration options
     * <p>
     * Equivalent to -load-config
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;loadConfigs&gt;
     *   &lt;loadConfig&gt;???&lt;/loadConfig&gt;
     *   &lt;loadConfig&gt;???&lt;/loadConfig&gt;
     * &lt;/loadConfigs&gt;
     * </pre>
     * 
     * @parameter
     */
    private File[] loadConfigs;

    /**
     * An XML file containing &lt;def&gt;, &lt;pre&gt;, and &lt;ext&gt; symbols to omit from linking when building a SWF
     * <p>
     * Equivalent to -load-externs
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;loadExterns&gt;
     *   &lt;loadExtern&gt;???&lt;/loadExtern&gt;
     *   &lt;loadExtern&gt;???&lt;/loadExtern&gt;
     * &lt;/loadExterns&gt;
     * </pre>
     * 
     * @parameter
     */
    private File[] loadExterns;

    // FIXME
    private IMetadataConfiguration metadataConfiguration;

    /**
     * The filename of the SWF movie to create
     * <p>
     * Equivalent to -output
     * </p>
     * 
     * @parameter expression="${flex.output}"
     */
    private String output;

    /**
     * XML text to store in the SWF metadata (overrides metadata.* configuration)
     * <p>
     * Equivalent to -raw-metadata
     * </p>
     * 
     * @parameter expression="${flex.rawMetadata}"
     */
    private String rawMetadata;

    /**
     * FIXME Guess what, undocumented by adobe. Looks like it was overwritten by source paths
     * <p>
     * Equivalent to -root
     * </p>
     * 
     * @parameter expression="${flex.root}"
     * @deprecated
     */
    private String root;

    /**
     * A list of runtime shared library URLs to be loaded before the application starts
     * <p>
     * Equivalent to -runtime-shared-libraries
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;runtimeSharedLibraries&gt;
     *   &lt;runtimeSharedLibrary&gt;???&lt;/runtimeSharedLibrary&gt;
     *   &lt;runtimeSharedLibrary&gt;???&lt;/runtimeSharedLibrary&gt;
     * &lt;/runtimeSharedLibraries&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] runtimeSharedLibraries;

    /**
     * Statically link the libraries specified by the -runtime-shared-libraries-path option.
     * <p>
     * Equivalent to -static-link-runtime-shared-libraries
     * </p>
     * 
     * @parameter expression="${flex.staticLinkRuntimeSharedLibraries}"
     */
    private Boolean staticLinkRuntimeSharedLibraries;

    /**
     * If true optimization using signature checksums are enabled
     * <p>
     * Equivalent to -swc-checksum
     * </p>
     * 
     * @parameter expression="${flex.swcChecksum}"
     */
    private Boolean swcChecksum;

    /**
     * Toggle whether the SWF is flagged for access to network resources
     * <p>
     * Equivalent to -use-network
     * </p>
     * 
     * @parameter expression="${flex.userNetwork}"
     */
    private Boolean userNetwork;

    /**
     * Verifies the libraries loaded at runtime are the correct ones
     * <p>
     * Equivalent to -verify-digests
     * </p>
     * 
     * @parameter expression="${flex.verifyDigests}"
     */
    private Boolean verifyDigests;

    /**
     * Toggle the display of warnings
     * <p>
     * Equivalent to -warnings
     * </p>
     * 
     * @parameter expression="${flex.warnings}"
     */
    private Boolean warnings;

    /**
     * FIXME Again, undocumented by adobe
     * <p>
     * Equivalent to -file-specs
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;fileSpecs&gt;
     *   &lt;fileSpec&gt;???&lt;/fileSpec&gt;
     *   &lt;fileSpec&gt;???&lt;/fileSpec&gt;
     * &lt;/fileSpecs&gt;
     * </pre>
     * 
     * @parameter
     */
    private List<String> fileSpecs;

    /**
     * FIXME Another, undocumented by adobe
     * <p>
     * Equivalent to -projector
     * </p>
     * 
     * @parameter expression="${flex.projector}"
     */
    private String projector;

    /**
     * Generate an accessible SWF
     * <p>
     * Equivalent to -compiler.accessible
     * </p>
     * 
     * @parameter expression="${flex.accessible}"
     */
    private Boolean accessible;

    /**
     * Specifies actionscript file encoding. If there is no BOM in the AS3 source files, the compiler will use this file
     * encoding.
     * <p>
     * Equivalent to -compiler.actionscript-file-encoding
     * </p>
     * 
     * @parameter expression="${flex.actionscriptFileEncoding}"
     */
    private String actionscriptFileEncoding;

    /**
     * FIXME undocumented by adobe
     * <p>
     * Equivalent to -compiler.adjust-opdebugline
     * </p>
     * 
     * @parameter expression="${flex.adjustOpdebugline}"
     */
    private Boolean adjustOpdebugline;

    /**
     * checks if a source-path entry is a subdirectory of another source-path entry. It helps make the package names of
     * MXML components unambiguous.
     * <p>
     * Equivalent to -compiler.allow-source-path-overlap
     * </p>
     * 
     * @parameter expression="${flex.allowSourcePathOverlap}"
     */
    private Boolean allowSourcePathOverlap;

    /**
     * FIXME undocumented by adobe
     * <p>
     * Equivalent to -compiler.archive-classes-and-assets
     * </p>
     * 
     * @parameter expression="${flex.archiveClassesAndAssets}"
     */
    private Boolean archiveClassesAndAssets;

    /**
     * Use the ActionScript 3 class based object model for greater performance and better error reporting. In the class
     * based object model most built-in functions are implemented as fixed methods of classes
     * <p>
     * Equivalent to -compiler.as3
     * </p>
     * 
     * @parameter expression="${flex.as3}"
     */
    private Boolean as3;

    /**
     * FIXME undocumented by adobe
     * <p>
     * Equivalent to -compiler.conservative
     * </p>
     * compiler algorithm settings
     * 
     * @parameter expression="${flex.conservative}"
     */
    private Boolean conservative;

    /**
     * Path to replace {context.root} tokens for service channel endpoints
     * <p>
     * Equivalent to -compiler.context-root
     * </p>
     * 
     * @parameter expression="${flex.contextRoot}"
     */
    private String contextRoot;

    /**
     * Generates a movie that is suitable for debugging
     * <p>
     * Equivalent to -compiler.debug
     * </p>
     * 
     * @parameter expression="${flex.debug}"
     */
    private Boolean debug;

    public Boolean getBenchmark()
    {
        return benchmark;
    }

    public ICompilerConfiguration getCompilerConfiguration()
    {
        return this;
    }

    public Boolean getComputeDigest()
    {
        return computeDigest;
    }

    public String getDebugPassword()
    {
        return debugPassword;
    }

    public Integer getDefaultBackgroundColor()
    {
        return defaultBackgroundColor;
    }

    public Integer getDefaultFrameRate()
    {
        return defaultFrameRate;
    }

    public IDefaultScriptLimits getDefaultScriptLimits()
    {
        return defaultScriptLimits;
    }

    public IDefaultSize getDefaultSize()
    {
        return defaultSize;
    }

    public Boolean getDirectory()
    {
        return directory;
    }

    public String getDumpConfig()
    {
        return PathUtil.getCanonicalPath( dumpConfig );
    }

    public List<String> getExterns()
    {
        if ( externs == null )
        {
            return null;
        }

        return Arrays.asList( externs );
    }

    public IFramesConfiguration getFramesConfiguration()
    {
        return this;
    }

    public List<FrameLabel> getFrame()
    {
        return frames;
    }

    public Boolean getGenerateFrameLoader()
    {
        return generateFrameLoader;
    }

    public final String[] getHelp()
    {
        // must return null, otherwise will prevent compiler execution
        return null;
    }

    public List<String> getIncludeClasses()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public File[] getIncludeFile()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getIncludeLookupOnly()
    {
        return includeLookupOnly;
    }

    public List getIncludeNamespaces()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public List getIncludeResourceBundles()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public File[] getIncludeSources()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public File[] getIncludeStylesheet()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public List<String> getIncludes()
    {
        if ( includes == null )
        {
            return null;
        }
        return Arrays.asList( includes );
    }

    public Boolean getLazyInit()
    {
        return lazyInit;
    }

    public ILicensesConfiguration getLicensesConfiguration()
    {
        return this;
    }

    public ILicense[] getLicense()
    {
        if ( licenses == null )
        {
            return null;
        }

        List<ILicense> keys = new ArrayList<ILicense>();
        Set<Entry<String, String>> entries = this.licenses.entrySet();
        for ( final Entry<String, String> entry : entries )
        {
            keys.add( new ILicense()
            {
                public String serialNumber()
                {
                    return entry.getValue();
                }

                public String product()
                {
                    return entry.getKey();
                }
            } );
        }

        return keys.toArray( new ILicense[keys.size()] );
    }

    public String getLinkReport()
    {
        return PathUtil.getCanonicalPath( linkReport );
    }

    public String[] getLoadConfig()
    {
        if ( loadConfig == null && loadConfigs != null )
        {
            return filesToStrings( loadConfigs );
        }

        File configFile;
        if ( loadConfig != null )
        {
            configFile = this.loadConfig;
        }
        else
        {
            File cfg = new File( configDirectory, "config.xml" );
            File flexCfg = new File( configDirectory, "flex-config.xml" );
            File airCfg = new File( configDirectory, "air-config.xml" );
            if ( cfg.exists() )
            {
                configFile = cfg;
            }
            else if ( flexCfg.exists() )
            {
                configFile = flexCfg;
            }
            else if ( airCfg.exists() )
            {
                configFile = airCfg;
            }
            else
            {
                return new String[0];
            }
        }
        return new String[] { PathUtil.getCanonicalPath( configFile ) };
    }

    public String[] getLoadExterns()
    {
        return filesToStrings( loadExterns );
    }

    public IMetadataConfiguration getMetadataConfiguration()
    {
        return this;
    }

    @SuppressWarnings( "unchecked" )
    public String[] getContributor()
    {
        if ( this.metadataConfiguration != null )
        {
            return this.metadataConfiguration.getContributor();
        }

        List<Contributor> contributors = project.getContributors();
        if ( contributors == null || contributors.isEmpty() )
        {
            return null;
        }

        String[] contributorsName = new String[contributors.size()];
        for ( int i = 0; i < contributorsName.length; i++ )
        {
            contributorsName[i] = contributors.get( i ).getName();
        }

        return contributorsName;
    }

    @SuppressWarnings( "unchecked" )
    public String[] getCreator()
    {
        if ( this.metadataConfiguration != null )
        {
            return this.metadataConfiguration.getCreator();
        }

        List<Developer> developers = project.getDevelopers();
        if ( developers == null || developers.isEmpty() )
        {
            return null;
        }

        String[] creatorsName = new String[developers.size()];
        for ( int i = 0; i < creatorsName.length; i++ )
        {
            creatorsName[i] = developers.get( i ).getName();
        }

        return creatorsName;
    }

    public String getDate()
    {
        if ( this.metadataConfiguration != null && this.metadataConfiguration.getDate() != null )
        {
            return this.metadataConfiguration.getDate();
        }

        return DATE_FORMAT.format( new Date() );
    }

    public String getDescription()
    {
        if ( this.metadataConfiguration != null )
        {
            return this.metadataConfiguration.getDescription();
        }

        return project.getDescription();
    }

    public String[] getLanguage()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ILocalizedDescription[] getLocalizedDescription()
    {
        if ( this.metadataConfiguration != null )
        {
            return this.metadataConfiguration.getLocalizedDescription();
        }

        return null;
    }

    public ILocalizedTitle[] getLocalizedTitle()
    {
        if ( this.metadataConfiguration != null )
        {
            return this.metadataConfiguration.getLocalizedTitle();
        }

        return null;
    }

    public String[] getPublisher()
    {
        if ( this.metadataConfiguration != null )
        {
            return this.metadataConfiguration.getPublisher();
        }

        return getCreator();
    }

    public String getTitle()
    {
        if ( this.metadataConfiguration != null )
        {
            return this.metadataConfiguration.getDescription();
        }

        return project.getName();
    }

    public String getOutput()
    {
        return output;
    }

    public String getRawMetadata()
    {
        return rawMetadata;
    }

    public String getResourceBundleList()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRoot()
    {
        return root;
    }

    public String[] getRuntimeSharedLibraries()
    {
        return runtimeSharedLibraries;
    }

    public String[] getRuntimeSharedLibraryPath()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getStaticLinkRuntimeSharedLibraries()
    {
        return staticLinkRuntimeSharedLibraries;
    }

    public Boolean getSwcChecksum()
    {
        return swcChecksum;
    }

    public String getTargetPlayer()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getUseNetwork()
    {
        return userNetwork;
    }

    public Boolean getVerifyDigests()
    {
        return verifyDigests;
    }

    public final Boolean getVersion()
    {
        // must return null, otherwise will prevent compiler execution
        return null;
    }

    public Boolean getWarnings()
    {
        return warnings;
    }

    public List<String> getFileSpecs()
    {
        return fileSpecs;
    }

    public String getProjector()
    {
        return projector;
    }

    public Boolean getAccessible()
    {
        return accessible;
    }

    public String getActionscriptFileEncoding()
    {
        return actionscriptFileEncoding;
    }

    public Boolean getAdjustOpdebugline()
    {
        return adjustOpdebugline;
    }

    public Boolean getAllowSourcePathOverlap()
    {
        return allowSourcePathOverlap;
    }

    public Boolean getArchiveClassesAndAssets()
    {
        return archiveClassesAndAssets;
    }

    public Boolean getAs3()
    {
        return as3;
    }

    public Boolean getConservative()
    {
        return conservative;
    }

    public String getContextRoot()
    {
        return contextRoot;
    }

    public Boolean getDebug()
    {
        return debug;
    }

    public List getDefaultsCssFiles()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getDefaultsCssUrl()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public IDefine[] getDefine()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getDisableIncrementalOptimizations()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getDoc()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getEs()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public File[] getExternalLibraryPath()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public IFontsConfiguration getFontsConfiguration()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getHeadlessServer()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public File[] getIncludeLibraries()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getIncremental()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getKeepAllTypeSelectors()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] getKeepAs3Metadata()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getKeepGeneratedActionscript()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getKeepGeneratedSignatures()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public File[] getLibraryPath()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] getLocale()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Integer getMemoryUsageFactor()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public IMxmlConfiguration getMxmlConfiguration()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public INamespacesConfiguration getNamespacesConfiguration()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getOptimize()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getResourceHack()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getServices()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getShowActionscriptWarnings()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getShowBindingWarnings()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getShowDependencyWarnings()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getShowDeprecationWarnings()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getShowShadowedDeviceFontWarnings()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getShowUnusedTypeSelectorWarnings()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public File getSignatureDirectory()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public File[] getSourcePath()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getStrict()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public List getTheme()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getTranslationFormat()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getUseResourceBundleMetadata()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getVerboseStacktraces()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnArrayTostringChanges()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnAssignmentWithinConditional()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnBadArrayCast()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnBadBoolAssignment()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnBadDateCast()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnBadEs3TypeMethod()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnBadEs3TypeProp()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnBadNanComparison()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnBadNullAssignment()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnBadNullComparison()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnBadUndefinedComparison()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnBooleanConstructorWithNoArgs()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnChangesInResolve()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnClassIsSealed()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnConstNotInitialized()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnConstructorReturnsValue()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnDeprecatedEventHandlerError()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnDeprecatedFunctionError()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnDeprecatedPropertyError()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnDuplicateArgumentNames()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnDuplicateVariableDef()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnForVarInChanges()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnImportHidesClass()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnInstanceOfChanges()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnInternalError()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnLevelNotSupported()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnMissingNamespaceDecl()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnNegativeUintLiteral()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnNoConstructor()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnNoExplicitSuperCallInConstructor()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnNoTypeDecl()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnNumberFromStringChanges()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnScopingChangeInThis()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnSlowTextFieldAddition()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnUnlikelyFunctionValue()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getWarnXmlClassHasChanged()
    {
        // TODO Auto-generated method stub
        return null;
    }

    private String[] filesToStrings( File[] files )
    {
        // TODO move out here
        if ( files == null )
        {
            return null;
        }

        String[] configs = new String[files.length];
        for ( int i = 0; i < configs.length; i++ )
        {
            configs[i] = PathUtil.getCanonicalPath( files[i] );
        }
        return configs;
    }

}
