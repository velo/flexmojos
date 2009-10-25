package org.sonatype.flexmojos.common;

import static org.sonatype.flexmojos.common.FlexScopes.COMPILE;
import static org.sonatype.flexmojos.common.FlexScopes.EXTERNAL;
import static org.sonatype.flexmojos.common.FlexScopes.INTERNAL;
import static org.sonatype.flexmojos.common.FlexScopes.MERGED;
import static org.sonatype.flexmojos.common.FlexScopes.THEME;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Developer;
import org.apache.maven.model.PatternSet;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.apache.maven.shared.artifact.filter.collection.ClassifierFilter;
import org.apache.maven.shared.artifact.filter.collection.FilterArtifacts;
import org.apache.maven.shared.artifact.filter.collection.ScopeFilter;
import org.apache.maven.shared.artifact.filter.collection.TypeFilter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.compiler.FrameLabel;
import org.sonatype.flexmojos.compiler.ICommandLineConfiguration;
import org.sonatype.flexmojos.compiler.ICompcConfiguration;
import org.sonatype.flexmojos.compiler.ICompilerConfiguration;
import org.sonatype.flexmojos.compiler.IDefaultScriptLimits;
import org.sonatype.flexmojos.compiler.IDefaultSize;
import org.sonatype.flexmojos.compiler.IDefine;
import org.sonatype.flexmojos.compiler.IFontsConfiguration;
import org.sonatype.flexmojos.compiler.IFramesConfiguration;
import org.sonatype.flexmojos.compiler.ILanguageRange;
import org.sonatype.flexmojos.compiler.ILanguages;
import org.sonatype.flexmojos.compiler.ILicense;
import org.sonatype.flexmojos.compiler.ILicensesConfiguration;
import org.sonatype.flexmojos.compiler.ILocalizedDescription;
import org.sonatype.flexmojos.compiler.ILocalizedTitle;
import org.sonatype.flexmojos.compiler.IMetadataConfiguration;
import org.sonatype.flexmojos.compiler.IMxmlConfiguration;
import org.sonatype.flexmojos.compiler.INamespace;
import org.sonatype.flexmojos.compiler.INamespacesConfiguration;
import org.sonatype.flexmojos.test.util.PathUtil;
import org.sonatype.flexmojos.utilities.MavenUtils;

public class AbstractMavenFlexCompilerConfiguration
    implements ICompcConfiguration, ICommandLineConfiguration, ICompilerConfiguration, IFramesConfiguration,
    ILicensesConfiguration, IMetadataConfiguration, IFontsConfiguration, ILanguages, IMxmlConfiguration,
    INamespacesConfiguration
{

    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat();

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
     * The maven compile source roots
     * <p>
     * Equivalent to -compiler.source-path
     * </p>
     * List of path elements that form the roots of ActionScript class
     * 
     * @parameter expression="${project.compileSourceRoots}"
     * @required
     * @readonly
     */
    private List<String> sourcePaths;

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
     * DOCME Undocumented by adobe
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
     * DOCME Undocumented by adobe
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

    /**
     * Information to store in the SWF metadata
     * <p>
     * Equivalent to: -metadata.contributor, -metadata.creator, -metadata.date, -metadata.description,
     * -metadata.language, -metadata.localized-description, -metadata.localized-title, -metadata.publisher,
     * -metadata.title
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;metadata&gt;
     *   &lt;contributors&gt;
     *     &lt;contributor&gt;???&lt;/contributor&gt;
     *   &lt;/contributors&gt;
     *   &lt;creators&gt;
     *     &lt;creator&gt;???&lt;/creator&gt;
     *   &lt;/creators&gt;
     *   &lt;date&gt;???&lt;/date&gt;
     *   &lt;description&gt;???&lt;/description&gt;
     *   &lt;languages&gt;
     *     &lt;language&gt;???&lt;/language&gt;
     *   &lt;/languages&gt;
     *   &lt;localizedDescriptions&gt;
     *     &lt;lang&gt;text&lt;/land&gt;
     *   &lt;/localizedDescriptions&gt;
     *   &lt;localizedTitles&gt;
     *     &lt;lang&gt;title&lt;/land&gt;
     *   &lt;/localizedTitles&gt;
     *   &lt;publishers&gt;
     *     &lt;publisher&gt;???&lt;/publisher&gt;
     *   &lt;/publishers&gt;
     *   &lt;title&gt;???&lt;/title&gt;
     * &lt;/metadata&gt;
     * </pre>
     * 
     * @parameter
     */
    private MavenMetadataConfiguration metadata;

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
     * DOCME Guess what, undocumented by adobe. Looks like it was overwritten by source paths
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
     * Specifies the version of the player the application is targeting. Features requiring a later version will not be
     * compiled into the application. The minimum value supported is "9.0.0".
     * <p>
     * Equivalent to -target-player
     * </p>
     * 
     * @parameter expression="${flex.targetPlayer}"
     */
    private String targetPlayer;

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
     * DOCME Again, undocumented by adobe
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
     * DOCME Another, undocumented by adobe
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
     * DOCME undocumented by adobe
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
     * DOCME undocumented by adobe
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
     * DOCME undocumented by adobe
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

    /**
     * Defines the location of the default style sheet. Setting this option overrides the implicit use of the
     * defaults.css style sheet in the framework.swc file
     * <p>
     * Equivalent to -compiler.defaults-css-url
     * </p>
     * 
     * @parameter expression="${flex.defaultsCssUrl}"
     */
    private String defaultsCssUrl;

    /**
     * Location of defaults style stylesheets
     * <p>
     * Equivalent to -compiler.defaults-css-url
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;defaultsCssFiles&gt;
     *   &lt;defaultsCssFile&gt;???&lt;/defaultsCssFile&gt;
     *   &lt;defaultsCssFile&gt;???&lt;/defaultsCssFile&gt;
     * &lt;/defaultsCssFiles&gt;
     * </pre>
     * 
     * @parameter
     */
    private File[] defaultsCssFiles;

    /**
     * Define a global AS3 conditional compilation definition, e.g. -define=CONFIG::debugging,true or
     * -define+=CONFIG::debugging,true (to append to existing definitions in flex-config.xml)
     * <p>
     * Equivalent to -compiler.define
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;definesDeclaration&gt;
     *   &lt;property&gt;
     *     &lt;name&gt;SOMETHING::aNumber&lt;/name&gt;
     *     &lt;value&gt;2.2&lt;/value&gt;
     *   &lt;/property&gt;
     *   &lt;property&gt;
     *     &lt;name&gt;SOMETHING::aString&lt;/name&gt;
     *     &lt;value&gt;&quot;text&quot;&lt;/value&gt;
     *   &lt;/property&gt;
     * &lt;/definesDeclaration&gt;
     * </pre>
     * 
     * @parameter
     */
    private Properties defines;

    /**
     * Back-door to disable optimizations in case they are causing problems
     * <p>
     * Equivalent to -compiler.disable-incremental-optimizations
     * </p>
     * 
     * @parameter expression="${flex.disableIncrementalOptimizations}"
     */
    private Boolean disableIncrementalOptimizations;

    /**
     * DOCME undocumented
     * <p>
     * Equivalent to -compiler.doc
     * </p>
     * 
     * @parameter expression="${flex.doc}"
     */
    private Boolean doc;

    /**
     * Use the ECMAScript edition 3 prototype based object model to allow dynamic overriding of prototype properties. In
     * the prototype based object model built-in functions are implemented as dynamic properties of prototype objects
     * <p>
     * Equivalent to -compiler.es
     * </p>
     * 
     * @parameter expression="${flex.es}"
     */
    private Boolean es;

    /**
     * Enables advanced anti-aliasing for embedded fonts, which provides greater clarity for small fonts
     * <p>
     * Equivalent to -compiler.fonts.advanced-anti-aliasing
     * </p>
     * 
     * @parameter expression="${flex.advancedAntiAliasing}"
     */
    private Boolean advancedAntiAliasing;

    /**
     * Enables FlashType for embedded fonts, which provides greater clarity for small fonts
     * <p>
     * Equivalent to -compiler.fonts.flash-type
     * </p>
     * 
     * @parameter expression="${flex.flashType}"
     */
    private Boolean flashType;

    /**
     * A range to restrict the number of font glyphs embedded into the SWF
     * <p>
     * Equivalent to -compiler.fonts.languages.language-range
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;languageRange&gt;
     *   &lt;lang&gt;range&lt;/lang&gt;
     * &lt;/languageRange&gt;
     * </pre>
     * 
     * @parameter
     */
    private Map<String, String> languageRange;

    /**
     * Compiler font manager classes, in policy resolution order
     * <p>
     * Equivalent to -compiler.fonts.local-fonts-snapshot
     * </p>
     * 
     * @parameter expression="${flex.localFontsSnapshot}"
     */
    private File localFontsSnapshot;

    /**
     * Compiler font manager classes, in policy resolution order
     * <p>
     * Equivalent to -compiler.fonts.managers
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;managers&gt;
     *   &lt;manager&gt;???&lt;/manager&gt;
     *   &lt;manager&gt;???&lt;/manager&gt;
     * &lt;/managers&gt;
     * </pre>
     * 
     * @parameter
     */
    private List<String> managers;

    /**
     * Sets the maximum number of fonts to keep in the server cache
     * <p>
     * Equivalent to -compiler.fonts.max-cached-fonts
     * </p>
     * 
     * @parameter expression="${flex.maxCachedFonts}"
     */
    private Integer maxCachedFonts;

    /**
     * Sets the maximum number of character glyph-outlines to keep in the server cache for each font face
     * <p>
     * Equivalent to -compiler.fonts.max-glyphs-per-face
     * </p>
     * 
     * @parameter expression="${flex.maxGlyphsPerFace}"
     */
    private Integer maxGlyphsPerFace;

    /**
     * A flag to set when Flex is running on a server without a display
     * <p>
     * Equivalent to -compiler.headless-server
     * </p>
     * 
     * @parameter expression="${flex.headlessServer}"
     */
    private Boolean headlessServer;

    /**
     * Enables incremental compilation
     * <p>
     * Equivalent to -compiler.incremental
     * </p>
     * 
     * @parameter expression="${flex.incremental}"
     */
    private Boolean incremental;

    /**
     * Disables the pruning of unused CSS type selectors
     * <p>
     * Equivalent to -compiler.keep-all-type-selectors
     * </p>
     * 
     * @parameter expression="${flex.keepAllTypeSelectors}"
     */
    private Boolean keepAllTypeSelectors;

    /**
     * Keep the specified metadata in the SWF
     * <p>
     * Equivalent to -compiler.keep-as3-metadata
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;keepAs3Metadatas&gt;
     *   &lt;keepAs3Metadata&gt;Bindable&lt;/keepAs3Metadata&gt;
     *   &lt;keepAs3Metadata&gt;Events&lt;/keepAs3Metadata&gt;
     * &lt;/keepAs3Metadatas&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] keepAs3Metadatas;

    /**
     * Keep the specified metadata in the SWF
     * <p>
     * Equivalent to -compiler.keep-generated-actionscript
     * </p>
     * 
     * @parameter expression="${flex.keepGeneratedActionscript}"
     */
    private Boolean keepGeneratedActionscript;

    /**
     * DOCME undocumented by adobe
     * <p>
     * Equivalent to -compiler.keep-generated-signatures
     * </p>
     * 
     * @parameter expression="${flex.keepGeneratedSignatures}"
     */
    private Boolean keepGeneratedSignatures;

    /**
     * DOCME undocumented by adobe
     * <p>
     * Equivalent to -compiler.memory-usage-factor
     * </p>
     * 
     * @parameter expression="${flex.memoryUsageFactor}"
     */
    private Integer memoryUsageFactor;

    /**
     * Specifies a compatibility version
     * <p>
     * Equivalent to -compiler.mxml.compatibility-version
     * </p>
     * 
     * @parameter expression="${flex.compatibilityVersion}"
     */
    private String compatibilityVersion;

    /**
     * Specify a URI to associate with a manifest of components for use as MXML elements
     * <p>
     * Equivalent to -compiler.namespaces.namespace
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;namespaces&gt;
     *   &lt;namespace&gt;
     *     &lt;uri&gt;http://www.adobe.com/2006/mxml&lt;/uri&gt;
     *     &lt;manifest&gt;${basedir}/manifest.xml&lt;/manifest&gt;
     *   &lt;/namespace&gt;
     * &lt;/namespaces&gt;
     * </pre>
     * 
     * @parameter
     */
    private MavenNamespaces[] namespaces;

    /**
     * Enable post-link SWF optimization
     * <p>
     * Equivalent to -compiler.optimize
     * </p>
     * 
     * @parameter expression="${flex.optimize}"
     */
    private Boolean optimize;

    /**
     * This undocumented option is for compiler performance testing. It allows the Flex 3 compiler to compile the Flex 2
     * framework and Flex 2 apps. This is not an officially-supported combination
     * <p>
     * Equivalent to -compiler.resource-hack
     * </p>
     * 
     * @parameter expression="${flex.resourceHack}"
     */
    private Boolean resourceHack;

    /**
     * Path to Flex Data Services configuration file
     * <p>
     * Equivalent to -compiler.services
     * </p>
     * 
     * @parameter expression="${flex.services}"
     */
    private File services;

    /**
     * A list of warnings that should be enabled/disabled
     * <p>
     * Equivalent to -compiler.show-actionscript-warnings, -compiler.show-binding-warnings,
     * -compiler.show-shadowed-device-font-warnings, -compiler.show-unused-type-selector-warnings and -compiler.warn-*
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;compilerWarnings&gt;
     *   &lt;show-actionscript-warnings&gt;true&lt;/show-actionscript-warnings&gt;
     *   &lt;warn-bad-nan-comparison&gt;false&lt;/warn-bad-nan-comparison&gt;
     * &lt;/compilerWarnings&gt;
     * </pre>
     * 
     * @parameter
     */
    private Map<String, Boolean> compilerWarnings = new LinkedHashMap<String, Boolean>();

    /**
     * DOCME undocumented by adobe
     * <p>
     * Equivalent to -compiler.signature-directory
     * </p>
     * 
     * @parameter expression="${flex.signatureDirectory}"
     */
    private File signatureDirectory;

    /**
     * Runs the AS3 compiler in strict error checking mode
     * <p>
     * Equivalent to -compiler.strict
     * </p>
     * 
     * @parameter expression="${flex.strict}"
     */
    private Boolean strict;

    /**
     * DOCME undocumented by adobe
     * <p>
     * Equivalent to -compiler.translation-format
     * </p>
     * 
     * @parameter expression="${flex.translationFormat}"
     */
    private String translationFormat;

    /**
     * Determines whether resources bundles are included in the application
     * <p>
     * Equivalent to -compiler.use-resource-bundle-metadata
     * </p>
     * 
     * @parameter expression="${flex.useResourceBundleMetadata}"
     */
    private Boolean useResourceBundleMetadata;

    /**
     * Save callstack information to the SWF for debugging
     * <p>
     * Equivalent to -compiler.verbose-stacktraces
     * </p>
     * 
     * @parameter expression="${flex.verboseStacktraces}"
     */
    private Boolean verboseStacktraces;

    /**
     * Inclusion/exclusion patterns used to filter resources to be include in the output SWC
     * <p>
     * Equivalent to -include-file
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeFiles&gt;
     *   &lt;includeFile&gt;
     *     &lt;includes&gt;
     *       &lt;include&gt;*.xml&lt;/include&gt;
     *     &lt;/includes&gt;
     *     &lt;excludes&gt;
     *       &lt;exclude&gt;excluded-*.xml&lt;/exclude&gt;
     *     &lt;/excludes&gt;
     *   &lt;/includeFile&gt;
     * &lt;/includeFiles&gt;
     * </pre>
     * 
     * @parameter
     */
    private PatternSet[] includeFiles;

    /**
     * Inclusion/exclusion patterns used to filter classes to include in the output SWC
     * <p>
     * Equivalent to -include-classes
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeClasses&gt;
     *   &lt;includeClasse&gt;
     *     &lt;includes&gt;
     *       &lt;include&gt;com/mycompany/*&lt;/include&gt;
     *     &lt;/includes&gt;
     *     &lt;excludes&gt;
     *       &lt;exclude&gt;com/mycompany/ui/*&lt;/exclude&gt;
     *     &lt;/excludes&gt;
     *   &lt;/includeClasse&gt;
     * &lt;/includeClasses&gt;
     * </pre>
     * 
     * @parameter
     */
    private PatternSet[] includeClasses;

    /**
     * All classes in the listed namespaces are included in the output SWC
     * <p>
     * Equivalent to -include-namespaces
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeNamespaces&gt;
     *   &lt;includeNamespace&gt;
     *     &lt;namespace&gt;http://mynamespace.com&lt;/namespace&gt;
     *   &lt;/includeNamespace&gt;
     * &lt;/includeNamespaces&gt;
     * </pre>
     * 
     * @parameter
     */
    private List<String> includeNamespaces;

    /**
     * A list of directories and source files to include in the output SWC
     * <p>
     * Equivalent to -include-sources
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeSources&gt;
     *   &lt;includeSource&gt;${project.build.sourceDirectory}&lt;/includeSource&gt;
     * &lt;/includeSources&gt;
     * </pre>
     * 
     * @parameter
     */
    private File[] includeSources;

    /**
     * A list of named stylesheet resources to include in the output SWC
     * <p>
     * Equivalent to -include-stylesheet
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeStylesheets&gt;
     *   &lt;stylesheet&gt;${basedir}/mystyle.css&lt;/stylesheet&gt;
     * &lt;/includeStylesheets&gt;
     * </pre>
     * 
     * @parameter
     */
    private File[] includeStylesheets;

    /**
     * Specifies the locale for internationalization
     * <p>
     * Equivalent to -compiler.locale
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;compilerLocales&gt;
     *   &lt;locale&gt;en_US&lt;/locale&gt;
     * &lt;/compilerLocales&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] compilerLocales;

    /**
     * A list of resource bundles to include in the output SWC
     * <p>
     * Equivalent to -include-resource-bundles
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeResourceBundles&gt;
     *   &lt;rb&gt;SharedResources&lt;/rb&gt;
     *   &lt;rb&gt;Collections&lt;/rb&gt;
     * &lt;/includeResourceBundles&gt;
     * </pre>
     * 
     * @parameter
     */
    private List<String> includeResourceBundles;

    /**
     * Prints a list of resource bundles to a file for input to the compc compiler to create a resource bundle SWC file.
     * <p>
     * Equivalent to -resource-bundle-list
     * </p>
     * 
     * @parameter expression="${flex.resourceBundleList}"
     */
    private File resourceBundleList;

    /**
     * List of CSS or SWC files to apply as a theme
     * <p>
     * Equivalent to -compiler.theme
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;themes&gt;
     *    &lt;theme&gt;css/main.css&lt;/theme&gt;
     * &lt;/themes&gt;
     * </pre>
     * 
     * If you are using SWC theme should be better keep it's version controlled, so is advised to use a dependency with
     * theme scope.<BR>
     * Like this:
     * 
     * <pre>
     * &lt;dependency&gt;
     *   &lt;groupId&gt;com.acme&lt;/groupId&gt;
     *   &lt;artifactId&gt;acme-theme&lt;/artifactId&gt;
     *   &lt;type&gt;swc&lt;/type&gt;
     *   &lt;scope&gt;theme&lt;/scope&gt;
     *   &lt;version&gt;1.0&lt;/version&gt;
     * &lt;/dependency&gt;
     * </pre>
     * 
     * @parameter
     */
    private File[] themes;

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
        if ( includeClasses == null )
        {
            return null;
        }

        List<File> directories = new ArrayList<File>();
        for ( String sourceRoot : sourcePaths )
        {
            directories.add( new File( sourceRoot ) );
        }

        List<String> classes = new ArrayList<String>();

        for ( Resource resource : resources )
        {
            File directory = new File( resource.getDirectory() );
            if ( !directory.exists() )
            {
                continue;
            }

            for ( PatternSet pattern : includeClasses )
            {
                DirectoryScanner scanner = scan( directory, pattern );

                String[] included = scanner.getIncludedFiles();
                for ( String file : included )
                {
                    String classname = file;
                    classname = classname.replaceAll( "\\.(.)*", "" );
                    classname = classname.replace( '\\', '.' );
                    classname = classname.replace( '/', '.' );
                    classes.add( classname );
                }
            }
        }

        return classes;
    }

    private DirectoryScanner scan( File directory, PatternSet pattern )
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( directory );
        scanner.setIncludes( (String[]) pattern.getIncludes().toArray( new String[0] ) );
        scanner.setExcludes( (String[]) pattern.getExcludes().toArray( new String[0] ) );
        scanner.addDefaultExcludes();
        scanner.scan();
        return scanner;
    }

    public File[] getIncludeFile()
    {
        PatternSet[] patterns;
        if ( includeFiles == null && includeNamespaces == null && includeSources == null && includeClasses == null )
        {
            PatternSet pattern = new PatternSet();
            pattern.addInclude( "*.*" );
            patterns = new PatternSet[] { pattern };
        }
        else if ( includeFiles == null )
        {
            return null;
        }
        else
        {
            patterns = includeFiles;
        }

        List<File> files = new ArrayList<File>();

        for ( Resource resource : resources )
        {
            File directory = new File( resource.getDirectory() );
            if ( !directory.exists() )
            {
                continue;
            }

            for ( PatternSet pattern : patterns )
            {
                DirectoryScanner scanner = scan( directory, pattern );

                String[] included = scanner.getIncludedFiles();
                for ( String file : included )
                {
                    files.add( new File( directory, file ) );
                }
            }
        }

        return files.toArray( new File[0] );
    }

    public Boolean getIncludeLookupOnly()
    {
        return includeLookupOnly;
    }

    public List<String> getIncludeNamespaces()
    {
        return includeNamespaces;
    }

    public List<String> getIncludeResourceBundles()
    {
        return includeResourceBundles;
    }

    public File[] getIncludeSources()
    {
        if ( includeFiles == null && includeNamespaces == null && includeSources == null && includeClasses == null )
        {
            return PathUtil.getFiles( sourcePaths );
        }
        return includeSources;
    }

    public File[] getIncludeStylesheet()
    {
        return includeStylesheets;
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
            return PathUtil.getCanonicalPath( loadConfigs );
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
        if ( loadExterns == null )
        {
            Set<Artifact> dependencies = getDependencies( FlexClassifier.LINK_REPORT, FlexExtension.ZIP, null );

            if ( dependencies.isEmpty() )
            {
                return null;
            }

            return PathUtil.getCanonicalPath( MavenUtils.getFilesSet( dependencies ) );
        }
        return PathUtil.getCanonicalPath( loadExterns );
    }

    public IMetadataConfiguration getMetadataConfiguration()
    {
        return this;
    }

    public String[] getContributor()
    {
        if ( this.metadata != null )
        {
            return this.metadata.getContributor();
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

    public String[] getCreator()
    {
        if ( this.metadata != null )
        {
            return this.metadata.getCreator();
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
        if ( this.metadata != null && this.metadata.getDate() != null )
        {
            return this.metadata.getDate();
        }

        return DATE_FORMAT.format( new Date() );
    }

    public String getDescription()
    {
        if ( this.metadata != null )
        {
            return this.metadata.getDescription();
        }

        return project.getDescription();
    }

    public String[] getLanguage()
    {
        if ( this.metadata != null )
        {
            return this.metadata.getLanguage();
        }

        return getLocale();
    }

    public ILocalizedDescription[] getLocalizedDescription()
    {
        if ( this.metadata != null )
        {
            return this.metadata.getLocalizedDescription();
        }

        return null;
    }

    public ILocalizedTitle[] getLocalizedTitle()
    {
        if ( this.metadata != null )
        {
            return this.metadata.getLocalizedTitle();
        }

        return null;
    }

    public String[] getPublisher()
    {
        if ( this.metadata != null )
        {
            return this.metadata.getPublisher();
        }

        return getCreator();
    }

    public String getTitle()
    {
        if ( this.metadata != null )
        {
            return this.metadata.getDescription();
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
        return PathUtil.getCanonicalPath( resourceBundleList );
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
        return targetPlayer;
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

    public List<String> getDefaultsCssFiles()
    {
        return PathUtil.getCanonicalPathList( defaultsCssFiles );
    }

    public String getDefaultsCssUrl()
    {
        return defaultsCssUrl;
    }

    public IDefine[] getDefine()
    {
        if ( defines == null )
        {
            return null;
        }

        List<IDefine> keys = new ArrayList<IDefine>();
        Set<Entry<Object, Object>> entries = this.defines.entrySet();
        for ( final Entry<Object, Object> entry : entries )
        {
            keys.add( new IDefine()
            {
                public String value()
                {
                    return entry.getValue().toString();
                }

                public String name()
                {
                    return entry.getKey().toString();
                }
            } );
        }

        return keys.toArray( new IDefine[keys.size()] );
    }

    public Boolean getDisableIncrementalOptimizations()
    {
        return disableIncrementalOptimizations;
    }

    public Boolean getDoc()
    {
        return doc;
    }

    public Boolean getEs()
    {
        return es;
    }

    public File[] getExternalLibraryPath()
    {
        // TODO on swc include compile dependencies too
        ArrayUtils.addAll( MavenUtils.getFiles( getDependencies( null, null, EXTERNAL ) ),
                           MavenUtils.getFiles( getDependencies( null, null, COMPILE ) ) );
        return MavenUtils.getFiles( getDependencies( null, null, EXTERNAL ) );
    }

    public IFontsConfiguration getFontsConfiguration()
    {
        return this;
    }

    public Boolean getAdvancedAntiAliasing()
    {
        return advancedAntiAliasing;
    }

    public Boolean getFlashType()
    {
        return flashType;
    }

    public ILanguages getLanguagesConfiguration()
    {
        return this;
    }

    public ILanguageRange[] getLanguageRange()
    {
        if ( licenses == null )
        {
            return null;
        }

        List<ILanguageRange> keys = new ArrayList<ILanguageRange>();
        Set<Entry<String, String>> entries = this.languageRange.entrySet();
        for ( final Entry<String, String> entry : entries )
        {
            keys.add( new ILanguageRange()
            {
                public String range()
                {
                    return entry.getValue();
                }

                public String lang()
                {
                    return entry.getKey();
                }
            } );
        }

        return keys.toArray( new ILanguageRange[keys.size()] );
    }

    public String getLocalFontsSnapshot()
    {
        if ( localFontsSnapshot != null )
        {
            return PathUtil.getCanonicalPath( localFontsSnapshot );
        }

        URL url;
        if ( MavenUtils.isMac() )
        {
            url = getClass().getResource( "/fonts/macFonts.ser" );
        }
        else
        {
            // TODO And linux?!
            // if(os.contains("windows")) {
            url = getClass().getResource( "/fonts/winFonts.ser" );
        }
        File fontsSer = new File( project.getBuild().getOutputDirectory(), "fonts.ser" );
        try
        {
            FileUtils.copyURLToFile( url, fontsSer );
        }
        catch ( IOException e )
        {
            throw new IllegalStateException( "Error copying fonts file.", e );
        }
        return PathUtil.getCanonicalPath( fontsSer );
    }

    public List<String> getManagers()
    {
        return managers;
    }

    public String getMaxCachedFonts()
    {
        if ( maxCachedFonts == null )
        {
            return null;
        }
        return maxCachedFonts.toString();
    }

    public String getMaxGlyphsPerFace()
    {
        if ( maxGlyphsPerFace == null )
        {
            return null;
        }
        return maxGlyphsPerFace.toString();
    }

    public Boolean getHeadlessServer()
    {
        if ( headlessServer == null )
        {
            return GraphicsEnvironment.isHeadless();
        }

        return headlessServer;
    }

    public File[] getIncludeLibraries()
    {
        return MavenUtils.getFiles( getDependencies( null, null, INTERNAL ) );
    }

    public Boolean getIncremental()
    {
        return incremental;
    }

    public Boolean getKeepAllTypeSelectors()
    {
        return keepAllTypeSelectors;
    }

    public String[] getKeepAs3Metadata()
    {
        return keepAs3Metadatas;
    }

    public Boolean getKeepGeneratedActionscript()
    {
        return keepGeneratedActionscript;
    }

    public Boolean getKeepGeneratedSignatures()
    {
        return keepGeneratedSignatures;
    }

    public File[] getLibraryPath()
    {
        // TODO on SWF include compile dependencies too
        return MavenUtils.getFiles( getDependencies( null, null, MERGED ) );
    }

    public String[] getLocale()
    {
        return compilerLocales;
    }

    public Integer getMemoryUsageFactor()
    {
        return memoryUsageFactor;
    }

    public IMxmlConfiguration getMxmlConfiguration()
    {
        return this;
    }

    public String getCompatibilityVersion()
    {
        return compatibilityVersion;
    }

    public INamespacesConfiguration getNamespacesConfiguration()
    {
        return this;
    }

    public INamespace[] getNamespace()
    {
        return namespaces;
    }

    public Boolean getOptimize()
    {
        return optimize;
    }

    public Boolean getResourceHack()
    {
        return resourceHack;
    }

    public String getServices()
    {
        if ( services != null )
        {
            return PathUtil.getCanonicalPath( services );
        }

        File cfg = new File( configDirectory, "services-config.xml" );
        if ( cfg.exists() )
        {
            return PathUtil.getCanonicalPath( cfg );
        }
        return null;
    }

    public Boolean getShowActionscriptWarnings()
    {
        return compilerWarnings.get( "show-actionscript-warnings" );
    }

    public Boolean getShowBindingWarnings()
    {
        return compilerWarnings.get( "show-binding-warnings" );
    }

    public Boolean getShowDependencyWarnings()
    {
        return compilerWarnings.get( "show-dependency-warnings" );
    }

    public Boolean getShowDeprecationWarnings()
    {
        return compilerWarnings.get( "show-deprecation-warnings" );
    }

    public Boolean getShowShadowedDeviceFontWarnings()
    {
        return compilerWarnings.get( "show-shadowed-device-font-warnings" );
    }

    public Boolean getShowUnusedTypeSelectorWarnings()
    {
        return compilerWarnings.get( "show-unused-type-selector-warnings" );
    }

    public File getSignatureDirectory()
    {
        return signatureDirectory;
    }

    public File[] getSourcePath()
    {
        return PathUtil.getFiles( sourcePaths );
    }

    public Boolean getStrict()
    {
        return strict;
    }

    public List<String> getTheme()
    {
        List<String> themes = new ArrayList<String>();
        if ( this.themes != null )
        {
            themes.addAll( PathUtil.getCanonicalPathList( this.themes ) );
        }
        themes.addAll( PathUtil.getCanonicalPathList( MavenUtils.getFiles( getDependencies( null, null, THEME ) ) ) );
        return themes;
    }

    public String getTranslationFormat()
    {
        return translationFormat;
    }

    public Boolean getUseResourceBundleMetadata()
    {
        return useResourceBundleMetadata;
    }

    public Boolean getVerboseStacktraces()
    {
        return verboseStacktraces;
    }

    public Boolean getWarnArrayTostringChanges()
    {
        return compilerWarnings.get( "warn-array-tostring-changes" );
    }

    public Boolean getWarnAssignmentWithinConditional()
    {
        return compilerWarnings.get( "warn-assignment-within-conditional" );
    }

    public Boolean getWarnBadArrayCast()
    {
        return compilerWarnings.get( "warn-bad-array-cast" );
    }

    public Boolean getWarnBadBoolAssignment()
    {
        return compilerWarnings.get( "warn-bad-bool-assignment" );
    }

    public Boolean getWarnBadDateCast()
    {
        return compilerWarnings.get( "warn-bad-date-cast" );
    }

    public Boolean getWarnBadEs3TypeMethod()
    {
        return compilerWarnings.get( "warn-bad-es3-type-method" );
    }

    public Boolean getWarnBadEs3TypeProp()
    {
        return compilerWarnings.get( "warn-bad-es3-type-prop" );
    }

    public Boolean getWarnBadNanComparison()
    {
        return compilerWarnings.get( "warn-bad-nan-comparison" );
    }

    public Boolean getWarnBadNullAssignment()
    {
        return compilerWarnings.get( "warn-bad-null-assignment" );
    }

    public Boolean getWarnBadNullComparison()
    {
        return compilerWarnings.get( "warn-bad-null-comparison" );
    }

    public Boolean getWarnBadUndefinedComparison()
    {
        return compilerWarnings.get( "warn-bad-undefined-comparison" );
    }

    public Boolean getWarnBooleanConstructorWithNoArgs()
    {
        return compilerWarnings.get( "warn-boolean-constructor-with-no-args" );
    }

    public Boolean getWarnChangesInResolve()
    {
        return compilerWarnings.get( "warn-changes-in-resolve" );
    }

    public Boolean getWarnClassIsSealed()
    {
        return compilerWarnings.get( "warn-class-is-sealed" );
    }

    public Boolean getWarnConstNotInitialized()
    {
        return compilerWarnings.get( "warn-const-not-initialized" );
    }

    public Boolean getWarnConstructorReturnsValue()
    {
        return compilerWarnings.get( "warn-constructor-returns-value" );
    }

    public Boolean getWarnDeprecatedEventHandlerError()
    {
        return compilerWarnings.get( "warn-deprecated-event-handler-error" );
    }

    public Boolean getWarnDeprecatedFunctionError()
    {
        return compilerWarnings.get( "warn-deprecated-function-error" );
    }

    public Boolean getWarnDeprecatedPropertyError()
    {
        return compilerWarnings.get( "warn-deprecated-property-error" );
    }

    public Boolean getWarnDuplicateArgumentNames()
    {
        return compilerWarnings.get( "warn-duplicate-argument-names" );
    }

    public Boolean getWarnDuplicateVariableDef()
    {
        return compilerWarnings.get( "warn-duplicate-variable-def" );
    }

    public Boolean getWarnForVarInChanges()
    {
        return compilerWarnings.get( "warn-for-var-in-changes" );
    }

    public Boolean getWarnImportHidesClass()
    {
        return compilerWarnings.get( "warn-import-hides-class" );
    }

    public Boolean getWarnInstanceOfChanges()
    {
        return compilerWarnings.get( "warn-instance-of-changes" );
    }

    public Boolean getWarnInternalError()
    {
        return compilerWarnings.get( "warn-internal-error" );
    }

    public Boolean getWarnLevelNotSupported()
    {
        return compilerWarnings.get( "warn-level-not-supported" );
    }

    public Boolean getWarnMissingNamespaceDecl()
    {
        return compilerWarnings.get( "warn-missing-namespace-decl" );
    }

    public Boolean getWarnNegativeUintLiteral()
    {
        return compilerWarnings.get( "warn-negative-uint-literal" );
    }

    public Boolean getWarnNoConstructor()
    {
        return compilerWarnings.get( "warn-no-constructor" );
    }

    public Boolean getWarnNoExplicitSuperCallInConstructor()
    {
        return compilerWarnings.get( "warn-no-explicit-super-call-in-constructor" );
    }

    public Boolean getWarnNoTypeDecl()
    {
        return compilerWarnings.get( "warn-no-type-decl" );
    }

    public Boolean getWarnNumberFromStringChanges()
    {
        return compilerWarnings.get( "warn-number-from-string-changes" );
    }

    public Boolean getWarnScopingChangeInThis()
    {
        return compilerWarnings.get( "warn-scoping-change-in-this" );
    }

    public Boolean getWarnSlowTextFieldAddition()
    {
        return compilerWarnings.get( "warn-slow-text-field-addition" );
    }

    public Boolean getWarnUnlikelyFunctionValue()
    {
        return compilerWarnings.get( "warn-unlikely-function-value" );
    }

    public Boolean getWarnXmlClassHasChanged()
    {
        return compilerWarnings.get( "warn-xml-class-has-changed" );
    }

    @SuppressWarnings( "unchecked" )
    private Set<Artifact> getDependencies( String classifier, String type, String scope )
    {
        Set<Artifact> dependencies = getDependencies();
        FilterArtifacts filter = new FilterArtifacts();

        if ( classifier != null )
        {
            filter.addFilter( new ClassifierFilter( classifier, null ) );
        }
        if ( type != null )
        {
            filter.addFilter( new TypeFilter( type, null ) );
        }
        if ( scope != null )
        {
            filter.addFilter( new ScopeFilter( scope, null ) );
        }

        Set filteredDependencies;
        try
        {
            filteredDependencies = filter.filter( dependencies );
        }
        catch ( ArtifactFilterException e )
        {
            throw new MavenRuntimeException( e );
        }

        if ( filteredDependencies.isEmpty() )
        {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet( filteredDependencies );
    }

    public Set<Artifact> getDependencies()
    {
        return Collections.unmodifiableSet( project.getDependencyArtifacts() );
    }

}
