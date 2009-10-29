package org.sonatype.flexmojos.common;

import static org.sonatype.flexmojos.common.FlexExtension.SWC;
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
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Developer;
import org.apache.maven.model.PatternSet;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.apache.maven.shared.artifact.filter.collection.ClassifierFilter;
import org.apache.maven.shared.artifact.filter.collection.FilterArtifacts;
import org.apache.maven.shared.artifact.filter.collection.ScopeFilter;
import org.apache.maven.shared.artifact.filter.collection.TypeFilter;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.compiler.FlexCompiler;
import org.sonatype.flexmojos.compiler.FrameLabel;
import org.sonatype.flexmojos.compiler.ICompilerConfiguration;
import org.sonatype.flexmojos.compiler.IDefaultScriptLimits;
import org.sonatype.flexmojos.compiler.IDefaultSize;
import org.sonatype.flexmojos.compiler.IDefine;
import org.sonatype.flexmojos.compiler.IExtensionsConfiguration;
import org.sonatype.flexmojos.compiler.IFontsConfiguration;
import org.sonatype.flexmojos.compiler.IFrame;
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

public abstract class AbstractMavenFlexCompilerConfiguration
    extends AbstractMojo
    implements ICompilerConfiguration, IFramesConfiguration, ILicensesConfiguration, IMetadataConfiguration,
    IFontsConfiguration, ILanguages, IMxmlConfiguration, INamespacesConfiguration
{

    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat();

    /**
     * @parameter expression="${plugin.artifacts}"
     * @readonly
     */
    protected List<Artifact> pluginArtifacts;

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
     * Enables advanced anti-aliasing for embedded fonts, which provides greater clarity for small fonts
     * <p>
     * Equivalent to -compiler.fonts.advanced-anti-aliasing
     * </p>
     * 
     * @parameter expression="${flex.advancedAntiAliasing}"
     */
    private Boolean advancedAntiAliasing;

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
     * Output performance benchmark
     * <p>
     * Equivalent to -benchmark
     * </p>
     * 
     * @parameter expression="${flex.benchmark}"
     */
    protected Boolean benchmark;

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
     * The maven configuration directory
     * 
     * @parameter expression="${basedir}/src/main/config"
     * @required
     * @readonly
     */
    protected File configDirectory;

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
     * Write a file containing all currently set configuration values in a format suitable for use as a flex config file
     * <p>
     * Equivalent to -dump-config
     * </p>
     * 
     * @parameter expression="${flex.dumpConfig}"
     */
    private File dumpConfig;

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
     * Enables FlashType for embedded fonts, which provides greater clarity for small fonts
     * <p>
     * Equivalent to -compiler.fonts.flash-type
     * </p>
     * 
     * @parameter expression="${flex.flashType}"
     */
    private Boolean flashType;

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
     * A flag to set when Flex is running on a server without a display
     * <p>
     * Equivalent to -compiler.headless-server
     * </p>
     * 
     * @parameter expression="${flex.headlessServer}"
     */
    private Boolean headlessServer;

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
     * DOCME undocumented by adobe
     * <p>
     * Equivalent to -compiler.memory-usage-factor
     * </p>
     * 
     * @parameter expression="${flex.memoryUsageFactor}"
     */
    private Integer memoryUsageFactor;

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
     * The filename of the SWF movie to create
     * <p>
     * Equivalent to -output
     * </p>
     * 
     * @parameter expression="${flex.output}"
     */
    private String output;

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

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
     * Prints a list of resource bundles to a file for input to the compc compiler to create a resource bundle SWC file.
     * <p>
     * Equivalent to -resource-bundle-list
     * </p>
     * 
     * @parameter expression="${flex.resourceBundleList}"
     */
    private File resourceBundleList;

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
     * The maven resources
     * 
     * @parameter expression="${project.build.resources}"
     * @required
     * @readonly
     */
    protected List<Resource> resources;

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
     * Path to Flex Data Services configuration file
     * <p>
     * Equivalent to -compiler.services
     * </p>
     * 
     * @parameter expression="${flex.services}"
     */
    private File services;

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
     * Statically link the libraries specified by the -runtime-shared-libraries-path option.
     * <p>
     * Equivalent to -static-link-runtime-shared-libraries
     * </p>
     * 
     * @parameter expression="${flex.staticLinkRuntimeSharedLibraries}"
     */
    private Boolean staticLinkRuntimeSharedLibraries;

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
     * Toggle whether the SWF is flagged for access to network resources
     * <p>
     * Equivalent to -use-network
     * </p>
     * 
     * @parameter expression="${flex.userNetwork}"
     */
    private Boolean userNetwork;

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

    public Boolean getAdvancedAntiAliasing()
    {
        return advancedAntiAliasing;
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

    public Boolean getBenchmark()
    {
        return benchmark;
    }

    public String getCompatibilityVersion()
    {
        return compatibilityVersion;
    }

    public ICompilerConfiguration getCompilerConfiguration()
    {
        return this;
    }

    public Boolean getConservative()
    {
        return conservative;
    }

    public String getContextRoot()
    {
        return contextRoot;
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

    public Boolean getDebug()
    {
        return debug;
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

    public List<String> getDefaultsCssFiles()
    {
        return PathUtil.getCanonicalPathList( defaultsCssFiles );
    }

    public String getDefaultsCssUrl()
    {
        return defaultsCssUrl;
    }

    public IDefaultSize getDefaultSize()
    {
        return defaultSize;
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
                public String name()
                {
                    return entry.getKey().toString();
                }

                public String value()
                {
                    return entry.getValue().toString();
                }
            } );
        }

        return keys.toArray( new IDefine[keys.size()] );
    }

    protected Set<Artifact> getDependencies()
    {
        return Collections.unmodifiableSet( project.getDependencyArtifacts() );
    }

    @SuppressWarnings( "unchecked" )
    protected Set<Artifact> getDependencies( String classifier, String type, String scope )
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

    public String getDescription()
    {
        if ( this.metadata != null )
        {
            return this.metadata.getDescription();
        }

        return project.getDescription();
    }

    public Boolean getDisableIncrementalOptimizations()
    {
        return disableIncrementalOptimizations;
    }

    public Boolean getDoc()
    {
        return doc;
    }

    public String getDumpConfig()
    {
        return PathUtil.getCanonicalPath( dumpConfig );
    }

    public Boolean getEs()
    {
        return es;
    }

    public List<String> getExterns()
    {
        if ( externs == null )
        {
            return null;
        }

        return Arrays.asList( externs );
    }

    public Boolean getFlashType()
    {
        return flashType;
    }

    public IFontsConfiguration getFontsConfiguration()
    {
        return this;
    }

    public IFrame[] getFrame()
    {
        // TODO
        // return frames;
        return null;
    }

    public IFramesConfiguration getFramesConfiguration()
    {
        return this;
    }

    public Boolean getGenerateFrameLoader()
    {
        return generateFrameLoader;
    }

    public Boolean getHeadlessServer()
    {
        if ( headlessServer == null )
        {
            return GraphicsEnvironment.isHeadless();
        }

        return headlessServer;
    }

    public final String[] getHelp()
    {
        // must return null, otherwise will prevent compiler execution
        return null;
    }

    public File[] getIncludeLibraries()
    {
        return MavenUtils.getFiles( getDependencies( null, null, INTERNAL ) );
    }

    public Boolean getIncludeLookupOnly()
    {
        return includeLookupOnly;
    }

    public List<String> getIncludeResourceBundles()
    {
        return includeResourceBundles;
    }

    public List<String> getIncludes()
    {
        if ( includes == null )
        {
            return null;
        }
        return Arrays.asList( includes );
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

    public String[] getLanguage()
    {
        if ( this.metadata != null )
        {
            return this.metadata.getLanguage();
        }

        return getLocale();
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
                public String lang()
                {
                    return entry.getKey();
                }

                public String range()
                {
                    return entry.getValue();
                }
            } );
        }

        return keys.toArray( new ILanguageRange[keys.size()] );
    }

    public ILanguages getLanguagesConfiguration()
    {
        return this;
    }

    public Boolean getLazyInit()
    {
        return lazyInit;
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
                public String product()
                {
                    return entry.getKey();
                }

                public String serialNumber()
                {
                    return entry.getValue();
                }
            } );
        }

        return keys.toArray( new ILicense[keys.size()] );
    }

    public ILicensesConfiguration getLicensesConfiguration()
    {
        return this;
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

    public String[] getLocale()
    {
        return compilerLocales;
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

    public Integer getMemoryUsageFactor()
    {
        return memoryUsageFactor;
    }

    public IMetadataConfiguration getMetadataConfiguration()
    {
        return this;
    }

    public IMxmlConfiguration getMxmlConfiguration()
    {
        return this;
    }

    public INamespace[] getNamespace()
    {
        return namespaces;
    }

    public INamespacesConfiguration getNamespacesConfiguration()
    {
        return this;
    }

    public Boolean getOptimize()
    {
        return optimize;
    }

    public String getOutput()
    {
        return output;
    }

    public String[] getPublisher()
    {
        if ( this.metadata != null )
        {
            return this.metadata.getPublisher();
        }

        return getCreator();
    }

    public String getRawMetadata()
    {
        return rawMetadata;
    }

    public String getResourceBundleList()
    {
        return PathUtil.getCanonicalPath( resourceBundleList );
    }

    public Boolean getResourceHack()
    {
        return resourceHack;
    }

    public String[] getRuntimeSharedLibraries()
    {
        return runtimeSharedLibraries;
    }

    public String[][] getRuntimeSharedLibraryPath()
    {
        // TODO Auto-generated method stub
        return null;
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

    public Boolean getStaticLinkRuntimeSharedLibraries()
    {
        return staticLinkRuntimeSharedLibraries;
    }

    public Boolean getStrict()
    {
        return strict;
    }

    public Boolean getSwcChecksum()
    {
        return swcChecksum;
    }

    public String getTargetPlayer()
    {
        return targetPlayer;
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

    public String getTitle()
    {
        if ( this.metadata != null )
        {
            return this.metadata.getDescription();
        }

        return project.getName();
    }

    public String getTranslationFormat()
    {
        return translationFormat;
    }

    public Boolean getUseNetwork()
    {
        return userNetwork;
    }

    public Boolean getUseResourceBundleMetadata()
    {
        return useResourceBundleMetadata;
    }

    public Boolean getVerboseStacktraces()
    {
        return verboseStacktraces;
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

    public Boolean getWarnings()
    {
        return warnings;
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

    protected DirectoryScanner scan( File directory, PatternSet pattern )
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( directory );
        scanner.setIncludes( (String[]) pattern.getIncludes().toArray( new String[0] ) );
        scanner.setExcludes( (String[]) pattern.getExcludes().toArray( new String[0] ) );
        scanner.addDefaultExcludes();
        scanner.scan();
        return scanner;
    }

    public File[] getExternalLibraryPath()
    {
        if ( SWC.equals( project.getPackaging() ) )
        {
            return (File[]) ArrayUtils.addAll( MavenUtils.getFiles( getDependencies( null, null, EXTERNAL ) ),
                                               MavenUtils.getFiles( getDependencies( null, null, COMPILE ) ) );
        }
        else
        {
            return MavenUtils.getFiles( getDependencies( null, null, EXTERNAL ) );
        }
    }

    public File[] getLibraryPath()
    {
        if ( SWC.equals( project.getPackaging() ) )
        {
            return MavenUtils.getFiles( getDependencies( null, null, MERGED ) );
        }
        else
        {
            return (File[]) ArrayUtils.addAll( MavenUtils.getFiles( getDependencies( null, null, MERGED ) ),
                                               MavenUtils.getFiles( getDependencies( null, null, COMPILE ) ) );
        }
    }

    /**
     * @component
     */
    protected FlexCompiler compiler;

    protected List<String> filterClasses( PatternSet[] classesPattern, File[] directories )
    {
        List<String> classes = new ArrayList<String>();

        for ( File directory : directories )
        {
            if ( !directory.exists() )
            {
                continue;
            }

            for ( PatternSet pattern : classesPattern )
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

    protected List<String> getNamespacesUri()
    {
        if ( getNamespace() == null || getNamespace().length == 0 )
        {
            return null;
        }

        List<String> uris = new ArrayList<String>();
        for ( INamespace namespace : getNamespace() )
        {
            uris.add( namespace.uri() );
        }

        return uris;
    }

    public String getFlexVersion()
    {
        Artifact compiler = MavenUtils.searchFor( pluginArtifacts, "com.adobe.flex", "compiler", null, "pom", null );
        return compiler.getVersion();
    }

    /**
     * @component
     * @readonly
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @component
     * @readonly
     */
    protected ArtifactResolver resolver;

    /**
     * Local repository to be used by the plugin to resolve dependencies.
     * 
     * @parameter expression="${localRepository}"
     */
    protected ArtifactRepository localRepository;

    /**
     * List of remote repositories to be used by the plugin to resolve dependencies.
     * 
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    protected List<ArtifactRepository> remoteRepositories;

    protected Artifact resolve( String groupId, String artifactId, String version, String classifier, String type )
    {
        Artifact artifact =
            artifactFactory.createArtifactWithClassifier( groupId, artifactId, version, classifier, type );
        if ( !artifact.isResolved() )
        {
            ArtifactResolutionRequest req = new ArtifactResolutionRequest();
            req.setArtifact( artifact );
            req.setLocalRepository( localRepository );
            req.setRemoteRepositories( remoteRepositories );
            resolver.resolve( req );
        }
        return artifact;
    }

    /**
     * @component
     * @readonly
     */
    protected ArchiverManager archiverManager;

    public Integer getBenchmarkCompilerDetails()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Long getBenchmarkTimeFilter()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getFramework()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getEnableRuntimeDesignLayers()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public IExtensionsConfiguration getExtensionsConfiguration()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getGenerateAbstractSyntaxTree()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public List getLocalFontPaths()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean getQualifiedTypeSelectors()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
