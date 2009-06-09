/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.compiler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Developer;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.sonatype.flexmojos.AbstractIrvinMojo;
import org.sonatype.flexmojos.common.FlexExtension;
import org.sonatype.flexmojos.common.FlexScopes;
import org.sonatype.flexmojos.compatibilitykit.FlexCompatibility;
import org.sonatype.flexmojos.compatibilitykit.FlexMojo;
import org.sonatype.flexmojos.utilities.MavenUtils;
import org.sonatype.flexmojos.utilities.Namespace;

import flex2.tools.oem.Builder;
import flex2.tools.oem.Configuration;
import flex2.tools.oem.Report;
import flex2.tools.oem.internal.OEMConfiguration;

public abstract class AbstractFlexCompilerMojo<E extends Builder>
    extends AbstractIrvinMojo
    implements FlexMojo, FlexScopes, FlexExtension
{

    private static final String REPORT_LINK = "link";

    private static final String REPORT_CONFIG = "config";

    public static final String[] DEFAULT_RSL_URLS =
        new String[] { "/{contextRoot}/rsl/{artifactId}-{version}.{extension}" };

    public static final String DEFAULT_RUNTIME_LOCALE_OUTPUT_PATH =
        "/{contextRoot}/locales/{artifactId}-{version}-{locale}.{extension}";

    private static final String COMPATIBILITY_2_0_0 = "2.0.0";

    private static final String COMPATIBILITY_2_0_1 = "2.0.1";

    private static final String COMPATIBILITY_3_0_0 = "3.0.0";

    /**
     * license.properties locations get from http://livedocs.adobe.com/flex/3/html/configuring_environment_2.html
     */
    private static final File[] licensePropertiesLocations =
        new File[] { new File( // Windows XP
                               "C:/Documents and Settings/All Users/Application Data/Adobe/Flex/license.properties" ),
            new File( // Windows Vista
                      "C:/ProgramData/Adobe/Flex/license.properties" ),
            new File( // Mac OSX
                      "/Library/Application Support/Adobe/Flex/license.properties" ),
            new File( // Linux
                      System.getProperty( "user.home" ), ".adobe/Flex/license.properties" ) };

    /**
     * Turn on generation of accessible SWFs.
     * 
     * @parameter default-value="false"
     */
    private boolean accessible;

    /**
     * LW : needed for expression evaluation Note : needs at least maven 2.0.8 because of MNG-3062 The maven
     * MojoExecution needed for ExpressionEvaluation
     * 
     * @parameter expression="${mojoExecution}"
     * @required
     * @readonly
     */
    protected MojoExecution execution;

    /**
     * LW : needed for expression evaluation The maven MojoExecution needed for ExpressionEvaluation
     * 
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    protected MavenSession context;

    /**
     * Sets the locales that the compiler uses to replace <code>{locale}</code> tokens that appear in some configuration
     * values. This is equivalent to using the <code>compiler.locale</code> option of the mxmlc or compc compilers. <BR>
     * Usage:
     * 
     * <pre>
     * &lt;locales&gt;
     *    &lt;locale&gt;en_US&lt;/locale&gt;
     *    &lt;locale&gt;pt_BR&lt;/locale&gt;
     *    &lt;locale&gt;es_ES&lt;/locale&gt;
     * &lt;/locales&gt;
     * </pre>
     * 
     * @parameter
     * @deprecated
     */
    protected String[] locales;

    /**
     * List of path elements that form the roots of ActionScript class hierarchies.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;sourcePaths&gt;
     *    &lt;path&gt;${baseDir}/src/main/flex&lt;/path&gt;
     * &lt;/sourcePaths&gt;
     * </pre>
     * 
     * By default use Maven source and resources folders.
     * 
     * @parameter
     */
    // * @readonly
    protected File[] sourcePaths;

    /**
     * Allow the source-path to have path-elements which contain other path-elements
     * 
     * @parameter default-value="false"
     */
    private boolean allowSourcePathOverlap;

    /**
     * Run the AS3 compiler in a mode that detects legal but potentially incorrect code. Equivalent compiler option:
     * warnings
     * 
     * @parameter default-value="true"
     */
    private boolean showWarnings;

    /**
     * Enables checking of the following ActionScript warnings:
     * 
     * <pre>
     * --compiler.warn-array-tostring-changes
     * --compiler.warn-assignment-within-conditional
     * --compiler.warn-bad-array-cast
     * --compiler.warn-bad-bool-assignment
     * --compiler.warn-bad-date-cast
     * --compiler.warn-bad-es3-type-method
     * --compiler.warn-bad-es3-type-prop
     * --compiler.warn-bad-nan-comparison
     * --compiler.warn-bad-null-assignment
     * --compiler.warn-bad-null-comparison
     * --compiler.warn-bad-undefined-comparison
     * --compiler.warn-boolean-constructor-with-no-args
     * --compiler.warn-changes-in-resolve
     * --compiler.warn-class-is-sealed
     * --compiler.warn-const-not-initialized
     * --compiler.warn-constructor-returns-value
     * --compiler.warn-deprecated-event-handler-error
     * --compiler.warn-deprecated-function-error
     * --compiler.warn-deprecated-property-error
     * --compiler.warn-duplicate-argument-names
     * --compiler.warn-duplicate-variable-def
     * --compiler.warn-for-var-in-changes
     * --compiler.warn-import-hides-class
     * --compiler.warn-instance-of-changes
     * --compiler.warn-internal-error
     * --compiler.warn-level-not-supported
     * --compiler.warn-missing-namespace-decl
     * --compiler.warn-negative-uint-literal
     * --compiler.warn-no-constructor
     * --compiler.warn-no-explicit-super-call-in-constructor
     * --compiler.warn-no-type-decl
     * --compiler.warn-number-from-string-changes
     * --compiler.warn-scoping-change-in-this
     * --compiler.warn-slow-text-field-addition
     * --compiler.warn-unlikely-function-value
     * --compiler.warn-xml-class-has-changed
     * </pre>
     * 
     * <BR>
     * Usage:
     * 
     * <pre>
     * &lt;warnigs&gt;
     *   &lt;arrayTostringChanges&gt;true&lt;/arrayTostringChanges&gt;
     *   &lt;assignmentWithinConditional&gt;false&lt;/assignmentWithinConditional&gt;
     * &lt;/warnigs&gt;
     * </pre>
     * 
     * @see Warning
     * @parameter
     */
    private Warning warnings;

    /**
     * A password that is embedded in the application
     * 
     * @parameter
     */
    protected String debugPassword;

    /**
     * Turn on writing of generated/*.as files to disk. These files are generated by the compiler during mxml
     * translation and are helpful with understanding and debugging Flex applications.
     * 
     * @parameter default-value="false"
     */
    private boolean keepGeneratedActionscript;

    /**
     * Specify a URI to associate with a manifest of components for use as MXML elements.<BR>
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
    private Namespace[] namespaces;

    /**
     * Enable post-link SWF optimization.
     * 
     * @parameter default-value="true"
     */
    private boolean optimize;

    /**
     * If the <code>incremental</code> input argument is <code>false</code>, this method recompiles all parts of the
     * object. If the <code>incremental</code> input argument is <code>true</code>, this method compiles only the parts
     * of the object that have changed since the last compilation.
     * 
     * @parameter default-value="false" expression="${incremental}"
     */
    private boolean incremental;

    /**
     * Keep the following AS3 metadata in the bytecodes.<BR>
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
     * Run the AS3 compiler in strict error checking mode.
     * 
     * @parameter default-value="true"
     */
    private boolean strict;

    /**
     * Use the ActionScript 3 class based object model for greater performance and better error reporting. In the class
     * based object model most built-in functions are implemented as fixed methods of classes (-strict is recommended,
     * but not required, for earlier errors)
     * 
     * @parameter default-value="true"
     */
    private boolean as3;

    /**
     * Use the ECMAScript edition 3 prototype based object model to allow dynamic overriding of prototype properties. In
     * the prototype based object model built-in functions are implemented as dynamic properties of prototype objects
     * (-strict is allowed, but may result in compiler errors for references to dynamic properties)
     * 
     * @parameter default-value="false"
     */
    private boolean es;

    /**
     * Turns on the display of stack traces for uncaught runtime errors.
     * 
     * @parameter default-value="false"
     */
    private boolean verboseStacktraces;

    /**
     * Local Fonts Snapshot File containing cached system font licensing information produced via
     * <code>java -cp mxmlc.jar flex2.tools.FontSnapshot (fontpath)</code>. Will default to winFonts.ser on Windows XP
     * and macFonts.ser on Mac OS X.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;fonts&gt;
     *   &lt;advancedAntiAliasing&gt;true&lt;/advancedAntiAliasing&gt;
     *   &lt;flashType&gt;true&lt;/flashType&gt;
     *   &lt;languages&gt;
     *     &lt;englishRange&gt;U+0020-U+007E&lt;/englishRange&gt;
     *   &lt;/languages&gt;
     *   &lt;localFontsSnapshot&gt;${baseDir}/src/main/resources/fonts.ser&lt;/localFontsSnapshot&gt;
     *   &lt;managers&gt;
     *     &lt;manager&gt;flash.fonts.BatikFontManager&lt;/manager&gt;
     *   &lt;/managers&gt;
     *   &lt;maxCachedFonts&gt;20&lt;/maxCachedFonts&gt;
     *   &lt;maxGlyphsPerFace&gt;1000&lt;/maxGlyphsPerFace&gt;
     * &lt;/fonts&gt;
     * </pre>
     * 
     * @parameter
     */
    private Font fonts;

    /**
     * Enables SWFs to access the network.
     * 
     * @parameter default-value="true"
     */
    private boolean useNetwork;

    /**
     * licenses: specifies a list of product and serial number pairs.<BR>
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
     * defines: specifies a list of define directive key and value pairs. For example, CONFIG::debugging<BR>
     * Usage:
     * 
     * <pre>
     * &lt;defines&gt;
     *   &lt;SOMETHING::aNumber&gt;2.2&lt;/SOMETHING::aNumber&gt;
     *   &lt;SOMETHING::aString&gt;&quot;text&quot;&lt;/SOMETHING::aString&gt;
     * &lt;/defines&gt;
     * </pre>
     * 
     * @parameter
     * @deprecated See definesDeclaration
     */
    private Map<String, String> defines;

    /**
     * defines: specifies a list of define directive key and value pairs. For example, CONFIG::debugging<BR>
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
    private Properties definesDeclaration;

    /**
     * Sets the context root path so that the compiler can replace <code>{context.root}</code> tokens for service
     * channel endpoints.
     * 
     * @parameter
     */
    private String contextRoot;

    /**
     * Uses the default compiler options as base
     * 
     * @parameter default-value="false"
     */
    protected boolean linkReport;

    /**
     * Writes the configuration report to a file after the build.
     * 
     * @parameter default-value="false" expression="${configurationReport}"
     */
    protected boolean configurationReport;

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
     * @parameter
     */
    protected MavenArtifact[] loadExterns;

    /**
     * Load a file containing configuration options If not defined, by default will search for one on resources folder.
     * 
     * @parameter
     */
    protected File configFile;

    /**
     * specifies the version of the player the application is targeting. Features requiring a later version will not be
     * compiled into the application. The minimum value supported is "9.0.0".
     * 
     * @parameter default-value="9.0.0"
     */
    private String targetPlayer;

    /**
     * Sets the metadata section of the application SWF. This is equivalent to the <code>raw-metadata</code> option of
     * the mxmlc or compc compilers. Need a well-formed XML fragment
     * 
     * @parameter
     */
    private String rawMetadata;

    /**
     * SWF metadata useless there is no API to read it.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;metadata&gt;
     *   &lt;contributor&gt;buddy&lt;/contributor&gt;
     *   &lt;creator&gt;me&lt;/creator&gt;
     *   &lt;date&gt;01/01/01&lt;/date&gt;
     *   &lt;descriptions&gt;
     *     &lt;en_US&gt;Simple description&lt;/en_US&gt;
     *   &lt;/descriptions&gt;
     *   &lt;language&gt;en_US&lt;/language&gt;
     *   &lt;publishers&gt;
     *     &lt;publisher&gt;publisher1&lt;/publisher&gt;
     *     &lt;publisher&gt;publisher2&lt;/publisher&gt;
     *   &lt;/publishers&gt;
     *   &lt;titles&gt;
     *     &lt;en_US&gt;Project title&lt;/en_US&gt;
     *   &lt;/titles&gt;
     * &lt;/metadata&gt;
     * </pre>
     * 
     * @parameter
     */
    private Metadata metadata;

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
     * </pre>
     * 
     * default-value="/{contextRoot}/rsl/{artifactId}-{version}.{extension}" <BR>
     * Usage:
     * 
     * <pre>
     * &lt;rslUrls&gt;
     *   &lt;url&gt;/{contextRoot}/rsl/{artifactId}-{version}.{extension}&lt;/url&gt;
     * &lt;/rslUrls&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] rslUrls;

    /**
     * Resource module or resource library output path
     * 
     * @parameter 
     *            default-value="${project.build.directory}/locales/${project.artifactId}-${project.version}-{locale}.{extension}"
     */
    private String runtimeLocaleOutputPath;

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
     * Sets the location of the Flex Data Services service configuration file. This is equivalent to using the
     * <code>compiler.services</code> option of the mxmlc and compc compilers. If not define will look inside resources
     * directory for services-config.xml
     * 
     * @parameter
     */
    private File services;

    /**
     * When true resources are compiled into Application or Library. When false resources are compiled into separated
     * Application or Library files. If not defined no resourceBundle generation is done
     * 
     * @parameter
     * @deprecated
     */
    private Boolean mergeResourceBundle;

    /**
     * Define the base path to locate resouce bundle files Accept some special tokens:
     * 
     * <pre>
     * {locale}     - replace by locale name
     * </pre>
     * 
     * @parameter default-value="${basedir}/src/main/locales/{locale}"
     */
    protected String resourceBundlePath;

    /**
     * This is equilvalent to the <code>compiler.mxmlc.compatibility-version</code> option of the compc compiler. Must
     * be in the form <major>.<minor>.<revision> Valid values: <tt>2.0.0</tt>, <tt>2.0.1</tt> and <tt>3.0.0</tt>
     * 
     * @see http://livedocs.adobe.com/flex/3/html/help.html?content=versioning_4. html
     * @parameter
     */
    private String compatibilityVersion;

    /**
     * Sets the ActionScript file encoding. The compiler uses this encoding to read the ActionScript source files. This
     * is equivalent to using the <code>actionscript-file-encoding</code> option of the mxmlc or compc compilers.
     * <p>
     * The character encoding; for example <code>UTF-8</code> or <code>Big5</code>.
     * 
     * @parameter default-value="UTF-8"
     */
    private String encoding;

    /**
     * Sets the location of the default CSS file. This is equivalent to using the <code>compiler.defaults-css-url</code>
     * option of the mxmlc or compc compilers</code>.
     * 
     * @parameter
     */
    private File defaultsCss;

    /**
     * Sets the default background color. You can override this by using the application code. This is the equivalent of
     * the <code>default-background-color</code> option of the mxmlc or compc compilers.
     * 
     * @parameter default-value="869CA7"
     */
    private String defaultBackgroundColor;

    /**
     * Sets the default frame rate to be used in the application. This is the equivalent of the
     * <code>default-frame-rate</code> option of the mxmlc or compc compilers.
     * 
     * @parameter default-value="24"
     */
    private int defaultFrameRate;

    /**
     * Sets the default script execution limits (which can be overridden by root attributes). This is equivalent to
     * using the <code>default-script-limits</code> option of the mxmlc or compc compilers. Recursion depth
     * 
     * @parameter default-value="1000"
     */
    private int scriptMaxRecursionDepth;

    /**
     * Sets the default script execution limits (which can be overridden by root attributes). This is equivalent to
     * using the <code>default-script-limits</code> option of the mxmlc or compc compilers. Execution time, in seconds
     * 
     * @parameter default-value="60"
     */
    private int scriptMaxExecutionTime;

    /**
     * Sets the default application width in pixels. This is equivalent to using the <code>default-size</code> option of
     * the mxmlc or compc compilers.
     * 
     * @parameter default-value="500"
     */
    private int defaultSizeWidth;

    /**
     * Sets the default application height in pixels. This is equivalent to using the <code>default-size</code> option
     * of the mxmlc or compc compilers.
     * 
     * @parameter default-value="375"
     */
    private int defaultSizeHeight;

    /**
     * Sets a list of definitions to omit from linking when building an application. This is equivalent to using the
     * <code>externs</code> option of the mxmlc and compc compilers. An array of definitions (for example, classes,
     * functions, variables, or namespaces).<BR>
     * Usage:
     * 
     * <pre>
     * &lt;externs&gt;
     *   &lt;extern&gt;com.acme.AClass&lt;/extern&gt;
     * &lt;/externs&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] externs;

    /**
     * Sets a SWF frame label with a sequence of class names that are linked onto the frame. This is equivalent to using
     * the <code>frames.frame</code> option of the mxmlc or compc compilers.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;frames&gt;
     *   &lt;frame&gt;
     *     &lt;label&gt;frame1&lt;/label&gt;
     *     &lt;classNames&gt;
     *       &lt;className&gt;com.acme.AClass&lt;/className&gt;
     *     &lt;/classNames&gt;
     *   &lt;/frame&gt;
     * &lt;/frames&gt;
     * </pre>
     * 
     * @parameter
     */
    private FrameLabel[] frames;

    /**
     * Sets a list of definitions to always link in when building an application. This is equivalent to using the
     * <code>includes</code> option of the mxmlc or compc compilers. An array of definitions (for example, classes,
     * functions, variables, or namespaces).<BR>
     * Usage:
     * 
     * <pre>
     * &lt;includes&gt;
     *   &lt;include&gt;com.acme.AClass&lt;/include&gt;
     * &lt;/includes&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] includes;

    /**
     * Sets the compiler when it runs on a server without a display. This is equivalent to using the
     * <code>compiler.headless-server</code> option of the mxmlc or compc compilers. that value determines if the
     * compiler is running on a server without a display.
     * 
     * @parameter default-value="false"
     */
    private boolean headlessServer;

    /**
     * Instructs the compiler to keep a style sheet's type selector in a SWF file, even if that type (the class) is not
     * used in the application. This is equivalent to using the <code>compiler.keep-all-type-selectors</code> option of
     * the mxmlc or compc compilers.
     * 
     * @parameter default-value="false"
     */
    private boolean keepAllTypeSelectors;

    /**
     * Determines whether resources bundles are included in the application. This is equivalent to using the
     * <code>compiler.use-resource-bundle-metadata</code> option of the mxmlc or compc compilers.
     * 
     * @parameter default-value="true"
     */
    private boolean useResourceBundleMetadata;

    /**
     * Determines whether to compile against libraries statically or use RSLs. Set this option to true to ignore the
     * RSLs specified by the <code>rslUrls</code>. Set this option to false to use the RSLs.
     * 
     * @parameter default-value="false"
     */
    private boolean staticLinkRuntimeSharedLibraries;

    /**
     * Verifies the RSL loaded has the same digest as the RSL specified when the application was compiled. This is
     * equivalent to using the <code>verify-digests</code> option in the mxmlc compiler.
     * 
     * @parameter default-value="true"
     */
    private boolean verifyDigests;

    /**
     * Previous compilation data, used to incremental builds
     */
    private File compilationData;

    /**
     * Builder to be used by compiler
     */
    protected E builder;

    /**
     * Flex OEM compiler configurations We can not use interface, because Flex SDK 3.2.0.3958 has method
     * "setConfiguration(java.lang.String[] strings)" only in OEMConfiguration (Flex SDK 4 is ok)
     */
    protected Configuration configuration;

    /**
     * When true sets the artifact generated by this mojos as pom artifact
     */
    protected boolean isSetProjectFile = true;

    /**
     * Generated link report file
     */
    protected File linkReportFile;

    /**
     * Quick compile mode. When true, flexmojos will check if the last artifact available at maven repository is newer
     * then sources. If so, will not recompile.
     * 
     * @parameter default-value="false" expression="${quick.compile}"
     */
    private boolean quick;

    /**
     * When enabled flexmojos will add a custom path resolver to flex compiler. This allow flexmojos to resolve Embed
     * assets located at src/main/resources. This is a workaround and it is described at
     * http://bugs.adobe.com/jira/browse/SDK-15466
     * 
     * @parameter default-value="true" expression="${enableMavenResourcesResolver}"
     */
    private boolean enableMavenResourcesResolver;

    /**
     * Sets the locales that should be used to generate resource bundles. <BR>
     * Usage:
     * 
     * <pre>
     * &lt;runtimeLocales&gt;
     *    &lt;locale&gt;en_US&lt;/locale&gt;
     *    &lt;locale&gt;pt_BR&lt;/locale&gt;
     *    &lt;locale&gt;es_ES&lt;/locale&gt;
     * &lt;/runtimeLocales&gt;
     * </pre>
     * 
     * @parameter
     */
    protected String[] runtimeLocales;

    /**
     * Sets the locales that the compiler uses to replace <code>{locale}</code> tokens that appear in some configuration
     * values. This is equivalent to using the <code>compiler.locale</code> option of the mxmlc or compc compilers. <BR>
     * Usage:
     * 
     * <pre>
     * &lt;compiledLocales&gt;
     *    &lt;locale&gt;en_US&lt;/locale&gt;
     *    &lt;locale&gt;pt_BR&lt;/locale&gt;
     *    &lt;locale&gt;es_ES&lt;/locale&gt;
     * &lt;/compiledLocales&gt;
     * </pre>
     * 
     * @parameter
     */
    protected String[] compiledLocales;

    /**
     * List of CSS or SWC files to apply as a theme. <>BR Usage:
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
     * This is equilvalent to the <code>include-resource-bundles</code> option of the compc compiler.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;includeResourceBundles&gt;
     *   &lt;bundle&gt;SharedResources&lt;/bundle&gt;
     *   &lt;bundle&gt;collections&lt;/bundle&gt;
     *   &lt;bundle&gt;containers&lt;/bundle&gt;
     * &lt;/includeResourceBundles&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] includeResourceBundles;

    /**
     * @parameter TODO check if is used/useful
     */
    private MavenArtifact[] includeResourceBundlesArtifact;

    /**
     * if true, manifest entries with lookupOnly=true are included in SWC catalog. default is false. This exists only so
     * that manifests can mention classes that come in from filespec rather than classpath, e.g. in playerglobal.swc.
     * 
     * @parameter default-value="false"
     */
    private boolean includeLookupOnly;

    /**
     * Construct instance
     */
    public AbstractFlexCompilerMojo()
    {
        super();
    }

    /**
     * Setup before compilation of source
     */
    @SuppressWarnings( "unchecked" )
    @Override
    public void setUp()
        throws MojoExecutionException, MojoFailureException
    {

        processLocales();

        if ( sourcePaths == null )
        {
            List<String> sourceRoots;
            if ( project.getExecutionProject() != null )
            {
                sourceRoots = project.getExecutionProject().getCompileSourceRoots();
            }
            else
            {
                sourceRoots = project.getCompileSourceRoots();
            }
            List<File> sources = getValidSourceRoots( sourceRoots );
            // if ( mergeResourceBundle != null && mergeResourceBundle )
            if ( compiledLocales != null )
            {
                sources.add( new File( resourceBundlePath ) );
            }
            sourcePaths = sources.toArray( new File[sources.size()] );
        }

        if ( configFile == null )
        {
            List<Resource> resources = build.getResources();
            for ( Resource resource : resources )
            {
                File cfg = new File( resource.getDirectory(), "config.xml" );
                if ( cfg.exists() )
                {
                    configFile = cfg;
                    break;
                }
            }
        }
        if ( configFile != null )
        {
            getLog().info( "Using configuration file " + configFile );
        }

        if ( services == null )
        {
            List<Resource> resources = build.getResources();
            for ( Resource resource : resources )
            {
                File cfg = new File( resource.getDirectory(), "services-config.xml" );
                if ( cfg.exists() )
                {
                    services = cfg;
                    break;
                }
            }
        }

        if ( rslUrls == null )
        {
            rslUrls = DEFAULT_RSL_URLS;
        }

        if ( policyFileUrls == null )
        {
            policyFileUrls = new String[] { "" };
        }

        if ( runtimeLocaleOutputPath == null )
        {
            runtimeLocaleOutputPath = DEFAULT_RUNTIME_LOCALE_OUTPUT_PATH;
        }

        if ( metadata == null )
        {
            metadata = new Metadata();
            if ( project.getDevelopers() != null && !project.getDevelopers().isEmpty() )
            {
                List<Developer> developers = project.getDevelopers();
                for ( Developer d : developers )
                {
                    metadata.setCreator( d.getName() );
                    break;
                }
            }

            if ( project.getContributors() != null && !project.getContributors().isEmpty() )
            {
                List<Contributor> contributors = project.getContributors();
                for ( Contributor c : contributors )
                {
                    metadata.setContributor( c.getName() );
                    break;
                }
            }
            metadata.setDate( new Date() );
            // FIXME what to do here?
            // if ( locales != null )
            // {
            // metadata.setLanguage( locales[0] );
            // metadata.addDescription( locales[0], project.getDescription() );
            // metadata.addTitle( locales[0], project.getName() );
            // }
        }

        if ( licenses == null )
        {
            licenses = getLicenses();
        }

        if ( licenses != null )
        {
            try
            {
                Class.forName( "flex.license.License" );
            }
            catch ( ClassNotFoundException e )
            {
                getLog().warn(
                               "Unable to find license.jar on classpath. Check wiki for instructions about how to add it:\n   https://docs.sonatype.org/display/FLEXMOJOS/FAQ#FAQ-1.3" );
                getLog().debug( "Java classpath: " + System.getProperty( "java.class.path" ) );
            }
        }

        validateLocales( runtimeLocales );
        validateLocales( compiledLocales );

        configuration = builder.getDefaultConfiguration();
        configure();

        compilationData = new File( build.getDirectory(), build.getFinalName() + ".incr" );

        setMavenPathResolver( builder );

        // compiler didn't create parent if it doesn't exists
        getOutput().getParentFile().mkdirs();
    }

    @SuppressWarnings( "deprecation" )
    private void processLocales()
    {
        if ( this.locales != null )
        {
            if ( this.mergeResourceBundle == null )
            {
                getLog().warn( "Not defined if locales should be merged or not" );
                return;
            }

            if ( this.mergeResourceBundle )
            {
                this.compiledLocales = locales;
            }
            else
            {
                this.runtimeLocales = locales;
            }
        }
    }

    private void validateLocales( String... locales )
        throws MojoExecutionException
    {
        if ( locales == null )
        {
            return;
        }

        for ( String locale : locales )
        {
            MavenUtils.getLocaleResourcePath( resourceBundlePath, locale );
        }
    }

    protected List<File> getValidSourceRoots( List<?> sourceRoots )
    {
        List<File> sources = new ArrayList<File>();
        for ( Object sourceRoot : sourceRoots )
        {
            File source = new File( sourceRoot.toString() );
            if ( source.exists() )
            {
                sources.add( source );
            }
        }
        return sources;
    }

    @SuppressWarnings( "unchecked" )
    @FlexCompatibility( minVersion = "3" )
    protected void setMavenPathResolver( E builder )
    {
        if ( enableMavenResourcesResolver )
        {
            builder.setPathResolver( new MavenPathResolver( build.getResources() ) );
        }
    }

    private Map<String, String> getLicenses()
        throws MojoExecutionException
    {
        File licensePropertyFile = null;
        for ( File lpl : licensePropertiesLocations )
        {
            if ( lpl.exists() )
            {
                licensePropertyFile = lpl;
                break;
            }
        }

        if ( licensePropertyFile == null )
        {
            return null;
        }

        Properties props = new Properties();
        try
        {
            props.load( new FileInputStream( licensePropertyFile ) );
        }
        catch ( IOException e )
        {
            getLog().warn( "Unable to read license files " + licensePropertyFile.getAbsolutePath(), e );
            return null;
        }

        Map<String, String> licenses = new HashMap<String, String>();

        Enumeration<?> names = props.propertyNames();
        while ( names.hasMoreElements() )
        {
            String name = (String) names.nextElement();
            String value = props.getProperty( name );
            licenses.put( name, value );
        }

        return licenses;
    }

    /**
     * Perform compilation of Flex source
     */
    public void run()
        throws MojoExecutionException, MojoFailureException
    {
        builder.setLogger( new CompileLogger( getLog() ) );

        builder.setConfiguration( configuration );

        build( builder, true );
    }

    /**
     * Writes compilation data to a file to support incremental compilation
     * 
     * @return OutputStream with compilation data
     * @throws FileNotFoundException
     */
    private OutputStream saveCompilationData()
        throws FileNotFoundException
    {
        return new BufferedOutputStream( new FileOutputStream( compilationData ) );
    }

    /**
     * Loads compilation data to support incremental compilation
     * 
     * @return InputStream of compilation data
     * @throws FileNotFoundException
     */
    private InputStream loadCompilationData()
        throws FileNotFoundException
    {
        return new BufferedInputStream( new FileInputStream( compilationData ) );
    }

    /**
     * Setup builder configuration
     * 
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    protected void configure()
        throws MojoExecutionException, MojoFailureException
    {
        resolveDependencies();

        configuration.enableAccessibility( accessible );
        configuration.allowSourcePathOverlap( allowSourcePathOverlap );
        configuration.useActionScript3( as3 );
        configuration.enableDebugging( isDebug(), debugPassword );
        configuration.useECMAScript( es );

        // Fonts
        if ( fonts != null )
        {
            configureFontsAntiAliasing();
            enableFlashType();
            configuration.setFontManagers( fonts.getManagers() );
            configuration.setMaximumCachedFonts( fonts.getMaxCachedFonts() );
            configuration.setMaximumGlyphsPerFace( fonts.getMaxGlyphsPerFace() );
            if ( fonts.getLanguages() != null && !fonts.getLanguages().isEmpty() )
            {
                for ( String language : fonts.getLanguages().keySet() )
                {
                    configuration.setFontLanguageRange( language, fonts.getLanguages().get( language ) );
                }
            }
        }
        File fontsSnapshot = getFontsSnapshot();
        if ( fontsSnapshot == null || !fontsSnapshot.exists() )
        {
            throw new MojoExecutionException( "LocalFontSnapshot not found " + fontsSnapshot );
        }
        configuration.setLocalFontSnapshot( fontsSnapshot );

        configuration.setActionScriptMetadata( keepAs3Metadatas );
        configuration.keepCompilerGeneratedActionScript( keepGeneratedActionscript );

        if ( licenses != null )
        {
            for ( String licenseName : licenses.keySet() )
            {
                String key = licenses.get( licenseName );
                configuration.setLicense( licenseName, key );
            }
        }

        addDefines();

        if ( compiledLocales == null && runtimeLocales == null && isApplication() )
        {
            setLocales( getDefaultLocale() );
        }
        else if ( compiledLocales != null )
        {
            setLocales( compiledLocales );
        }
        else
        {
            // When using the resource-bundle-list option, you must also set the
            // value of the locale option to an empty string.
            setLocales( new String[0] );
        }

        // Add namespaces from FDK
        List<Namespace> fdkNamespaces = MavenUtils.getFdkNamespaces( getDependencyArtifacts(), build );
        if ( this.namespaces != null )
        {
            fdkNamespaces.addAll( Arrays.asList( this.namespaces ) );
        }
        this.namespaces = fdkNamespaces.toArray( new Namespace[0] );

        if ( namespaces != null )
        {
            for ( Namespace namespace : namespaces )
            {
                File manifest = namespace.getManifest();
                configuration.setComponentManifest( namespace.getUri(), manifest );
            }
        }

        configuration.optimize( optimize );
        if ( this.warnings != null )
        {
            configureWarnings( configuration );
        }

        configuration.setSourcePath( sourcePaths );
        configuration.enableStrictChecking( strict );
        configuration.useNetwork( useNetwork );
        configuration.enableVerboseStacktraces( verboseStacktraces );

        if ( contextRoot != null )
        {
            configuration.setContextRoot( contextRoot );
        }
        configuration.keepLinkReport( linkReport );
        configuration.keepConfigurationReport( configurationReport );
        configuration.setServiceConfiguration( services );

        if ( loadExterns != null )
        {
            List<File> externsFiles = new ArrayList<File>();

            for ( MavenArtifact mvnArtifact : loadExterns )
            {
                Artifact artifact =
                    artifactFactory.createArtifactWithClassifier( mvnArtifact.getGroupId(),
                                                                  mvnArtifact.getArtifactId(),
                                                                  mvnArtifact.getVersion(), "xml", "link-report" );
                MavenUtils.resolveArtifact( artifact, resolver, localRepository, remoteRepositories );
                externsFiles.add( artifact.getFile() );
            }
            configuration.setExterns( externsFiles.toArray( new File[externsFiles.size()] ) );

        }

        if ( rawMetadata != null )
        {
            configuration.setSWFMetaData( rawMetadata );
        }

        if ( metadata != null )
        {
            if ( metadata.getContributor() != null )
            {
                configuration.setSWFMetaData( Configuration.CONTRIBUTOR, metadata.getContributor() );
            }

            if ( metadata.getCreator() != null )
            {
                configuration.setSWFMetaData( Configuration.CREATOR, metadata.getCreator() );
            }

            if ( metadata.getDate() != null )
            {
                configuration.setSWFMetaData( Configuration.DATE, metadata.getDate() );
            }

            if ( metadata.getDescriptions() != null )
            {
                configuration.setSWFMetaData( Configuration.DESCRIPTION, metadata.getDescriptions() );
            }

            if ( metadata.getTitles() != null )
            {
                configuration.setSWFMetaData( Configuration.TITLE, metadata.getTitles() );
            }

            if ( metadata.getLanguage() != null )
            {
                configuration.setSWFMetaData( Configuration.LANGUAGE, metadata.getLanguage() );
            }
        }

        setCompatibilityMode();

        configuration.setActionScriptFileEncoding( encoding );

        setTargetPlayer();

        if ( defaultsCss != null )
            configuration.setDefaultCSS( defaultsCss );

        configuration.setDefaultBackgroundColor( Integer.parseInt( defaultBackgroundColor, 16 ) );

        configuration.setDefaultFrameRate( defaultFrameRate );

        configuration.setDefaultScriptLimits( scriptMaxRecursionDepth, scriptMaxExecutionTime );

        configuration.setDefaultSize( defaultSizeWidth, defaultSizeHeight );

        if ( externs != null && externs.length > 0 )
        {
            configuration.setExterns( externs );
        }

        if ( frames != null && frames.length > 0 )
        {
            for ( FrameLabel frame : frames )
            {
                configuration.setFrameLabel( frame.getLabel(), frame.getClassNames() );
            }
        }

        if ( includes != null && includes.length > 0 )
        {
            configuration.setIncludes( includes );
        }

        configuration.useHeadlessServer( headlessServer );

        configuration.keepAllTypeSelectors( keepAllTypeSelectors );

        configuration.useResourceBundleMetaData( useResourceBundleMetadata );

        if ( configFile != null )
        {
            configuration.setConfiguration( configFile );
        }

        if ( configuration instanceof OEMConfiguration )
        {
            // http://bugs.adobe.com/jira/browse/SDK-15581
            // http://bugs.adobe.com/jira/browse/SDK-18719
            // workaround

            OEMConfiguration oemConfig = (OEMConfiguration) configuration;
            List<String> commandLineArguments = new ArrayList<String>();

            if ( staticLinkRuntimeSharedLibraries )
            {
                commandLineArguments.add( "-static-link-runtime-shared-libraries=true" );
            }

            if ( includeResourceBundles != null )
            {
                oemConfig.addIncludeResourceBundles( includeResourceBundles );
            }

            if ( includeResourceBundlesArtifact != null )
            {
                for ( MavenArtifact mvnArtifact : includeResourceBundlesArtifact )
                {
                    Artifact artifact =
                        artifactFactory.createArtifactWithClassifier( mvnArtifact.getGroupId(),
                                                                      mvnArtifact.getArtifactId(),
                                                                      mvnArtifact.getVersion(), "properties",
                                                                      "resource-bundle" );
                    MavenUtils.resolveArtifact( artifact, resolver, localRepository, remoteRepositories );
                    String bundleFile;
                    try
                    {
                        bundleFile = FileUtils.readFileToString( artifact.getFile() );
                    }
                    catch ( IOException e )
                    {
                        throw new MojoExecutionException( "Ocorreu um erro ao ler o artefato " + artifact, e );
                    }
                    String[] bundles = bundleFile.split( " " );
                    oemConfig.addIncludeResourceBundles( bundles );
                }
            }

            if ( configFile == null )
            {
                commandLineArguments.add( "-load-config=" );
            }

            if ( includeLookupOnly )
            {
                commandLineArguments.add( "-include-lookup-only" );
            }

            oemConfig.setConfiguration( commandLineArguments.toArray( new String[commandLineArguments.size()] ) );
        }
        else
        {
            throw new MojoFailureException( "Flex-compiler API change, unable to use suggested 'solutions'!" );
        }

        verifyDigests();
    }

    protected abstract String getDefaultLocale();

    protected abstract boolean isApplication();

    /**
     * @return if should be compiled as debug
     */
    protected abstract boolean isDebug();

    protected void resolveDependencies()
        throws MojoExecutionException, MojoFailureException
    {
        configuration.setExternalLibraryPath( getGlobalDependency() );
        configuration.addExternalLibraryPath( getDependenciesPath( EXTERNAL ) );

        configuration.includeLibraries( getDependenciesPath( INTERNAL ) );

        configuration.setLibraryPath( getDependenciesPath( Artifact.SCOPE_COMPILE ) );
        configuration.addLibraryPath( getDependenciesPath( MERGED ) );

        if ( compiledLocales == null && runtimeLocales == null && isApplication() )
        {
            configuration.addLibraryPath( getResourcesBundles( getDefaultLocale() ) );
        }
        else if ( compiledLocales != null )
        {
            configuration.addLibraryPath( getResourcesBundles( compiledLocales ) );
        }

        resolveRuntimeLibraries();

        configuration.setTheme( getThemes() );
    }

    protected File[] getThemes()
        throws MojoExecutionException, MojoFailureException
    {
        List<File> themeFiles = new ArrayList<File>();

        if ( this.themes != null )
        {
            for ( String theme : themes )
            {
                File themeFile = MavenUtils.resolveResourceFile( project, theme );
                themeFiles.add( themeFile );
            }
        }

        themeFiles.addAll( Arrays.asList( getDependenciesPath( "theme" ) ) );

        if ( themeFiles.isEmpty() )
        {
            return null;
        }

        return themeFiles.toArray( new File[0] );
    }

    @SuppressWarnings( "deprecation" )
    @FlexCompatibility( maxVersion = "2" )
    private void enableFlashType()
    {
        configuration.enableFlashType( fonts.isFlashType() );
    }

    @FlexCompatibility( minVersion = "3" )
    private void verifyDigests()
    {
        configuration.enableDigestVerification( verifyDigests );
    }

    @FlexCompatibility( minVersion = "3" )
    private void setTargetPlayer()
        throws MojoExecutionException
    {
        if ( targetPlayer != null )
        {
            String[] nodes = targetPlayer.split( "\\." );
            if ( nodes.length != 3 )
            {
                throw new MojoExecutionException( "Invalid player version " + targetPlayer );
            }
            int[] versions = new int[nodes.length];
            for ( int i = 0; i < nodes.length; i++ )
            {
                try
                {
                    versions[i] = Integer.parseInt( nodes[i] );
                }
                catch ( NumberFormatException e )
                {
                    throw new MojoExecutionException( "Invalid player version " + targetPlayer );
                }
            }
            if ( versions[0] < 9 )
            {
                throw new MojoExecutionException( "Invalid player version " + targetPlayer );
            }
            configuration.setTargetPlayer( versions[0], versions[1], versions[2] );
        }
    }

    @FlexCompatibility( minVersion = "3" )
    private void setCompatibilityMode()
        throws MojoExecutionException
    {
        if ( compatibilityVersion != null )
        {
            if ( !COMPATIBILITY_2_0_0.equals( compatibilityVersion )
                && !COMPATIBILITY_2_0_1.equals( compatibilityVersion )
                && !COMPATIBILITY_3_0_0.equals( compatibilityVersion ) )
            {
                throw new MojoExecutionException( "Invalid compatibility version " + compatibilityVersion );
            }
            else if ( COMPATIBILITY_2_0_0.equals( compatibilityVersion ) )
            {
                configuration.setCompatibilityVersion( 2, 0, 0 );
            }
            else if ( COMPATIBILITY_2_0_1.equals( compatibilityVersion ) )
            {
                configuration.setCompatibilityVersion( 2, 0, 1 );
            }
            else if ( COMPATIBILITY_3_0_0.equals( compatibilityVersion ) )
            {
                configuration.setCompatibilityVersion( 3, 0, 0 );
            }
            else
            {
                throw new IllegalStateException( "Should never reach this" );
            }
        }
    }

    protected void setLocales( String... locales )
        throws MojoExecutionException
    {
        setLocales2( locales );
        setLocales3( locales );
    }

    @FlexCompatibility( minVersion = "3" )
    private void setLocales3( String[] locales )
    {
        configuration.setLocale( locales );
    }

    @SuppressWarnings( "deprecation" )
    @FlexCompatibility( maxVersion = "2" )
    private void setLocales2( String[] locales )
        throws MojoExecutionException
    {
        if ( locales.length == 1 )
        {
            configuration.setLocale( new Locale( locales[0] ) );
        }
        else if ( locales.length != 0 )
        {
            throw new MojoExecutionException( "Only one locale is allowed" );
        }
    }

    @SuppressWarnings( "deprecation" )
    @FlexCompatibility( minVersion = "3" )
    private void addDefines()
        throws MojoExecutionException
    {
        ExpressionEvaluator expressionEvaluator =
            new PluginParameterExpressionEvaluator( context, execution, null, null, project, project.getProperties() );

        if ( defines != null )
        {
            if ( definesDeclaration == null )
            {
                definesDeclaration = new Properties();
            }
            definesDeclaration.putAll( defines );
        }

        if ( definesDeclaration != null )
        {
            for ( Object definekey : definesDeclaration.keySet() )
            {
                String defineName = definekey.toString();
                String value = definesDeclaration.getProperty( defineName );
                if ( value.contains( "${" ) )
                {
                    // Fix bug in maven which doesn't always evaluate ${} constructions
                    try
                    {
                        value = (String) expressionEvaluator.evaluate( value );
                    }
                    catch ( ExpressionEvaluationException e )
                    {
                        throw new MojoExecutionException( "Expression error in " + defineName, e );
                    }
                }
                getLog().debug( "define " + defineName + " = " + value );
                configuration.addDefineDirective( defineName, value );
            }
        }
    }

    @FlexCompatibility( minVersion = "3" )
    private void configureFontsAntiAliasing()
    {
        configuration.enableAdvancedAntiAliasing( fonts.isAdvancedAntiAliasing() );
    }

    protected File[] getGlobalDependency()
        throws MojoExecutionException
    {
        Set<Artifact> dependencies = getDependencyArtifacts();
        for ( Artifact artifact : dependencies )
        {
            if ( "playerglobal".equals( artifact.getArtifactId() ) || //
                "airglobal".equals( artifact.getArtifactId() ) )
            {
                return new File[] { MavenUtils.getArtifactFile( artifact, build ) };
            }
        }

        throw new MojoExecutionException( "Player/Air Global dependency not found." );
    }

    /**
     * Resolves all runtime libraries, that includes RSL and framework CACHING
     * 
     * @throws MojoExecutionException
     */
    private void resolveRuntimeLibraries()
        throws MojoExecutionException
    {
        List<Artifact> rsls = getDependencyArtifacts( RSL, CACHING );
        rslsSort( rsls );

        for ( Artifact artifact : rsls )
        {
            String scope = artifact.getScope();
            File artifactFile = artifact.getFile();
            String artifactPath = artifactFile.getAbsolutePath();
            String extension;
            if ( CACHING.equals( scope ) )
            {
                extension = SWZ;
            }
            else
            {
                extension = SWF;
            }
            String[] rslUrls = getRslUrls( artifact, extension );
            String[] rslPolicyFileUrls = getRslPolicyFileUrls( artifact );
            configuration.addRuntimeSharedLibraryPath( artifactPath, rslUrls, rslPolicyFileUrls );

            // when -static-link-runtime-shared-libraries=true ignore -runtime-shared-library-path,
            // not put all RSLs to -library-path (tested on 3.2.0.3958 and 4.0.0.4600)
            configuration.addLibraryPath( new File[] { artifactFile } );
        }
    }

    public void rslsSort( List<Artifact> rslArtifacts )
        throws MojoExecutionException
    {
        Map<Artifact, List<Artifact>> dependencies = getDependencies( rslArtifacts );

        List<Artifact> ordered = new ArrayList<Artifact>();
        for ( Artifact a : rslArtifacts )
        {
            if ( dependencies.get( a ) == null || dependencies.get( a ).isEmpty() )
            {
                ordered.add( a );
            }
        }
        rslArtifacts.removeAll( ordered );

        while ( !rslArtifacts.isEmpty() )
        {
            int original = rslArtifacts.size();
            for ( Artifact a : rslArtifacts )
            {
                List<Artifact> deps = dependencies.get( a );
                if ( ordered.containsAll( deps ) )
                {
                    ordered.add( a );
                }
            }
            rslArtifacts.removeAll( ordered );
            if ( original == rslArtifacts.size() )
            {
                throw new MojoExecutionException( "Unable to resolve " + rslArtifacts );
            }
        }

        rslArtifacts.addAll( ordered );
    }

    @SuppressWarnings( "unchecked" )
    private Map<Artifact, List<Artifact>> getDependencies( List<Artifact> rslArtifacts )
        throws MojoExecutionException
    {
        Map<Artifact, List<Artifact>> dependencies = new HashMap<Artifact, List<Artifact>>();

        for ( Artifact pomArtifact : rslArtifacts )
        {
            try
            {
                MavenProject pomProject =
                    mavenProjectBuilder.buildFromRepository( pomArtifact, remoteRepositories, localRepository );
                Set pomArtifacts = pomProject.createArtifacts( artifactFactory, null, null );
                ArtifactResolutionResult arr =
                    resolver.resolveTransitively( pomArtifacts, pomArtifact, remoteRepositories, localRepository,
                                                  artifactMetadataSource );
                List<Artifact> artifactDependencies = new ArrayList<Artifact>( arr.getArtifacts() );
                artifactDependencies = removeNonRSLDependencies( rslArtifacts, artifactDependencies );
                dependencies.put( pomArtifact, artifactDependencies );
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }
        return dependencies;
    }

    private List<Artifact> removeNonRSLDependencies( List<Artifact> rslArtifacts, List<Artifact> artifactDependencies )
    {
        List<Artifact> cleanArtifacts = new ArrayList<Artifact>();
        artifacts: for ( Artifact artifact : artifactDependencies )
        {
            for ( Artifact rslArtifact : rslArtifacts )
            {
                if ( artifact.getGroupId().equals( rslArtifact.getGroupId() )
                    && artifact.getArtifactId().equals( rslArtifact.getArtifactId() )
                    && artifact.getType().equals( rslArtifact.getType() ) )
                {
                    cleanArtifacts.add( rslArtifact );
                    continue artifacts;
                }
            }
        }
        return cleanArtifacts;
    }

    /**
     * Gets RslPolicyFileUrls for given artifact
     * 
     * @param artifact
     * @return Array of urls
     */
    private String[] getRslPolicyFileUrls( Artifact artifact )
    {
        String[] domains = new String[policyFileUrls.length];
        for ( int i = 0; i < policyFileUrls.length; i++ )
        {
            String domain = policyFileUrls[i];
            if ( contextRoot != null )
            {
                domain = domain.replace( "{contextRoot}", contextRoot );
            }
            domain = domain.replace( "{groupId}", artifact.getGroupId() );
            domain = domain.replace( "{artifactId}", artifact.getArtifactId() );
            domain = domain.replace( "{version}", artifact.getVersion() );
            domains[i] = domain;
        }
        return domains;
    }

    /**
     * Get RslUrls
     * 
     * @param artifact
     * @param extension
     * @return Array of url's
     */
    private String[] getRslUrls( Artifact artifact, String extension )
    {
        String[] rsls = new String[rslUrls.length];
        for ( int i = 0; i < rslUrls.length; i++ )
        {
            String rsl = rslUrls[i];
            if ( contextRoot == null || "".equals( contextRoot ) )
            {
                rsl = rsl.replace( "/{contextRoot}/", "" );
            }
            else
            {
                rsl = rsl.replace( "{contextRoot}", contextRoot );
            }
            rsl = MavenUtils.getRslUrl( rsl, artifact, extension );
            rsls[i] = rsl;
        }
        return rsls;
    }

    protected File getRuntimeLocaleOutputFile( String locale, String extension )
    {
        String path = runtimeLocaleOutputPath.replace( "/{contextRoot}", project.getBuild().getDirectory() );
        File output =
            new File( MavenUtils.getRuntimeLocaleOutputPath( path, project.getArtifact(), locale, extension ) );
        output.getParentFile().mkdirs();

        return output;
    }

    /**
     * Get Fonts snapshot
     * 
     * @return File of font snapshot
     * @throws MojoExecutionException
     */
    protected File getFontsSnapshot()
        throws MojoExecutionException
    {
        if ( fonts != null && fonts.getLocalFontsSnapshot() != null )
        {
            return fonts.getLocalFontsSnapshot();
        }
        else
        {
            getLog().debug( "No fonts snapshot found, generating one!" );
            return MavenUtils.getFontsFile( build );
        }
    }

    /**
     * Get resource bundles for the given locale
     * 
     * @param requestedLocales the locale for which you want bundles, null for all locales
     * @return Array of resource bundle files
     * @throws MojoExecutionException
     */
    protected File[] getResourcesBundles( String... requestedLocales )
        throws MojoExecutionException
    {
        if ( requestedLocales == null )
        {
            return new File[0];
        }

        List<File> resourceBundles = new ArrayList<File>();

        for ( Artifact resourceBundleBeacon : getDependencyArtifacts() )
        {
            if ( !RB_SWC.equals( resourceBundleBeacon.getType() ) )
            {
                continue;
            }

            // resouceBundles.add(artifact.getFile());
            for ( String requestLocale : requestedLocales )
            {
                Artifact resolvedResourceBundle =
                    artifactFactory.createArtifactWithClassifier( resourceBundleBeacon.getGroupId(),
                                                                  resourceBundleBeacon.getArtifactId(),
                                                                  resourceBundleBeacon.getVersion(),
                                                                  resourceBundleBeacon.getType(), requestLocale );

                MavenUtils.resolveArtifact( resolvedResourceBundle, resolver, localRepository, remoteRepositories );
                resourceBundles.add( resolvedResourceBundle.getFile() );
            }

        }
        getLog().debug( "getResourcesBundles(" + requestedLocales + ") returning resourceBundles: " + resourceBundles );
        return resourceBundles.toArray( new File[resourceBundles.size()] );
    }

    /**
     * Get array of files for dependency artifacts for given scope
     * 
     * @param scope for which to get files
     * @return Array of dependency artifact files
     * @throws MojoExecutionException
     */
    protected File[] getDependenciesPath( String scope )
        throws MojoExecutionException
    {
        if ( scope == null )
            return null;

        List<File> files = new ArrayList<File>();
        for ( Artifact a : getDependencyArtifacts( scope ) )
        {
            if ( "playerglobal".equals( a.getArtifactId() ) || //
                "airglobal".equals( a.getArtifactId() ) )
            {
                continue;
            }
            files.add( a.getFile() );
        }
        return files.toArray( new File[files.size()] );
    }

    /**
     * Perform actions after compilation has run
     */
    @Override
    protected void tearDown()
        throws MojoExecutionException, MojoFailureException
    {
        if ( isSetProjectFile )
        {
            project.getArtifact().setFile( getOutput() );
        }
        Report report = builder.getReport();
        if ( linkReport )
        {
            writeLinkReport( report );
        }
        if ( configurationReport )
        {
            writeConfigurationReport( report );
        }
        if ( runtimeLocales != null )
        {
            writeResourceBundle( report );
        }

    }

    /**
     * Write a resource bundle
     * 
     * @param report from which to obtain info about resource bundle
     * @throws MojoExecutionException
     */
    private void writeResourceBundle( Report report )
        throws MojoExecutionException
    {
        getLog().info( "Compiling resources bundles!" );

        String[] bundles = report.getResourceBundleNames();

        if ( bundles == null || bundles.length == 0 )
        {
            getLog().warn( "Resource-bundle generation fail: No resource-bundle found." );
            return;
        }

        // install resource bundle beacon
        try
        {
            File tempFile = File.createTempFile( build.getFinalName(), ".rb.swc" );
            tempFile.deleteOnExit();
            FileUtils.copyURLToFile( getClass().getResource( "/rb.swc" ), tempFile );
            getLog().info( "Installing resource bundle beacon: " + tempFile );
            projectHelper.attachArtifact( project, "resource-bundle", tempFile );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to create beacon resource bundle. " + build.getFinalName()
                + ".rb.swc", e );
        }

        for ( String locale : runtimeLocales )
        {
            getLog().info( "Generating resource bundle for locale: " + locale );
            File localePath = MavenUtils.getLocaleResourcePath( resourceBundlePath, locale );

            if ( !localePath.exists() )
            {
                getLog().error( "Unable to find locales path: " + localePath.getAbsolutePath() );
                continue;
            }
            writeResourceBundle( bundles, locale, localePath );
        }
    }

    /**
     * Write resource bundle
     * 
     * @param bundles
     * @param locale
     * @param localePath
     * @throws MojoExecutionException
     */
    protected abstract void writeResourceBundle( String[] bundles, String locale, File localePath )
        throws MojoExecutionException;

    /**
     * Configure warnings
     * 
     * @param cfg Configuration instance to configure
     */
    private void configureWarnings( Configuration cfg )
    {
        if ( !warnings.getActionScript() || !showWarnings )
        {
            cfg.showActionScriptWarnings( false );
        }
        if ( !warnings.getBinding() || !showWarnings )
        {
            cfg.showBindingWarnings( false );
        }
        if ( !warnings.getDeprecation() || !showWarnings )
        {
            cfg.showDeprecationWarnings( false );
        }
        if ( !warnings.getUnusedTypeSelector() || !showWarnings )
        {
            cfg.showUnusedTypeSelectorWarnings( false );
        }

        if ( !showWarnings )
        {
            return;
        }

        configureWarningIfTrue( Configuration.WARN_ARRAY_TOSTRING_CHANGES, warnings.getArrayTostringChanges(), cfg );
        configureWarningIfFalse( Configuration.WARN_ASSIGNMENT_WITHIN_CONDITIONAL,
                                 warnings.getAssignmentWithinConditional(), cfg );
        configureWarningIfFalse( Configuration.WARN_BAD_ARRAY_CAST, warnings.getBadArrayCast(), cfg );

        configureWarningIfFalse( Configuration.WARN_BAD_BOOLEAN_ASSIGNMENT, warnings.getBadBooleanAssignment(), cfg );
        configureWarningIfFalse( Configuration.WARN_BAD_DATE_CAST, warnings.getBadDateCast(), cfg );
        configureWarningIfFalse( Configuration.WARN_BAD_ES3_TYPE_METHOD, warnings.getBadEs3TypeMethod(), cfg );
        configureWarningIfFalse( Configuration.WARN_BAD_ES3_TYPE_PROP, warnings.getBadEs3TypeProp(), cfg );
        configureWarningIfFalse( Configuration.WARN_BAD_NAN_COMPARISON, warnings.getBadNanComparison(), cfg );
        configureWarningIfFalse( Configuration.WARN_BAD_NULL_ASSIGNMENT, warnings.getBadNullAssignment(), cfg );
        configureWarningIfFalse( Configuration.WARN_BAD_NULL_COMPARISON, warnings.getBadNullComparison(), cfg );
        configureWarningIfFalse( Configuration.WARN_BAD_UNDEFINED_COMPARISON, warnings.getBadUndefinedComparison(), cfg );
        configureWarningIfTrue( Configuration.WARN_BOOLEAN_CONSTRUCTOR_WITH_NO_ARGS,
                                warnings.getBooleanConstructorWithNoArgs(), cfg );
        configureWarningIfTrue( Configuration.WARN_CHANGES_IN_RESOLVE, warnings.getChangesInResolve(), cfg );
        configureWarningIfFalse( Configuration.WARN_CLASS_IS_SEALED, warnings.getClassIsSealed(), cfg );
        configureWarningIfFalse( Configuration.WARN_CONST_NOT_INITIALIZED, warnings.getConstNotInitialized(), cfg );
        configureWarningIfTrue( Configuration.WARN_CONSTRUCTOR_RETURNS_VALUE, warnings.getConstructorReturnsValue(),
                                cfg );
        configureWarningIfTrue( Configuration.WARN_DEPRECATED_EVENT_HANDLER_ERROR,
                                warnings.getDeprecatedEventHandlerError(), cfg );
        configureWarningIfFalse( Configuration.WARN_DEPRECATED_FUNCTION_ERROR, warnings.getDeprecatedFunctionError(),
                                 cfg );
        configureWarningIfFalse( Configuration.WARN_DEPRECATED_PROPERTY_ERROR, warnings.getDeprecatedPropertyError(),
                                 cfg );
        configureWarningIfFalse( Configuration.WARN_DUPLICATE_ARGUMENT_NAMES, warnings.getDuplicateArgumentNames(), cfg );
        configureWarningIfFalse( Configuration.WARN_DUPLICATE_VARIABLE_DEF, warnings.getDuplicateVariableDef(), cfg );
        configureWarningIfTrue( Configuration.WARN_FOR_VAR_IN_CHANGES, warnings.getForVarInChanges(), cfg );
        configureWarningIfFalse( Configuration.WARN_IMPORT_HIDES_CLASS, warnings.getImportHidesClass(), cfg );
        configureWarningIfFalse( Configuration.WARN_INSTANCEOF_CHANGES, warnings.getInstanceOfChanges(), cfg );
        configureWarningIfFalse( Configuration.WARN_INTERNAL_ERROR, warnings.getInternalError(), cfg );
        configureWarningIfFalse( Configuration.WARN_LEVEL_NOT_SUPPORTED, warnings.getLevelNotSupported(), cfg );
        configureWarningIfFalse( Configuration.WARN_MISSING_NAMESPACE_DECL, warnings.getMissingNamespaceDecl(), cfg );
        configureWarningIfFalse( Configuration.WARN_NEGATIVE_UINT_LITERAL, warnings.getNegativeUintLiteral(), cfg );
        configureWarningIfFalse( Configuration.WARN_NO_CONSTRUCTOR, warnings.getNoConstructor(), cfg );
        configureWarningIfTrue( Configuration.WARN_NO_EXPLICIT_SUPER_CALL_IN_CONSTRUCTOR,
                                warnings.getNoExplicitSuperCallInConstructor(), cfg );
        configureWarningIfFalse( Configuration.WARN_NO_TYPE_DECL, warnings.getNoTypeDecl(), cfg );
        configureWarningIfTrue( Configuration.WARN_NUMBER_FROM_STRING_CHANGES, warnings.getNumberFromStringChanges(),
                                cfg );
        configureWarningIfTrue( Configuration.WARN_SCOPING_CHANGE_IN_THIS, warnings.getScopingChangeInThis(), cfg );
        configureWarningIfFalse( Configuration.WARN_SLOW_TEXTFIELD_ADDITION, warnings.getSlowTextFieldAddition(), cfg );
        configureWarningIfFalse( Configuration.WARN_UNLIKELY_FUNCTION_VALUE, warnings.getUnlikelyFunctionValue(), cfg );
        configureWarningIfTrue( Configuration.WARN_XML_CLASS_HAS_CHANGED, warnings.getXmlClassHasChanged(), cfg );

        configureWarnings3( cfg );
    }

    private void configureWarningIfTrue( int code, boolean value, Configuration cfg )
    {
        if ( value )
        {
            cfg.checkActionScriptWarning( code, true );
        }
    }

    private void configureWarningIfFalse( int code, boolean value, Configuration cfg )
    {
        if ( !value )
        {
            cfg.checkActionScriptWarning( code, false );
        }
    }

    @FlexCompatibility( minVersion = "3" )
    private void configureWarnings3( Configuration cfg )
    {
        if ( !warnings.getShadowedDeviceFont() )
        {
            cfg.showShadowedDeviceFontWarnings( warnings.getShadowedDeviceFont() );
        }
    }

    /**
     * Writes configuration report to file
     * 
     * @param report contains info to write
     * @throws MojoExecutionException throw if an error occurs during writing of report to file
     */
    private void writeLinkReport( Report report )
        throws MojoExecutionException
    {

        writeReport( report, REPORT_LINK );
    }

    /**
     * Writes configuration report to file
     * 
     * @param report contains info to write
     * @throws MojoExecutionException throw if an error occurs during writing of report to file
     */
    private void writeConfigurationReport( Report report )
        throws MojoExecutionException
    {
        writeReport( report, REPORT_CONFIG );
    }

    /**
     * Writes a report to a file.
     * 
     * @param report Report containing info to write to file
     * @param type Type of report to write. Valid types are <code>link</code> and <code>config</code>.
     * @throws MojoExecutionException throw if an error occurs during writing of report to file
     */
    protected void writeReport( Report report, String type )
        throws MojoExecutionException
    {
        File fileReport =
            new File( build.getDirectory(), project.getArtifactId() + "-" + project.getVersion() + "-" + type
                + "-report.xml" );

        try
        {
            StringWriter writer = new StringWriter();
            if ( REPORT_LINK.equals( type ) )
            {
                report.writeLinkReport( writer );
                linkReportFile = fileReport;
                FileUtils.writeStringToFile( fileReport, writer.toString() );
            }
            else if ( REPORT_CONFIG.equals( type ) )
            {
                report.writeConfigurationReport( writer );
                FlexConfigBuilder configBuilder = new FlexConfigBuilder( writer.toString() );
                fixConfigReport( configBuilder );
                configBuilder.write( fileReport );
            }

            getLog().info( "Written " + type + " report to " + fileReport );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "An error has ocurried while recording " + type + "-report", e );
        }

        if ( !( REPORT_CONFIG.equals( type ) ) )
        {
            projectHelper.attachArtifact( project, "xml", type + "-report", fileReport );
        }
    }

    @SuppressWarnings( "unchecked" )
    protected void fixConfigReport( FlexConfigBuilder configBuilder )
    {
        configBuilder.addOutput( getOutput() );

        if ( compiledLocales == null )
        {
            configBuilder.addEmptyLocale();
        }

        for ( Resource resource : (List<Resource>) project.getResources() )
        {
            File resourceDirectory = new File( resource.getDirectory() );
            if ( resourceDirectory.exists() )
            {
                configBuilder.addSourcePath( resourceDirectory );
            }
        }
    }

    protected void build( E builder, boolean printConfigurations )
        throws MojoExecutionException
    {
        if ( !isCompilationRequired() )
        {
            return;
        }

        long bytes;
        if ( printConfigurations )
        {
            getLog().info( "Flex compiler configurations:" + configuration.toString().replace( "--", "\n-" ) );
        }
        else
        {
            getLog().debug( "Flex compiler configurations:" + configuration.toString().replace( "--", "\n-" ) );
        }

        try
        {

            if ( incremental && compilationData.exists() )
            {
                builder.load( loadCompilationData() );
            }
            bytes = builder.build( incremental );
            if ( incremental )
            {
                if ( compilationData.exists() )
                {
                    compilationData.delete();
                    compilationData.createNewFile();
                }

                builder.save( saveCompilationData() );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        if ( bytes == 0 )
        {
            throw new MojoExecutionException( "Error compiling!" );
        }
    }

    @SuppressWarnings( "unchecked" )
    private boolean isCompilationRequired()
        throws MojoExecutionException
    {
        if ( !quick )
        {
            // not running at quick mode
            return true;
        }

        Artifact artifact =
            artifactFactory.createArtifact( project.getGroupId(), project.getArtifactId(), project.getVersion(), null,
                                            project.getPackaging() );
        try
        {
            resolver.resolve( artifact, remoteRepositories, localRepository );
        }
        catch ( AbstractArtifactResolutionException e )
        {
            // Not available at repository
            return true;
        }

        File artifactFile = artifact.getFile();
        if ( artifactFile == null || !artifactFile.exists() )
        {
            // Recompile, file doesn't exists
            getLog().warn( "Can't find any older instaled version." );
            return true;
        }
        try
        {
            FileUtils.copyFile( artifactFile, getOutput() );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to copy instaled version to target folder.", e );
        }
        long lastCompiledArtifact = artifactFile.lastModified();

        Set<Artifact> dependencies = getDependencyArtifacts();
        for ( Artifact dependency : dependencies )
        {
            if ( FileUtils.isFileNewer( dependency.getFile(), lastCompiledArtifact ) )
            {
                // a dependency is newer, recompile
                getLog().warn( "Found a updated dependency: " + dependency );
                return true;
            }
        }

        List<File> paths = new ArrayList<File>( Arrays.asList( sourcePaths ) );
        if ( compiledLocales != null )
        {
            // resourceBundlePath is unresolved
            paths.remove( new File( resourceBundlePath ) );
            // resolving it
            for ( String locale : compiledLocales )
            {
                paths.add( MavenUtils.getLocaleResourcePath( resourceBundlePath, locale ) );
            }
        }
        if ( runtimeLocales != null )
        {
            // resolving locale
            for ( String locale : runtimeLocales )
            {
                paths.add( MavenUtils.getLocaleResourcePath( resourceBundlePath, locale ) );
            }
        }

        for ( File sourcePath : paths )
        {
            Collection<File> files =
                FileUtils.listFiles( sourcePath, new AgeFileFilter( lastCompiledArtifact, false ),
                                     TrueFileFilter.INSTANCE );

            // If has any newer file
            if ( files.size() > 0 )
            {
                getLog().warn( "Found some updated files." );
                return true;
            }
        }

        // nothing new was found.
        return false;
    }

    public String getFDKVersion()
    {
        Artifact compiler = MavenUtils.searchFor( pluginArtifacts, "com.adobe.flex", "compiler", null, "pom", null );
        return compiler.getVersion();
    }

    protected abstract File getOutput();
}
