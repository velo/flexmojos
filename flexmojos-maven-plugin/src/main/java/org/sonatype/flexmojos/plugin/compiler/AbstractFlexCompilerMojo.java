package org.sonatype.flexmojos.plugin.compiler;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.classifier;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.scope;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.type;
import static org.sonatype.flexmojos.plugin.common.FlexClassifier.CONFIGS;
import static org.sonatype.flexmojos.plugin.common.FlexClassifier.LINK_REPORT;
import static org.sonatype.flexmojos.plugin.common.FlexClassifier.SIZE_REPORT;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.CSS;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.RB_SWC;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWF;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWZ;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.XML;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.CACHING;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.COMPILE;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.EXTERNAL;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.INTERNAL;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.MERGED;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.RSL;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.THEME;
import static org.sonatype.flexmojos.util.PathUtil.files;
import static org.sonatype.flexmojos.util.PathUtil.pathsList;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Developer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.hamcrest.Matcher;
import org.sonatype.flexmojos.compatibilitykit.FlexCompatibility;
import org.sonatype.flexmojos.compatibilitykit.FlexMojo;
import org.sonatype.flexmojos.compiler.ICompcConfiguration;
import org.sonatype.flexmojos.compiler.ICompilerConfiguration;
import org.sonatype.flexmojos.compiler.IDefaultScriptLimits;
import org.sonatype.flexmojos.compiler.IDefaultSize;
import org.sonatype.flexmojos.compiler.IDefine;
import org.sonatype.flexmojos.compiler.IExtension;
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
import org.sonatype.flexmojos.compiler.IRuntimeSharedLibraryPath;
import org.sonatype.flexmojos.compiler.IRuntimeSharedLibrarySettingsConfiguration;
import org.sonatype.flexmojos.compiler.command.Result;
import org.sonatype.flexmojos.license.LicenseCalculator;
import org.sonatype.flexmojos.plugin.AbstractMavenMojo;
import org.sonatype.flexmojos.plugin.RuntimeMavenResolutionException;
import org.sonatype.flexmojos.plugin.compiler.attributes.MavenArtifact;
import org.sonatype.flexmojos.plugin.compiler.attributes.MavenDefaultScriptLimits;
import org.sonatype.flexmojos.plugin.compiler.attributes.MavenDefaultSize;
import org.sonatype.flexmojos.plugin.compiler.attributes.MavenExtension;
import org.sonatype.flexmojos.plugin.compiler.attributes.MavenFrame;
import org.sonatype.flexmojos.plugin.compiler.attributes.MavenMetadataConfiguration;
import org.sonatype.flexmojos.plugin.compiler.attributes.MavenNamespace;
import org.sonatype.flexmojos.plugin.compiler.attributes.MavenRuntimeException;
import org.sonatype.flexmojos.plugin.compiler.lazyload.Cacheable;
import org.sonatype.flexmojos.plugin.utilities.ConfigurationResolver;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;
import org.sonatype.flexmojos.util.PathUtil;

public abstract class AbstractFlexCompilerMojo<CFG, C extends AbstractFlexCompilerMojo<CFG, C>>
    extends AbstractMavenMojo
    implements ICompilerConfiguration, IFramesConfiguration, ILicensesConfiguration, IMetadataConfiguration,
    IFontsConfiguration, ILanguages, IMxmlConfiguration, INamespacesConfiguration, IExtensionsConfiguration, Cacheable,
    Cloneable, FlexMojo, IRuntimeSharedLibrarySettingsConfiguration
{

    private static final Object lock = new Object();

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
     * If true, a style manager will add style declarations to the local style manager without checking to see if the
     * parent already has the same style selector with the same properties. If false, a style manager will check the
     * parent to make sure a style with the same properties does not already exist before adding one locally.<BR>
     * If there is no local style manager created for this application, then don't check for duplicates. Just use the
     * old "selector exists" test.
     * <p>
     * Equivalent to -compiler.allow-duplicate-style-declaration
     * </p>
     * 
     * @parameter expression="${flex.allowDuplicateDefaultStyleDeclarations}"
     */
    private Boolean allowDuplicateDefaultStyleDeclarations;

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
     * DOCME undocumented by adobe
     * <p>
     * Equivalent to -benchmark-compiler-details
     * </p>
     * 0 = none, 1 = light, 5 = verbose
     * 
     * @parameter expression="${flex.benchmarkCompilerDetails}"
     */
    private Integer benchmarkCompilerDetails;

    /**
     * DOCME undocumented by adobe
     * <p>
     * Equivalent to -benchmark-time-filter
     * </p>
     * min time of units to log in ms
     * 
     * @parameter expression="${flex.benchmarkTimeFilter}"
     */
    private Long benchmarkTimeFilter;

    /**
     * Classifier to add to the artifact generated. If given, the artifact will be an attachment instead.
     * 
     * @parameter expression="${flex.classifier}"
     */
    protected String classifier;

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
     * @component
     * @readonly
     */
    protected org.sonatype.flexmojos.compiler.FlexCompiler compiler;

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
    private Map<String, String> compilerWarnings = new LinkedHashMap<String, String>();

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
    private List<String> compileSourceRoots;

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
     * Default value of resourceBundleList used when it is not defined
     * 
     * @parameter default-value= "${project.build.directory}/${project.build.finalName}-rb.properties"
     * @readonly
     */
    private File defaultResourceBundleList;

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
     * &lt;defines&gt;
     *   &lt;property&gt;
     *     &lt;name&gt;SOMETHING::aNumber&lt;/name&gt;
     *     &lt;value&gt;2.2&lt;/value&gt;
     *   &lt;/property&gt;
     *   &lt;property&gt;
     *     &lt;name&gt;SOMETHING::aString&lt;/name&gt;
     *     &lt;value&gt;&quot;text&quot;&lt;/value&gt;
     *   &lt;/property&gt;
     * &lt;/defines&gt;
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
    private boolean dumpConfigAttach;

    /**
     * DOCME undocumented by adobe
     * <p>
     * Equivalent to -compiler.enable-runtime-design-layers
     * </p>
     * 
     * @parameter expression="${flex.enableRuntimeDesignLayers}"
     */
    private Boolean enableRuntimeDesignLayers;

    /**
     * DOCME undocumented by adobe
     * <p>
     * Equivalent to -compiler.enable-swc-version-filtering
     * </p>
     * 
     * @parameter expression="${flex.enableSwcVersionFiltering}"
     */
    private Boolean enableSwcVersionFiltering;

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
     * Configure extensions to flex compiler
     * <p>
     * Equivalent to -compiler.extensions.extension
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;extensions&gt;
     *   &lt;extension&gt;
     *     &lt;extensionArtifact&gt;
     *       &lt;groupId&gt;org.myproject&lt;/groupId&gt;
     *       &lt;artifactId&gt;my-extension&lt;/artifactId&gt;
     *       &lt;version&gt;1.0&lt;/version&gt;
     *     &lt;/extensionArtifact&gt;
     *     &lt;parameters&gt;
     *       &lt;parameter&gt;param1&lt;/parameter&gt;
     *       &lt;parameter&gt;param2&lt;/parameter&gt;
     *       &lt;parameter&gt;param3&lt;/parameter&gt;
     *     &lt;/parameters&gt;
     *   &lt;/extension&gt;
     * &lt;/extensions&gt;
     * </pre>
     * 
     * @parameter
     */
    private MavenExtension[] extensions;

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
     * The name of the compiled file
     * 
     * @parameter default-name="${project.build.finalName}" expression="${flex.finalName}"
     */
    protected String finalName;

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
     * Force an RSL to be loaded, overriding the removal caused by using the remove-unused-rsls option.
     * <p>
     * Equivalent to -runtime-shared-library-settings.force-rsls
     * </p>
     * 
     * @parameter
     */
    private String[] forceRsls;

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
    private MavenFrame[] frames;

    /**
     * DOCME undocumented by adobe
     * <p>
     * Equivalent to -framework
     * </p>
     * 
     * @parameter expression="${flex.framework}"
     */
    private String framework;

    /**
     * When false (faster) Flexmojos will compiler modules and resource bundles using multiple threads (One per SWF). If
     * true, Thread.join() will be invoked to make the execution synchronous (sequential).
     * 
     * @parameter expression="${flex.fullSynchronization}" default-value="false"
     */
    protected boolean fullSynchronization;

    /**
     * DOCME undocumented by adobe
     * <p>
     * Equivalent to -compiler.generate-abstract-syntax-tree
     * </p>
     * 
     * @parameter expression="${flex.generateAbstractSyntaxTree}"
     */
    private Boolean generateAbstractSyntaxTree;

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
     * EXTREMELLY UN-ADVISIBLE. When true, flexmojos will check if the compiler and the framework versions match.
     * Usually, you must use the same compiler and framework versions. Set this to true to avoid this check. EXTREMELLY
     * UN-ADVISIBLE.
     * 
     * @parameter default-value="false" expression="${flex.ignoreVersionIssues}"
     */
    private boolean ignoreVersionIssues;

    /**
     * Only include inheritance dependencies of classes specified with include-classes.
     * <p>
     * Equivalent to -include-inheritance-dependencies-only
     * </p>
     * 
     * @parameter expression="${flex.includeInheritanceDependenciesOnly}"
     */
    private Boolean includeInheritanceDependenciesOnly;

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
    protected List<String> includeResourceBundles;

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
     * Enables the compiled application or module to set styles that only affect itself and its children.<BR>
     * Allow the user to decide if the compiled application/module should have its own style manager
     * <p>
     * Equivalent to -compiler.isolate-styles
     * </p>
     * 
     * @parameter expression="${flex.isolateStyles}"
     */
    private Boolean isolateStyles;

    /**
     * DOCME undocumented by adobe
     * <p>
     * Equivalent to -compiler.java-profiler-class
     * </p>
     * 
     * @parameter expression="${flex.javaProfilerClass}"
     */
    private String javaProfilerClass;

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
     * @component
     */
    private LicenseCalculator licenseCalculator;

    /**
     * When true flexmojos will automatically lookup for licenses folling this documentation
     * http://livedocs.adobe.com/flex/3/html/help.html?content=05B_Security_03 .html#140756
     * 
     * @parameter default-value="true" expression="${flex.licenseLocalLookup}"
     */
    private boolean licenseLocalLookup;

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
     * When true the link report will be attached to maven reactor
     * 
     * @parameter expression="${flex.linkReportAttach}"
     */
    private boolean linkReportAttach;

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
     * Sets a list of artifacts to omit from linking when building an application. This is equivalent to using the
     * <code>load-externs</code> option of the mxmlc or compc compilers.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;loadExterns&gt;
     *   &lt;loadExtern&gt;
     *     &lt;groupId&gt;com.acme&lt;/groupId&gt;
     *     &lt;artifactId&gt;flexmodule&lt;/artifactId&gt;
     *     &lt;version&gt;1.0.0&lt;/version&gt;
     *   &lt;/loadExtern&gt;
     *   &lt;loadExtern&gt;
     *     &lt;groupId&gt;org.tabajara&lt;/groupId&gt;
     *     &lt;artifactId&gt;flexmodule&lt;/artifactId&gt;
     *     &lt;version&gt;1.0.0&lt;/version&gt;
     *   &lt;/loadExtern&gt;
     * &lt;/loadExterns&gt;
     * </pre>
     * 
     * @deprecated use dependency with type "xml" and classifier "link-report"
     * @parameter
     */
    protected MavenArtifact[] loadExterns;

    /**
     * Specifies the locale for internationalization
     * <p>
     * Equivalent to -compiler.locale
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;localesCompiled&gt;
     *   &lt;locale&gt;en_US&lt;/locale&gt;
     * &lt;/localesCompiled&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] localesCompiled;

    /**
     * Relative path where the locales should be created
     * 
     * @parameter expression="${flex.localesOutputPath}"
     */
    private String localesOutputPath;

    /**
     * Specifies the locales for external internationalization bundles
     * <p>
     * No equivalent parameter
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;localesRuntime&gt;
     *   &lt;locale&gt;en_US&lt;/locale&gt;
     * &lt;/localesRuntime&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] localesRuntime;

    /**
     * Define the base path to locate resouce bundle files Accept some special tokens:
     * 
     * <pre>
     * {locale}     - replace by locale name
     * </pre>
     * 
     * @parameter default-value="${basedir}/src/main/locales/{locale}"
     */
    protected File localesSourcePath;

    /**
     * DOCME undocumented by adobe
     * <p>
     * Equivalent to -compiler.fonts.local-font-paths
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;localFontPaths&gt;
     *   &lt;localFontPath&gt;???&lt;/localFontPath&gt;
     *   &lt;localFontPath&gt;???&lt;/localFontPath&gt;
     * &lt;/localFontPaths&gt;
     * </pre>
     * 
     * @parameter
     */
    private File[] localFontPaths;

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
     * Minimum supported SDK version for this library. This string will always be of the form N.N.N. For example, if
     * -minimum-supported-version=2, this string is "2.0.0", not "2".
     * <p>
     * Equivalent to -compiler.mxml.minimum-supported-version
     * </p>
     * 
     * @parameter expression="${flex.minimumSupportedVersion}"
     */
    private String minimumSupportedVersion;

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
    private MavenNamespace[] namespaces;

    /**
     * Toggle whether trace statements are omitted
     * <p>
     * Equivalent to -compiler.omit-trace-statements
     * </p>
     * 
     * @parameter expression="${flex.omitTraceStatements}"
     */
    private Boolean omitTraceStatements;

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
     * policyFileUrls array of policy file URLs. Each entry in the rslUrls array must have a corresponding entry in this
     * array. A policy file may be needed in order to allow the player to read an RSL from another domain. If a policy
     * file is not required, then set it to an empty string. Accept some special tokens:
     * 
     * <pre>
     * {contextRoot}        - replace by defined context root
     * {groupId}            - replace by library groupId
     * {artifactId}         - replace by library artifactId
     * {version}            - replace by library version
     * {extension}          - replace by library extension swf or swz
     * </pre>
     * 
     * <BR>
     * Usage:
     * 
     * <pre>
     * &lt;policyFileUrls&gt;
     *   &lt;url&gt;/{contextRoot}/rsl/policy-{artifactId}-{version}.xml&lt;/url&gt;
     * &lt;/policyFileUrls&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] policyFileUrls;

    /**
     * Specifies the default value for the Application's preloader attribute. If not specified, the default preloader
     * value will be mx.preloaders.SparkDownloadProgressBar with -compatibility-version >= 4.0 and it will be
     * mx.preloader.DownloadProgressBar with -compatibility-version < 4.0.
     * <p>
     * Equivalent to -compiler.preloader
     * </p>
     * 
     * @parameter expression="${flex.preloader}"
     */
    private String preloader;

    /**
     * DOCME undocumented by adobe
     * <p>
     * Equivalent to -compiler.mxml.qualified-type-selectors
     * </p>
     * 
     * @parameter expression="${flex.qualifiedTypeSelectors}"
     */
    private Boolean qualifiedTypeSelectors;

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
     * Remove RSLs that are not being used by the application.
     * <p>
     * Equivalent to -remove-unused-rsls
     * </p>
     * 
     * @parameter expression="${flex.removeUnusedRsls}"
     */
    private Boolean removeUnusedRsls;

    /**
     * Use this option to generate a warning instead of an error when a missing required skin part is detected.
     * <p>
     * Equivalent to -compiler.report-missing-required-skin-parts-as-warnings
     * </p>
     * 
     * @parameter expression="${flex.reportMissingRequiredSkinPartsAsWarnings}"
     */
    private Boolean reportMissingRequiredSkinPartsAsWarnings;

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
     * rslUrls array of URLs. The first RSL URL in the list is the primary RSL. The remaining RSL URLs will only be
     * loaded if the primary RSL fails to load. Accept some special tokens:
     * 
     * <pre>
     * {contextRoot}        - replace by defined context root
     * {groupId}            - replace by library groupId
     * {artifactId}         - replace by library artifactId
     * {version}            - replace by library version
     * {extension}          - replace by library extension swf or swz
     * {classifier}         - replace by library classifier swf or swz
     * {hard-version}       - replace by library timestamped version (for -SNAPSHOT artifacts only and if timestamped is available)
     * </pre>
     * 
     * default-value="/{contextRoot}/rsl/{artifactId}-{version}.{extension}" <BR>
     * Usage:
     * 
     * <pre>
     * &lt;rslUrls&gt;
     *   &lt;url&gt;/{contextRoot}/rsl/{artifactId}-{classifier}-{version}.{extension}&lt;/url&gt;
     * &lt;/rslUrls&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] rslUrls;

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
     * Toggle the display of warnings
     * <p>
     * Equivalent to -warnings
     * </p>
     * 
     * @parameter expression="${flex.showWarnings}"
     */
    private Boolean showWarnings;

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
     * When true the size report will be attached to maven reactor
     * 
     * @parameter expression="${flex.sizeReportAttach}"
     */
    private boolean sizeReportAttach;

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
    private String[] themes;

    /**
     * Configures the LocalizationManager's locale, which is used when reporting compile time errors, warnings, and
     * info. For example, "en" or "ja_JP".
     * <p>
     * Equivalent to -tools-locale
     * </p>
     * 
     * @parameter expression="${flex.toolsLocale}" default-value="en_US"
     */
    protected String toolsLocale;

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
     * Toggle whether the SWF is flagged for access to network resources
     * <p>
     * Equivalent to -use-network
     * </p>
     * 
     * @parameter expression="${flex.useNetwork}"
     */
    private Boolean useNetwork;

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
     * Verifies the libraries loaded at runtime are the correct ones
     * <p>
     * Equivalent to -verify-digests
     * </p>
     * 
     * @parameter expression="${flex.verifyDigests}"
     */
    private Boolean verifyDigests;

    protected Artifact adaptResourceBundle( final Artifact baseRbSwc, String requestedLocale )
    {
        getLog().debug( "Adapting resource bundle " + baseRbSwc.getArtifactId() + ":" + baseRbSwc.getClassifier()
                            + " to " + requestedLocale );

        Artifact rbSwc;
        try
        {
            rbSwc =
                resolve( baseRbSwc.getGroupId(), baseRbSwc.getArtifactId(), baseRbSwc.getVersion(),
                         baseRbSwc.getClassifier() + "2" + requestedLocale, baseRbSwc.getType() );
        }
        catch ( RuntimeMavenResolutionException e )
        {
            rbSwc = e.getArtifact();
        }

        if ( rbSwc.isResolved() )
        {
            return rbSwc;
        }

        File dest;
        try
        {
            UnArchiver unzip = archiverManager.getUnArchiver( "zip" );
            unzip.setSourceFile( baseRbSwc.getFile() );
            dest = FileUtils.createTempFile( baseRbSwc.getArtifactId(), requestedLocale, getOutputDirectory() );
            unzip.extract( "locale/" + baseRbSwc.getClassifier(), dest );
        }
        catch ( Exception e )
        {
            throw new MavenRuntimeException( "Unable to extract base locale", e );
        }

        File resourceBundleBaseDir = new File( dest, "locale/" + baseRbSwc.getClassifier() );
        List<String> bundles = new ArrayList<String>();
        for ( String bundle : resourceBundleBaseDir.list() )
        {
            bundles.add( bundle.replace( ".properties", "" ) );
        }

        ICompcConfiguration cfg = mock( ICompcConfiguration.class, RETURNS_NULL );
        when( cfg.getLoadConfig() ).thenReturn( getLoadConfig() );
        when( cfg.getIncludeResourceBundles() ).thenReturn( bundles );
        String output = PathUtil.path( baseRbSwc.getFile() ).replace( baseRbSwc.getClassifier(), rbSwc.getClassifier() );
        when( cfg.getOutput() ).thenReturn( output );

        ICompilerConfiguration compilerCfg = mock( ICompilerConfiguration.class, RETURNS_NULL );
        when( compilerCfg.getTheme() ).thenReturn( Collections.EMPTY_LIST );
        when( compilerCfg.getFontsConfiguration() ).thenReturn( getFontsConfiguration() );
        when( compilerCfg.getLocale() ).thenReturn( new String[] { requestedLocale } );
        when( compilerCfg.getSourcePath() ).thenReturn( new File[] { resourceBundleBaseDir } );
        when( compilerCfg.getExternalLibraryPath() ).thenReturn( this.getExternalLibraryPath() );
        when( compilerCfg.getLibraryPath() ).thenReturn( this.getLibraryPath( false ) );

        when( cfg.getCompilerConfiguration() ).thenReturn( compilerCfg );

        try
        {
            checkResult( compiler.compileSwc( cfg, true ) );
        }
        catch ( Exception e )
        {
            throw new MavenRuntimeException( "Unable to compile adapted resource bundle", e );
        }

        rbSwc.setFile( new File( output ) );
        rbSwc.setResolved( true );
        return rbSwc;
    }

    protected Map<String, String> calculateRuntimeLibraryPath( Artifact artifact, String[] rslUrls,
                                                               String[] policyFileUrls )
    {
        getLog().debug( "runtime libraries: id: " + artifact.getArtifactId() );

        String scope = artifact.getScope();
        final String extension;
        if ( CACHING.equals( scope ) )
        {
            extension = SWZ;
        }
        else
        {
            extension = SWF;
        }

        Map<String, String> paths = new LinkedHashMap<String, String>();
        for ( int i = 0; i < rslUrls.length; i++ )
        {
            String rsl = rslUrls[i];
            String policy;
            if ( i < policyFileUrls.length )
            {
                policy = policyFileUrls[i];
            }
            else
            {
                policy = null;
            }

            rsl = MavenUtils.interpolateRslUrl( rsl, artifact, extension, contextRoot );
            policy = MavenUtils.interpolateRslUrl( policy, artifact, extension, contextRoot );

            getLog().debug( "RSL url: " + rsl + " - " + policy );
            paths.put( rsl, policy );
        }

        return paths;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public C clone()
    {
        try
        {
            C clone = (C) super.clone();
            clone.cache = new LinkedHashMap<String, Object>();
            return clone;
        }
        catch ( CloneNotSupportedException e )
        {
            throw new IllegalStateException( "The class '" + getClass() + "' is supposed to be clonable", e );
        }
    }

    @SuppressWarnings( "unchecked" )
    protected void configureResourceBundle( String locale, AbstractFlexCompilerMojo<?, ?> cfg )
    {
        cfg.localesCompiled = new String[] { locale };
        cfg.localesRuntime = null;
        if ( locale.contains( "," ) )
        {
            cfg.classifier = locale.split( "," )[0];
        }
        else
        {
            cfg.classifier = locale;
        }

        cfg.includeResourceBundles = getResourceBundleListContent();
        cfg.getCache().put( "getExternalLibraryPath", MavenUtils.getFiles( getDependencies( type( SWC ) ) ) );
        cfg.getCache().put( "getLibraryPath", MavenUtils.getFiles( cfg.getCompiledResouceBundles() ) );

        if ( localesOutputPath != null )
        {
            cfg.getCache().put( "getTargetDirectory", new File( getTargetDirectory(), localesOutputPath ) );
        }
    }

    @FlexCompatibility( minVersion = "4.0.0.11420" )
    private void configureThemeSparkCss( List<File> themes )
    {
        File dir = getUnpackedFrameworkConfig();

        File sparkCss = new File( dir, "themes/Spark/spark.css" );

        if ( !sparkCss.exists() )
        {
            sparkCss = new File( getOutputDirectory(), "spark.css" );
            sparkCss.getParentFile().mkdirs();
            try
            {
                FileUtils.copyURLToFile( MavenUtils.class.getResource( "/themes/spark.css" ), sparkCss );
            }
            catch ( IOException e )
            {
                throw new MavenRuntimeException( "Error copying spark.css file.", e );
            }
        }

        themes.add( sparkCss );
    }

    @FlexCompatibility( minVersion = "4.0.0.11420" )
    private void configureThemeHaloSwc( List<File> themes )
    {
        File dir = getUnpackedFrameworkConfig();

        File haloSwc = new File( dir, "themes/Halo/halo.swc" );

        if ( !haloSwc.exists() )
        {
            haloSwc = new File( getOutputDirectory(), "halo.swc" );
            haloSwc.getParentFile().mkdirs();
            try
            {
                FileUtils.copyURLToFile( MavenUtils.class.getResource( "/themes/halo.swc" ), haloSwc );
            }
            catch ( IOException e )
            {
                throw new MavenRuntimeException( "Error copying halo.swc file.", e );
            }
        }

        themes.add( haloSwc );
    }

    public abstract Result doCompile( CFG cfg, boolean synchronize )
        throws Exception;

    private Artifact doLocalizationChain( String[] locales, String requestedLocale, Artifact beacon,
                                          Artifact requestRbSwc )
    {
        getLog().info( "Resolving resource bundle for '" + beacon + "' using localization chain." );

        for ( String locale : locales )
        {
            Artifact rbSwc;
            try
            {
                rbSwc =
                    resolve( beacon.getGroupId(), beacon.getArtifactId(), beacon.getVersion(), locale, beacon.getType() );
            }
            catch ( RuntimeMavenResolutionException e )
            {
                rbSwc = e.getArtifact();
            }

            if ( rbSwc.isResolved() )
            {
                if ( !requestedLocale.equals( locale ) )
                {
                    getLog().info( "Resolved resource bundle for '" + beacon + "' using localization chain. The '"
                                       + locale + "' will be used to build the missing '" + requestedLocale + "'" );
                    return adaptResourceBundle( rbSwc, requestedLocale );
                }

                return rbSwc;
            }
        }

        throw new MavenRuntimeException( "Unable to resolve resource bundle '" + beacon + "' for '" + requestedLocale
            + "'" );
    }

    protected Result executeCompiler( CFG cfg, boolean synchronize )
        throws MojoExecutionException, MojoFailureException
    {
        Result result;
        try
        {
            result = doCompile( cfg, synchronize );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }

        if ( synchronize )
        {
            checkResult( result );
        }

        return result;
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

    public Boolean getAdvancedAntiAliasing()
    {
        return advancedAntiAliasing;
    }

    public Boolean getAllowDuplicateDefaultStyleDeclarations()
    {
        return allowDuplicateDefaultStyleDeclarations;
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

    public Integer getBenchmarkCompilerDetails()
    {
        if ( benchmarkCompilerDetails == null )
        {
            return null;
        }

        if ( benchmarkCompilerDetails != 0 && benchmarkCompilerDetails != 1 && benchmarkCompilerDetails != 5 )
        {
            throw new IllegalArgumentException( "Invalid benchmarck compiler details level: '"
                + benchmarkCompilerDetails + "', it does accept 0 = none, 1 = light, 5 = verbose" );
        }

        return benchmarkCompilerDetails;
    }

    public Long getBenchmarkTimeFilter()
    {
        return benchmarkTimeFilter;
    }

    public String getClassifier()
    {
        return classifier;
    }

    public String getCompatibilityVersion()
    {
        if ( compatibilityVersion == null )
        {
            return null;
        }

        String[] versionStringParts = compatibilityVersion.split( "\\." );
        if ( versionStringParts.length != 3 )
        {
            throw new MavenRuntimeException( "compatibilityVersion (" + compatibilityVersion
                + ") isn't in the required <major>.<minor>.<revision> pattern." );
        }
        else
        {
            try
            {
                for ( int i = 0; i < 3; i++ )
                {
                    Integer.parseInt( versionStringParts[i] );
                }
            }
            catch ( NumberFormatException e )
            {
                throw new MavenRuntimeException( "compatibilityVersion contained a non-numeric segment", e );
            }
        }
        return compatibilityVersion;
    }

    @SuppressWarnings( "unchecked" )
    protected Collection<Artifact> getCompiledResouceBundles()
    {
        if ( this.getLocale() == null )
        {
            return null;
        }

        Collection<Artifact> rbsSwc = new LinkedHashSet<Artifact>();

        Set<Artifact> beacons = getDependencies( type( RB_SWC ) );

        String[] localeChains = this.localesCompiled;
        if ( localeChains == null )
        {
            localeChains = getLocale();
        }

        // TODO for for for for if for for, too many nested blocks, improve this
        for ( Artifact beacon : beacons )
        {
            for ( String localeChain : localeChains )
            {
                String[] locales;
                if ( localeChain.contains( "," ) )
                {
                    locales = localeChain.split( "," );
                }
                else
                {
                    locales = new String[] { localeChain };
                }

                String requestedLocale = locales[0];

                Artifact requestedRbSwc;
                try
                {
                    requestedRbSwc =
                        resolve( beacon.getGroupId(), beacon.getArtifactId(), beacon.getVersion(), requestedLocale,
                                 beacon.getType() );
                }
                catch ( RuntimeMavenResolutionException e )
                {
                    requestedRbSwc = e.getArtifact();
                }

                Artifact resultRbSwc;
                if ( requestedRbSwc.isResolved() )
                {
                    resultRbSwc = requestedRbSwc;
                }
                else if ( locales.length > 1 )
                {
                    resultRbSwc = doLocalizationChain( locales, requestedLocale, beacon, requestedRbSwc );
                }
                else
                {
                    throw new MavenRuntimeException( "Missing resource bundle '" + requestedRbSwc + "'" );
                }

                rbsSwc.add( resultRbSwc );
            }
        }

        return rbsSwc;
    }

    public ICompilerConfiguration getCompilerConfiguration()
    {
        return this;
    }

    protected File getCompilerOutput()
    {
        File output = new File( getTargetDirectory(), getFinalName() + "." + getProjectType() );
        output.getParentFile().mkdirs();
        return output;
    }

    public Map<String, Boolean> getCompilerWarnings()
    {
        // converts the <String, String> map into a <String, Boolean> one
        Map<String, Boolean> compilerWarnings = new LinkedHashMap<String, Boolean>();

        Set<Entry<String, String>> warns = this.compilerWarnings.entrySet();
        for ( Entry<String, String> entry : warns )
        {
            compilerWarnings.put( entry.getKey(), Boolean.valueOf( entry.getValue() ) );
        }

        return compilerWarnings;
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
        if ( this.metadata != null && this.metadata.getContributor() != null )
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
        if ( this.metadata != null && this.metadata.getCreator() != null )
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
        return PathUtil.pathsList( defaultsCssFiles );
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
        File dumpConfig = new File( getTargetDirectory(), getFinalName() + "-" + CONFIGS + "." + XML );

        if ( dumpConfigAttach )
        {
            if ( getClassifier() != null )
            {
                getLog().warn( "Config dump is not attached for artifacts with classifier" );
            }
            else
            {
                projectHelper.attachArtifact( project, XML, CONFIGS, dumpConfig );
            }
        }
        return PathUtil.path( dumpConfig );
    }

    public Boolean getEnableRuntimeDesignLayers()
    {
        return enableRuntimeDesignLayers;
    }

    public Boolean getEnableSwcVersionFiltering()
    {
        return enableSwcVersionFiltering;
    }

    public Boolean getEs()
    {
        return es;
    }

    public IExtension[] getExtension()
    {
        if ( extensions == null )
        {
            return null;
        }

        IExtension[] extensions = new IExtension[this.extensions.length];
        for ( int i = 0; i < extensions.length; i++ )
        {
            final MavenExtension extension = this.extensions[i];

            if ( extension.getExtensionArtifact() == null )
            {
                throw new IllegalArgumentException( "Extension artifact is required!" );
            }

            extensions[i] = new IExtension()
            {
                public File extension()
                {
                    MavenArtifact a = extension.getExtensionArtifact();
                    Artifact resolvedArtifact =
                        resolve( a.getGroupId(), a.getArtifactId(), a.getVersion(), a.getClassifier(), a.getType() );
                    return resolvedArtifact.getFile();
                }

                public String[] parameters()
                {
                    return extension.getParameters();
                }
            };
        }

        return extensions;
    }

    public IExtensionsConfiguration getExtensionsConfiguration()
    {
        return this;
    }

    @SuppressWarnings( "unchecked" )
    public File[] getExternalLibraryPath()
    {
        if ( SWC.equals( getProjectType() ) )
        {
            Matcher<? extends Artifact> swcs =
                allOf( type( SWC ), //
                       anyOf( scope( EXTERNAL ), scope( CACHING ), scope( RSL ), scope( COMPILE ),
                              scope( nullValue( String.class ) ) )//
                );
            return MavenUtils.getFiles( getDependencies( swcs, not( GLOBAL_MATCHER ) ), getGlobalArtifact() );
        }
        else
        {
            return MavenUtils.getFiles( getDependencies( not( GLOBAL_MATCHER ),//
                                                         allOf( type( SWC ),//
                                                                anyOf( scope( EXTERNAL ), scope( CACHING ), scope( RSL ) ) ) ),
                                        getGlobalArtifact() );
        }
    }

    public List<String> getExterns()
    {
        if ( externs == null )
        {
            return null;
        }

        return Arrays.asList( externs );
    }

    public String getFinalName()
    {
        if ( finalName == null )
        {
            String c = getClassifier() == null ? "" : "-" + getClassifier();
            return project.getBuild().getFinalName() + c;
        }

        return finalName;
    }

    public Boolean getFlashType()
    {
        return flashType;
    }

    public IFontsConfiguration getFontsConfiguration()
    {
        return this;
    }

    public String[] getForceRsls()
    {
        return forceRsls;
    }

    public IFrame[] getFrame()
    {
        return frames;
    }

    public IFramesConfiguration getFramesConfiguration()
    {
        return this;
    }

    public String getFramework()
    {
        return framework;
    }

    public Boolean getGenerateAbstractSyntaxTree()
    {
        return generateAbstractSyntaxTree;
    }

    public Boolean getGenerateFrameLoader()
    {
        return generateFrameLoader;
    }

    @SuppressWarnings( "unchecked" )
    public Collection<Artifact> getGlobalArtifact()
    {
        synchronized ( lock )
        {
            Artifact global = getDependency( GLOBAL_MATCHER );
            if ( global == null )
            {
                throw new IllegalArgumentException(
                                                    "Global artifact is not available. Make sure to add 'playerglobal' or 'airglobal' to this project." );
            }

            File source = global.getFile();
            File dest =
                new File( source.getParentFile(), global.getClassifier() + "/" + global.getArtifactId() + "." + SWC );
            global.setFile( dest );

            try
            {
                if ( !dest.exists() )
                {
                    dest.getParentFile().mkdirs();
                    getLog().debug( "Striping global artifact, source: " + source + ", dest: " + dest );
                    FileUtils.copyFile( source, dest );
                }
            }
            catch ( IOException e )
            {
                throw new IllegalStateException( "Error renamming '" + global.getArtifactId() + "'.", e );
            }

            return Collections.singletonList( global );
        }
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

    public Boolean getIncludeInheritanceDependenciesOnly()
    {
        return includeInheritanceDependenciesOnly;
    }

    @SuppressWarnings( "unchecked" )
    public File[] getIncludeLibraries()
    {
        return MavenUtils.getFiles( getDependencies( type( SWC ), scope( INTERNAL ), not( GLOBAL_MATCHER ) ) );
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

    public Boolean getIsolateStyles()
    {
        return isolateStyles;
    }

    public String getJavaProfilerClass()
    {
        return javaProfilerClass;
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
        if ( this.metadata != null && this.metadata.getLanguage() != null )
        {
            return this.metadata.getLanguage();
        }

        if ( getLocale() == null || getLocale().length == 0 )
        {
            return null;
        }

        return getLocale();
    }

    public ILanguageRange[] getLanguageRange()
    {
        if ( languageRange == null )
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

    public File[] getLibraryPath()
    {
        return getLibraryPath( true );
    }

    @SuppressWarnings( "unchecked" )
    private File[] getLibraryPath( boolean includeResourceBundle )
    {
        Collection<Artifact> resourceBundle =
            includeResourceBundle ? getCompiledResouceBundles() : Collections.EMPTY_LIST;
        if ( SWC.equals( getProjectType() ) )
        {
            return MavenUtils.getFiles( getDependencies( type( SWC ), scope( MERGED ), not( GLOBAL_MATCHER ) ),
                                        resourceBundle );
        }
        else
        {
            return MavenUtils.getFiles( getDependencies( type( SWC ),//
                                                         anyOf( scope( MERGED ), scope( COMPILE ),
                                                                scope( nullValue( String.class ) ) ),//
                                                         not( GLOBAL_MATCHER ) ),//
                                        resourceBundle );
        }
    }

    public ILicense[] getLicense()
    {
        try
        {
            Class.forName( "flex.license.License" );
        }
        catch ( ClassNotFoundException e )
        {
            getLog().warn( "Unable to find license.jar on plugin classpath.  No license will be added.  Check wiki for instructions about how to add it:\n\t"
                               + "https://docs.sonatype.org/display/FLEXMOJOS/FAQ#FAQ-1.3" );
            return null;
        }

        Map<String, String> licenses = new LinkedHashMap<String, String>();

        if ( licenseLocalLookup )
        {
            licenses.putAll( licenseCalculator.getInstalledLicenses() );
        }

        if ( this.licenses != null )
        {
            licenses.putAll( this.licenses );
        }

        if ( licenses.isEmpty() )
        {
            return null;
        }

        Set<Entry<String, String>> entries = licenses.entrySet();
        List<ILicense> keys = new ArrayList<ILicense>();
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
        File linkReport = new File( getTargetDirectory(), getFinalName() + "-" + LINK_REPORT + "." + XML );

        if ( linkReportAttach )
        {
            if ( getClassifier() != null )
            {
                getLog().warn( "Link report is not attached for artifacts with classifier" );
            }
            else
            {
                projectHelper.attachArtifact( project, XML, LINK_REPORT, linkReport );
            }
        }
        return PathUtil.path( linkReport );
    }

    public String[] getLoadConfig()
    {
        return PathUtil.paths( ConfigurationResolver.resolveConfiguration( loadConfigs, loadConfig, configDirectory ) );
    }

    @SuppressWarnings( { "unchecked", "deprecation" } )
    public String[] getLoadExterns()
    {
        Collection<Artifact> artifacts = new LinkedHashSet<Artifact>();

        Set<Artifact> dependencies = getDependencies( classifier( LINK_REPORT ), type( XML ) );
        if ( !dependencies.isEmpty() )
        {
            artifacts.addAll( dependencies );
        }

        if ( loadExterns != null )
        {
            for ( MavenArtifact loadExtern : loadExterns )
            {
                Artifact resolvedArtifact =
                    resolve( loadExtern.getGroupId(), loadExtern.getArtifactId(), loadExtern.getVersion(), LINK_REPORT,
                             XML );
                artifacts.add( resolvedArtifact );
            }

        }

        if ( artifacts.isEmpty() )
        {
            return null;
        }

        return PathUtil.paths( MavenUtils.getFilesSet( artifacts ) );
    }

    public String[] getLocale()
    {
        if ( localesCompiled != null )
        {
            String[] locales = new String[localesCompiled.length];
            for ( int i = 0; i < localesCompiled.length; i++ )
            {
                String locale = localesCompiled[i];
                if ( locale.contains( "," ) )
                {
                    locale = locale.split( "," )[0];
                }
                locales[i] = locale;
            }
            return locales;
        }

        // if there are runtime locales, no need for compiled locales
        if ( getLocalesRuntime() != null )
        {
            return new String[] {};
        }

        return null;
    }

    public String[] getLocalesRuntime()
    {
        if ( localesRuntime == null )
        {
            return null;
        }

        try
        {
            File rbBeacon = new File( getTargetDirectory(), getFinalName() + "." + RB_SWC );
            FileUtils.copyURLToFile( getClass().getResource( "/rb.swc" ), rbBeacon );
            getLog().info( "Installing resource bundle beacon: " + rbBeacon );
            projectHelper.attachArtifact( project, RB_SWC, rbBeacon );
        }
        catch ( IOException e )
        {
            throw new MavenRuntimeException( "Failed to create beacon resource bundle", e );
        }

        return localesRuntime;
    }

    public List<String> getLocalFontPaths()
    {
        return PathUtil.pathsList( localFontPaths );
    }

    public String getLocalFontsSnapshot()
    {
        if ( localFontsSnapshot != null )
        {
            return PathUtil.path( localFontsSnapshot );
        }

        URL url;
        if ( MavenUtils.isMac() )
        {
            url = getClass().getResource( "/fonts/macFonts.ser" );
        }
        else if ( MavenUtils.isWindows() )
        {
            url = getClass().getResource( "/fonts/winFonts.ser" );
        }
        else
        {
            url = getClass().getResource( "/fonts/localFonts.ser" );
        }

        File fontsSer = new File( getOutputDirectory(), "fonts.ser" );
        try
        {
            FileUtils.copyURLToFile( url, fontsSer );
        }
        catch ( IOException e )
        {
            throw new IllegalStateException( "Error copying fonts file.", e );
        }
        return PathUtil.path( fontsSer );
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

    public String getMinimumSupportedVersion()
    {
        return this.minimumSupportedVersion;
    }

    public IMxmlConfiguration getMxmlConfiguration()
    {
        return this;
    }

    public INamespace[] getNamespace()
    {
        List<INamespace> namespaces = new ArrayList<INamespace>();
        if ( this.namespaces != null )
        {
            namespaces.addAll( Arrays.asList( this.namespaces ) );
        }

        File dir = getUnpackedFrameworkConfig();

        if ( dir == null )
        {
            return this.namespaces;
        }

        Reader cfg = null;
        try
        {
            cfg = new FileReader( new File( dir, "flex-config.xml" ) );

            Xpp3Dom dom = Xpp3DomBuilder.build( cfg );

            dom = dom.getChild( "compiler" );

            dom = dom.getChild( "namespaces" );

            Xpp3Dom[] defaultNamespaces = dom.getChildren();
            for ( Xpp3Dom xpp3Dom : defaultNamespaces )
            {
                String uri = xpp3Dom.getChild( "uri" ).getValue();
                String manifestName = xpp3Dom.getChild( "manifest" ).getValue();
                File manifest = new File( dir, manifestName );

                namespaces.add( new MavenNamespace( uri, manifest ) );
            }
        }
        catch ( Exception e )
        {
            throw new MavenRuntimeException( "Unable to retrieve flex default namespaces!", e );
        }
        finally
        {
            IOUtil.close( cfg );
        }

        return namespaces.toArray( new INamespace[0] );
    }

    public INamespacesConfiguration getNamespacesConfiguration()
    {
        return this;
    }

    protected List<String> getNamespacesUri()
    {
        if ( namespaces == null || namespaces.length == 0 )
        {
            return null;
        }

        List<String> uris = new ArrayList<String>();
        for ( INamespace namespace : namespaces )
        {
            uris.add( namespace.uri() );
        }

        return uris;
    }

    public Boolean getOmitTraceStatements()
    {
        return omitTraceStatements;
    }

    public Boolean getOptimize()
    {
        return optimize;
    }

    public String getOutput()
    {
        File output = getCompilerOutput();
        if ( getClassifier() != null )
        {
            projectHelper.attachArtifact( project, getProjectType(), getClassifier(), output );
        }
        else if ( !getProjectType().equals( packaging ) )
        {
            projectHelper.attachArtifact( project, getProjectType(), output );
        }
        else
        {
            project.getArtifact().setFile( output );
        }

        return PathUtil.path( output );
    }

    public String[] getPolicyFileUrls()
    {
        if ( policyFileUrls == null )
        {
            return new String[0];
        }
        return policyFileUrls;
    }

    public String getPreloader()
    {
        return preloader;
    }

    public String getProjectType()
    {
        return packaging;
    }

    public String[] getPublisher()
    {
        if ( this.metadata != null )
        {
            return this.metadata.getPublisher();
        }

        return getCreator();
    }

    public Boolean getQualifiedTypeSelectors()
    {
        return qualifiedTypeSelectors;
    }

    public String getRawMetadata()
    {
        return rawMetadata;
    }

    public Boolean getRemoveUnusedRsls()
    {
        return removeUnusedRsls;
    }

    public Boolean getReportInvalidStylesAsWarnings()
    {
        return getCompilerWarnings().get( "report-invalid-styles-as-warnings" );
    }

    public Boolean getReportMissingRequiredSkinPartsAsWarnings()
    {
        return reportMissingRequiredSkinPartsAsWarnings;
    }

    public String getResourceBundleList()
    {
        return PathUtil.path( getResourceBundleListFile() );
    }

    protected List<String> getResourceBundleListContent()
    {
        if ( !getResourceBundleListFile().exists() )
        {
            return null;
        }

        String bundles;
        try
        {
            bundles = FileUtils.fileRead( getResourceBundleListFile() );
        }
        catch ( IOException e )
        {
            throw new MavenRuntimeException( e );
        }

        return Arrays.asList( bundles.substring( 10 ).split( " " ) );
    }

    /**
     * File content sample:
     * 
     * <pre>
     * bundles = containers core effects skins styles
     * </pre>
     * 
     * @return bundle list file
     */
    protected File getResourceBundleListFile()
    {
        if ( resourceBundleList != null )
        {
            return resourceBundleList;
        }

        if ( getLocalesRuntime() == null )
        {
            return null;
        }

        defaultResourceBundleList.getParentFile().mkdirs();
        return defaultResourceBundleList;
    }

    public Boolean getResourceHack()
    {
        return resourceHack;
    }

    public String[] getRslUrls()
    {
        if ( rslUrls == null )
        {
            return DEFAULT_RSL_URLS;
        }
        return rslUrls;
    }

    public final String[] getRuntimeSharedLibraries()
    {
        // Set<Artifact> dependencies = getDependencies( not( GLOBAL_MATCHER
        // ),//
        // anyOf( scope( RSL ), scope( CACHING ), scope( EXTERNAL ) ) );
        //
        // return PathUtil.getCanonicalPath( MavenUtils.getFiles( dependencies )
        // );
        return null;
    }

    @SuppressWarnings( "unchecked" )
    public IRuntimeSharedLibraryPath[] getRuntimeSharedLibraryPath()
    {
        // get all the rsl dependencies
        Set<Artifact> dependencies = getDependencies( not( GLOBAL_MATCHER ),//
                                                      anyOf( scope( RSL ), scope( CACHING ) ) );

        if ( dependencies.isEmpty() )
        {
            return null;
        }

        final String[] rslUrls = getRslUrls();
        final String[] policyFileUrls = getPolicyFileUrls();

        // not sure if all this validation are required
        if ( rslUrls.length < policyFileUrls.length //
            && policyFileUrls.length != 0 //
            && rslUrls.length != policyFileUrls.length //
            && rslUrls.length != policyFileUrls.length - 1 )
        {
            throw new IllegalArgumentException(
                                                "The number of elements on RSL Urls and Policy File Urls doesn't match: "
                                                    + rslUrls.length + "/" + rslUrls.length );
        }

        List<IRuntimeSharedLibraryPath> rsls = new ArrayList<IRuntimeSharedLibraryPath>();
        for ( final Artifact artifact : dependencies )
        {

            rsls.add( new IRuntimeSharedLibraryPath()
            {
                public String pathElement()
                {
                    return artifact.getFile().getAbsolutePath();
                }

                public Map<String, String> rslUrl()
                {
                    return calculateRuntimeLibraryPath( artifact, rslUrls, policyFileUrls );
                }
            } );
        }

        return rsls.toArray( new IRuntimeSharedLibraryPath[rsls.size()] );
    }

    public IRuntimeSharedLibrarySettingsConfiguration getRuntimeSharedLibrarySettingsConfiguration()
    {
        return this;
    }

    public String getServices()
    {
        if ( services != null )
        {
            return PathUtil.path( services );
        }

        File cfg = new File( configDirectory, "services-config.xml" );
        if ( cfg.exists() )
        {
            return PathUtil.path( cfg );
        }
        return null;
    }

    public Boolean getShowActionscriptWarnings()
    {
        return getCompilerWarnings().get( "show-actionscript-warnings" );
    }

    public Boolean getShowBindingWarnings()
    {
        return getCompilerWarnings().get( "show-binding-warnings" );
    }

    public Boolean getShowDependencyWarnings()
    {
        return getCompilerWarnings().get( "show-dependency-warnings" );
    }

    public Boolean getShowDeprecationWarnings()
    {
        return getCompilerWarnings().get( "show-deprecation-warnings" );
    }

    public Boolean getShowInvalidCssPropertyWarnings()
    {
        return getCompilerWarnings().get( "show-invalid-css-property-warnings" );
    }

    public Boolean getShowShadowedDeviceFontWarnings()
    {
        return getCompilerWarnings().get( "show-shadowed-device-font-warnings" );
    }

    public Boolean getShowUnusedTypeSelectorWarnings()
    {
        return getCompilerWarnings().get( "show-unused-type-selector-warnings" );
    }

    public File getSignatureDirectory()
    {
        return signatureDirectory;
    }

    @FlexCompatibility( minVersion = "4.5.0" )
    public String getSizeReport()
    {
        File sizeReport = new File( getTargetDirectory(), getFinalName() + "-" + SIZE_REPORT + "." + XML );

        if ( sizeReportAttach )
        {
            if ( getClassifier() != null )
            {
                getLog().warn( "Size report is not attached for artifacts with classifier" );
            }
            else
            {
                projectHelper.attachArtifact( project, XML, SIZE_REPORT, sizeReport );
            }
        }
        return PathUtil.path( sizeReport );
    }

    public File[] getSourcePath()
    {
        List<File> files = new ArrayList<File>();

        files.addAll( PathUtil.existingFilesList( compileSourceRoots ) );

        if ( localesCompiled != null )
        {
            if ( localesSourcePath.getParentFile().exists() )
            {
                files.add( localesSourcePath );
            }
        }

        return files.toArray( new File[0] );
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

    @SuppressWarnings( "unchecked" )
    public List<String> getTheme()
    {
        List<File> themes = new ArrayList<File>();
        if ( this.themes != null )
        {
            themes.addAll( asList( files( this.themes, getResourcesTargetDirectories() ) ) );
        }
        themes.addAll( //
        asList( MavenUtils.getFiles( getDependencies( anyOf( type( SWC ), type( CSS ) ),//
                                                      scope( THEME ) ) ) ) );

        configureThemeSparkCss( themes );
        configureThemeHaloSwc( themes );

        if ( themes.isEmpty() )
        {
            return null;
        }

        return pathsList( themes );
    }

    public String getTitle()
    {
        if ( this.metadata != null )
        {
            return this.metadata.getDescription();
        }

        return project.getName();
    }

    @FlexCompatibility( minVersion = "4.0.0.13007" )
    public String getToolsLocale()
    {
        if ( toolsLocale == null )
        {
            throw new IllegalArgumentException( "Invalid toolsLocale it must be not null and must be in Java format."
                + "  For example, \"en\" or \"ja_JP\"" );
        }

        return toolsLocale;
    }

    public String getTranslationFormat()
    {
        return translationFormat;
    }

    public Boolean getUseNetwork()
    {
        return useNetwork;
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
        return getCompilerWarnings().get( "warn-array-tostring-changes" );
    }

    public Boolean getWarnAssignmentWithinConditional()
    {
        return getCompilerWarnings().get( "warn-assignment-within-conditional" );
    }

    public Boolean getWarnBadArrayCast()
    {
        return getCompilerWarnings().get( "warn-bad-array-cast" );
    }

    public Boolean getWarnBadBoolAssignment()
    {
        return getCompilerWarnings().get( "warn-bad-bool-assignment" );
    }

    public Boolean getWarnBadDateCast()
    {
        return getCompilerWarnings().get( "warn-bad-date-cast" );
    }

    public Boolean getWarnBadEs3TypeMethod()
    {
        return getCompilerWarnings().get( "warn-bad-es3-type-method" );
    }

    public Boolean getWarnBadEs3TypeProp()
    {
        return getCompilerWarnings().get( "warn-bad-es3-type-prop" );
    }

    public Boolean getWarnBadNanComparison()
    {
        return getCompilerWarnings().get( "warn-bad-nan-comparison" );
    }

    public Boolean getWarnBadNullAssignment()
    {
        return getCompilerWarnings().get( "warn-bad-null-assignment" );
    }

    public Boolean getWarnBadNullComparison()
    {
        return getCompilerWarnings().get( "warn-bad-null-comparison" );
    }

    public Boolean getWarnBadUndefinedComparison()
    {
        return getCompilerWarnings().get( "warn-bad-undefined-comparison" );
    }

    public Boolean getWarnBooleanConstructorWithNoArgs()
    {
        return getCompilerWarnings().get( "warn-boolean-constructor-with-no-args" );
    }

    public Boolean getWarnChangesInResolve()
    {
        return getCompilerWarnings().get( "warn-changes-in-resolve" );
    }

    public Boolean getWarnClassIsSealed()
    {
        return getCompilerWarnings().get( "warn-class-is-sealed" );
    }

    public Boolean getWarnConstNotInitialized()
    {
        return getCompilerWarnings().get( "warn-const-not-initialized" );
    }

    public Boolean getWarnConstructorReturnsValue()
    {
        return getCompilerWarnings().get( "warn-constructor-returns-value" );
    }

    public Boolean getWarnDeprecatedEventHandlerError()
    {
        return getCompilerWarnings().get( "warn-deprecated-event-handler-error" );
    }

    public Boolean getWarnDeprecatedFunctionError()
    {
        return getCompilerWarnings().get( "warn-deprecated-function-error" );
    }

    public Boolean getWarnDeprecatedPropertyError()
    {
        return getCompilerWarnings().get( "warn-deprecated-property-error" );
    }

    public Boolean getWarnDuplicateArgumentNames()
    {
        return getCompilerWarnings().get( "warn-duplicate-argument-names" );
    }

    public Boolean getWarnDuplicateVariableDef()
    {
        return getCompilerWarnings().get( "warn-duplicate-variable-def" );
    }

    public Boolean getWarnForVarInChanges()
    {
        return getCompilerWarnings().get( "warn-for-var-in-changes" );
    }

    public Boolean getWarnImportHidesClass()
    {
        return getCompilerWarnings().get( "warn-import-hides-class" );
    }

    public Boolean getWarnings()
    {
        return showWarnings;
    }

    public Boolean getWarnInstanceOfChanges()
    {
        return getCompilerWarnings().get( "warn-instance-of-changes" );
    }

    public Boolean getWarnInternalError()
    {
        return getCompilerWarnings().get( "warn-internal-error" );
    }

    public Boolean getWarnLevelNotSupported()
    {
        return getCompilerWarnings().get( "warn-level-not-supported" );
    }

    public Boolean getWarnMissingNamespaceDecl()
    {
        return getCompilerWarnings().get( "warn-missing-namespace-decl" );
    }

    public Boolean getWarnNegativeUintLiteral()
    {
        return getCompilerWarnings().get( "warn-negative-uint-literal" );
    }

    public Boolean getWarnNoConstructor()
    {
        return getCompilerWarnings().get( "warn-no-constructor" );
    }

    public Boolean getWarnNoExplicitSuperCallInConstructor()
    {
        return getCompilerWarnings().get( "warn-no-explicit-super-call-in-constructor" );
    }

    public Boolean getWarnNoTypeDecl()
    {
        return getCompilerWarnings().get( "warn-no-type-decl" );
    }

    public Boolean getWarnNumberFromStringChanges()
    {
        return getCompilerWarnings().get( "warn-number-from-string-changes" );
    }

    public Boolean getWarnScopingChangeInThis()
    {
        return getCompilerWarnings().get( "warn-scoping-change-in-this" );
    }

    public Boolean getWarnSlowTextFieldAddition()
    {
        return getCompilerWarnings().get( "warn-slow-text-field-addition" );
    }

    public Boolean getWarnUnlikelyFunctionValue()
    {
        return getCompilerWarnings().get( "warn-unlikely-function-value" );
    }

    public Boolean getWarnXmlClassHasChanged()
    {
        return getCompilerWarnings().get( "warn-xml-class-has-changed" );
    }

    @SuppressWarnings( "unchecked" )
    public boolean isCompilationRequired()
    {
        if ( !quick )
        {
            // not running at quick mode
            return true;
        }

        Artifact artifact;
        try
        {
            artifact =
                resolve( project.getGroupId(), project.getArtifactId(), project.getVersion(), getClassifier(),
                         project.getPackaging() );
        }
        catch ( RuntimeMavenResolutionException e )
        {
            artifact = e.getArtifact();
        }

        if ( !artifact.isResolved() || artifact.getFile() == null || !artifact.getFile().exists() )
        {
            // Recompile, file doesn't exists
            getLog().warn( "Can't find any older installed version." );
            return true;
        }

        long lastCompiledArtifact = artifact.getFile().lastModified();

        boolean required = false;
        Set<Artifact> dependencies = getDependencies();
        for ( Artifact dependency : dependencies )
        {
            if ( org.apache.commons.io.FileUtils.isFileNewer( dependency.getFile(), lastCompiledArtifact ) )
            {
                // a dependency is newer, recompile
                getLog().warn( "Found a updated dependency: " + dependency );
                required = true;
            }
        }

        if ( !required )
        {
            Collection<File> files =
                org.apache.commons.io.FileUtils.listFiles( new File( project.getBuild().getSourceDirectory() ),
                                                           new AgeFileFilter( lastCompiledArtifact, false ),
                                                           TrueFileFilter.INSTANCE );

            // If has any newer file
            if ( files.size() > 0 )
            {
                getLog().warn( "Found some updated files." );
                required = true;
            }
        }

        if ( !required )
        {
            try
            {
                final File output = new File( getOutput() );

                FileUtils.copyFile( artifact.getFile(), output );

                if ( !output.setLastModified( artifact.getFile().lastModified() ) )
                {
                    getLog().warn( "Could not set modified on copied artifact. Unnecessary rebuilds will occur." );
                }
            }
            catch ( IOException e )
            {
                getLog().error( "Unable to copy installed version to target folder.", e );
                return true;
            }
        }

        // nothing new was found.
        return required;
    }

    public void versionCheck()
    {
        if ( ignoreVersionIssues )
        {
            return;
        }

        String compilerVersion = getCompilerVersion();
        String frameworkVersion = getFrameworkVersion();
        if ( compilerVersion == null || frameworkVersion == null )
        {
            // ignore, missing version
            return;
        }

        if ( !compilerVersion.equals( frameworkVersion ) )
        {
            String msg =
                "Flex compiler and flex framework versions doesn't match. Compiler: '"
                    + compilerVersion
                    + "' - Framework: '"
                    + frameworkVersion
                    + "'.\n"
                    + " You can use 'ignoreVersionIssues' to disable this check.  Please refer to Flexmojos maven doc.\n"
                    + "If you prefer fixing it instead of ignoring, take a look at: https://docs.sonatype.org/display/FLEXMOJOS/How+to+set+Flex+SDK+version";
            throw new IllegalStateException( msg );
        }
    }

}
