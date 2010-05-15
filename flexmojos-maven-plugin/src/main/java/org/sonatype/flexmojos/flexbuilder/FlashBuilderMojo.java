/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.flexbuilder;

import static org.sonatype.flexmojos.common.FlexExtension.AIR;
import static org.sonatype.flexmojos.common.FlexExtension.RB_SWC;
import static org.sonatype.flexmojos.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.common.FlexExtension.SWF;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.ide.IdeDependency;
import org.apache.velocity.VelocityContext;
import org.jvnet.animal_sniffer.IgnoreJRERequirement;
import org.sonatype.flexmojos.common.FlexScopes;
import org.sonatype.flexmojos.compatibilitykit.FlexCompatibility;
import org.sonatype.flexmojos.compatibilitykit.FlexMojo;
import org.sonatype.flexmojos.utilities.HtmlWrapperUtil;
import org.sonatype.flexmojos.utilities.MavenUtils;
import org.sonatype.flexmojos.utilities.Namespace;



/**
 * Extends the standard Flex Builder configuration.
 * 
 * @author Lance Linder (llinder@gmail.com)
 * @since 3.5
 * @extendsPlugin eclipse
 * @extendsGoal eclipse
 * @goal flashbuilder
 * @requiresDependencyResolution
 */
public class FlashBuilderMojo
    extends FlexbuilderMojo implements FlexMojo
{
	/**
	 * Specify which Eclipse Flex SDK to use. This should be the same name as one of the available SDKs
	 * in the Eclipse Flex Preferences. The default is to sense the SDK from the first 3 digits of the
	 * Flex SDK that is in use. If the value of "default" is used it will fall back to the default SDK
	 * specified in the eclipse Flash Builder settings.<BR><BR>
	 * NOTE: No matter which Flex SDK is specified all artifact paths will still point to the maven
	 * repository. What this means is that the Flex SDK specified here will only point to source files and
	 * compiler to use with Flex Builder.
	 * 
	 * @parameter
	 */
	private String flexSDK;

	/**
	 * Specify which version of Flex/Flashbuilder to generate config for
	 * 
	 * @parameter default-value=3
	 */
	private int version;

	/**
	 * Keep the following AS3 metadata in the bytecodes.<BR>
	 * Usage:
	 *
	 * <pre>
	 * &lt;keepAs3Metadatas&gt;
	 * &lt;keepAs3Metadata&gt;Bindable&lt;/keepAs3Metadata&gt;
	 * &lt;keepAs3Metadata&gt;Events&lt;/keepAs3Metadata&gt;
	 * &lt;/keepAs3Metadatas&gt;
	 * </pre>
	 *
	 * @parameter
	 */
	private String[] keepAs3Metadatas;
	
	/**
	 * Specify a URI to associate with a manifest of components for use as MXML elements.<BR>
	 * Usage:
	 *
	 * <pre>
	 * &lt;namespaces&gt;
	 * &lt;namespace&gt;
	 * &lt;uri&gt;http://www.adobe.com/2006/mxml&lt;/uri&gt;
	 * &lt;manifest&gt;${basedir}/manifest.xml&lt;/manifest&gt;
	 * &lt;/namespace&gt;
	 * &lt;/namespaces&gt;
	 * </pre>
	 *
	 * @parameter
	 */
	private Namespace[] namespaces;
	
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
	* This is equivalent to the <code>include-file</code> option of the compc compiler.<BR>
	* Usage:
	*
	* <pre>
	* &lt;includeFiles&gt;
	* &lt;file&gt;${baseDir}/anyFile.txt&lt;/file&gt;
	* &lt;/includeFiles&gt;
	* </pre>
	*
	* @parameter
	*/
	private File[] includeFiles;
	
    /**
	* The template URI. This is the same usage as on the wrapper mojo.
	*
	* To make this mojo copy the template URI to the templateOutputPath
	* generateHtmlWrapper must be set to true.
	*
	* <p>
	* You can point to a zip file, a folder or use one of the following embed templates:
	* <ul>
	* embed:client-side-detection
	* </ul>
	* <ul>
	* embed:client-side-detection-with-history
	* </ul>
	* <ul>
	* embed:express-installation
	* </ul>
	* <ul>
	* embed:express-installation-with-history
	* </ul>
	* <ul>
	* embed:no-player-detection
	* </ul>
	* <ul>
	* embed:no-player-detection-with-history
	* </ul>
	* To point to a zip file you must use a URI like this:
	*
	* <pre>
	* zip:/myTemplateFolder/template.zip
	* zip:c:/myTemplateFolder/template.zip
	* </pre>
	*
	* To point to a folder use a URI like this:
	*
	* <pre>
	* folder:/myTemplateFolder/
	* folder:c:/myTemplateFolder/
	* </pre>
	* <p>
	* Unlike the html wrapper mojo this mojo will only copy the template files
	* to the htmlTemplateOutputPath. From there Flex Builder will work with them
	* as normal.
	*
	* @parameter default-value="embed:express-installation-with-history"
	*/
	private String templateURI;
	
	/**
	* List of CSS or SWC files to apply as a theme. <>BR Usage:
	*
	* <pre>
	* &lt;themes&gt;
	* &lt;theme&gt;css/main.css&lt;/theme&gt;
	* &lt;/themes&gt;
	* </pre>
	*
	* If you are using SWC theme should be better keep it's version controlled, so is advised to use a dependency with
	* theme scope.<BR>
	* Like this:
	*
	* <pre>
	* &lt;dependency&gt;
	* &lt;groupId&gt;com.acme&lt;/groupId&gt;
	* &lt;artifactId&gt;acme-theme&lt;/artifactId&gt;
	* &lt;type&gt;swc&lt;/type&gt;
	* &lt;scope&gt;theme&lt;/scope&gt;
	* &lt;version&gt;1.0&lt;/version&gt;
	* &lt;/dependency&gt;
	* </pre>
	*
	* @parameter
	*/
	private String[] themes;
	 
	/* end duplicated */
	    
	/**
	* Directory path where the html template files will be copied to.
	*
	* Since Flex Builder is hard coded to ${basedir}/html-template there
	* should be no reason to change this.
	*
	* @parameter default-value="${basedir}/html-template"
	*/
	protected File flexbuilderTemplateDirectory;
	
	// START Internal Properties
	
	/**
	 * Internal value that holds Flex Builder IDE dependencies that
	 * where converted from Eclipse IDE dependencies.
	 */
	private List<FbIdeDependency>fbDeps;
	
	/**
     * @parameter expression="${plugin.artifacts}"
     */
    protected List<Artifact> pluginArtifacts;
	
	// END Internal Properties
	
	
	
	/**
	 * Override to add some additional template writing.
	 */
	@Override
    public void writeConfiguration( IdeDependency[] deps )
        throws MojoExecutionException
    {
		// Convert Eclipse IDE dependencies to Flex Builder IDE dependencies with proper scope values.
		this.fbDeps = getFbIdeDependencies( deps );
		
		if ( SWF.equals( packaging ) || SWC.equals( packaging ) || AIR.equals( packaging ) )
		{
			writeFlexConfig( packaging, deps );
		}
		
		if ( SWF.equals( packaging ) )
		{
			writeHtmlTemplate();
		}
		
		// calling the super function must happen last so we can use some values in other
		// templates and then null them out so they are not used by the super write functions.
		super.writeConfiguration( deps );
    }
	
	@Override
	protected VelocityContext getFlexLibPropertiesContext()
	{
		VelocityContext context = super.getFlexLibPropertiesContext();
		context.put( "includeFiles", getResourceEntries( includeFiles ) );
		return context;
	}
	
    /**
     * Override to use different template file.
     */
    @Override
    protected String getFlexLibPropertiesTemplate()
    {
        return "/templates/flexbuilder/flexLibProperties2.vm";
    }

    /**
     * Override to use different template file.
     */
    @Override
    protected String getFlexPropertiesTemplate()
    {
    	return "/templates/flexbuilder/flexProperties2.vm";
    }

    /**
     * Override to use different template file.
     */
    @Override
    protected String getAsPropertiesTemplate()
    {
    	return "/templates/flexbuilder/actionScriptProperties2.vm";
    }
    
    /**
     * Override to remove the dependencies from the context generated by the super class
     * and add our own dependencies with proper scope values to be used by our custom template.
   	 * @return
     */
    @Override
    protected VelocityContext getAsPropertiesContext( String packaging, IdeDependency[] ideDependencies )
		throws MojoExecutionException
	{
    	// Clear out these values so they are not added to the
		// additoinalCompilerParameters in the super class getAsPropertiesContext.
		this.services = null;
		this.contextRoot = null;
		this.definesDeclaration = null;
    	
		// Get context from super class
    	VelocityContext context = super.getAsPropertiesContext( packaging, ideDependencies );
    	
    	// Add flex/flash builder version
    	context.put( "version", version );
    	
    	// Replace super dependencies with ours which have correct scope values.
    	context.remove("dependencies");
    	context.put("dependencies", fbDeps);
    	
    	// Add additional context params not supported by super.
    	context.put( "flexSDK", getFlashBuilderSdkVersion() );
    	
    	// Add .flexConfig.xml to additionalCompilerArguments
    	String args = (String)context.get( "additionalCompilerArguments" );
    	
    	// Super class puts adds {locale} sources for SWC projects. These do not work correctly with Flash/Flex Builder so strip them out.
    	String[] argsa = args.split(" ");
    	args = "";
    	for(int i=0; i<argsa.length; i++)
    	{
    		String arg = argsa[i];
    		if(!arg.contains("{locale}"))
    			args +=  arg+" ";
    	}
    	    	
    	args += " -load-config+="+project.getBasedir().getAbsolutePath()+"/.flexConfig.xml";
    	context.put( "additionalCompilerArguments", args );
    	
    	return context;
	}
    
    
    //---------------------------------------------------------------
    // Other functions that are not included in base Flex Builder Mojo
    // that are usefull for better Flex Builder config generation.
    //---------------------------------------------------------------
    
    /**
     * Returns the velocity template context to used for .flexConfig.xml generation.
     * 
     * @return
     */
    protected VelocityContext getFlexConfigContext()
    {
    	VelocityContext context = new VelocityContext();
    	
    	context.put("namespaces", namespaces);
		context.put("services", services);
		context.put("contextRoot", contextRoot);
		context.put("defines", definesDeclaration);
		context.put("metadatas", keepAs3Metadatas);
	
		if( SWF.equals( packaging ) || AIR.equals( packaging ) )
		{
			context.put( "defaultSizeWidth", defaultSizeWidth );
			context.put( "defaultSizeHeight", defaultSizeHeight );
	
			List<String> themes = getThemes();
			context.put("themes", themes);
		}
    	
    	return context;
    }
    
    /**
     * Returns the template that should be used for .flexConfig.xml generation
     * 
     * @return
     */
    protected String getFlexConfigTemplate()
    {
    	return "/templates/flexbuilder/flexConfig.vm";
    }
    
    private void writeFlexConfig( String packaging, IdeDependency[] ideDependencies )
		throws MojoExecutionException
	{
		runVelocity( getFlexConfigTemplate(), ".flexConfig.xml", getFlexConfigContext() );
	}

	private void writeHtmlTemplate()
		throws MojoExecutionException
	{
		if(generateHtmlWrapper)
		{
			// delete existing html template
			File outputDir = flexbuilderTemplateDirectory;
			if(outputDir.exists()) {
				outputDir.delete();
			}
	
			HtmlWrapperUtil.extractTemplate(project, templateURI, outputDir);
		}
	}
    
    /**
     * Combines themes passed in on the themes property with themes
     * that are added as Maven dependencies with scope theme
     */
    private List<String> getThemes()
    {
    	List<String> allThemes;
   
    	if(themes != null)
    	{
    		allThemes = new ArrayList<String>(Arrays.asList(themes));
    	}
    	else
    	{
    		allThemes = new ArrayList<String>();
    	}
    
    	Iterator<FbIdeDependency> it = fbDeps.iterator();
    	while(it.hasNext())
    	{
    		FbIdeDependency dp = it.next();
    		if( dp.getScope() != null && dp.getScope().equals("theme") )
    		{
    			if( !dp.isReferencedProject() )
    			{
    				allThemes.add( dp.getPath() );
    			}
    			else
    			{
    				allThemes.add( ".." + dp.getPath() );
    			}
    		}
    	}
    
    	return allThemes;
    }
    
    /**
    * Builds a collection of resource entries based of an array of files.
    * Each ResourceEntry has a destination and a source. The source is the
    * absolute path of the file include and the destination is a relative path
    * starting at the source path root. The destination path is important
    * because this is the path to where it will end up in the compiled SWC
    *
    * An example would be:
    * Source path = ${basedir}/src/main/resources/org/proj/myfile.txt
    * Destination path = org/proj/myfile.txt
    * @param includeFiles
    * @return
    */
    private Collection<ResourceEntry> getResourceEntries( File[] includeFiles )
    {
    	Collection<ResourceEntry> entries = new ArrayList<ResourceEntry>();
        
    	Collection<String> sourceRoots = getSourceRoots();
        
    	if( includeFiles != null )
    	{
    		for( int i=0; i<includeFiles.length; i++ )
    		{
    			File includeFile = includeFiles[i];
    			String sourcePath = includeFile.getAbsolutePath();
     
    			// Strip source roots from destination and source paths.
    			String destPath = "";
    			for( String sourceRoot : sourceRoots )
    			{
    				if( sourcePath.contains( sourceRoot) )
    				{
    					int srl = sourceRoot.length();
    					destPath = sourcePath.substring( srl+1 );
    					sourcePath = destPath;
    				}
    			}
     
    			// If the source path is not relative to any source roots
    			// then the destination path will use the full source path.
    			if(destPath.length() < 1)
    			{
    				destPath = sourcePath;
    			}
     
    			entries.add( new ResourceEntry( destPath, sourcePath ) );
    		}
    	}
        
    	return entries;
    }
    
    
    //---------------------------------------------------------------
    // Utility functions to solve the issue that IdeDependency class
    // doesn't include the scope value.
    //---------------------------------------------------------------
    
    private List<FbIdeDependency> getFbIdeDependencies(IdeDependency[] ideDependencies)
    	throws MojoExecutionException
    {
	    
	    List<FbIdeDependency> deps = new ArrayList<FbIdeDependency>();
	   
	   
	    // convert all IdeDependencies to FbIdeDependencies
	    for(int i=0; i<ideDependencies.length; i++)
	    {
	    	IdeDependency ideDep = ideDependencies[i];
	   
	    	// ignore artifacts that don't belong in flex builder
	        if( SWC.equals( ideDep.getType() ) || RB_SWC.equals( ideDep.getType() ) )
	        {
	        	FbIdeDependency fbDep = resolveFbIdeDependency( ideDep );
	        	
	        	if(fbDep == null)
	        		throw new MojoExecutionException( String.format( "Unable to find %s ", ideDep.toString() ) );
	       
	        	// make sure playergobal and airglobal are scoped to external
	            if ( "playerglobal".equals( fbDep.getArtifactId() ) ||
	            	 "airglobal".equals( fbDep.getArtifactId() ) )
	            {
	            	// ignore playerglobal or airglobal that is scoped as test.
	            	// these are picked up by test dependencies so need to be filtered out.
	            	if( fbDep.getScope().equals( "test" ) )
	            		continue;
	            	
	            	fbDep.setScope(FlexScopes.EXTERNAL);
	            	// work around issue with Flex Compiler not allowing playerglobal/airglobal to have a different name.
	            	// https://bugs.adobe.com/jira/browse/SDK-15073
	            	File tempGlobal = new File( project.getBuild().getDirectory() + "/classes/libraries/" + fbDep.getArtifactId() + ".swc");
	            	try
	            	{
	            		FileUtils.copyFile( fbDep.getFile(), tempGlobal );
	            	}
		            catch(IOException e)
		            {
		            	String msg =
		            		String.format( "Unable to copy %s to %s/classes/libraries ",
		            				fbDep.getArtifactId(),
		            				project.getBuild().getDirectory() );
		            	
		            	throw new MojoExecutionException( msg, e );									
		            }
	           
		            fbDep.setFile( tempGlobal );
	            }
	            
	            // link Flex Framework source paths
	            resolveFlexSourcePath( fbDep );
	            	           
	            // link file path to Eclipse project
	            if ( fbDep.isReferencedProject() )
	            {
	            	String projectName = fbDep.getEclipseProjectName();
	                // /todolist-lib/bin-debug/todolist-lib.swc
	            	fbDep.setFile( new File( "/" + projectName + "/bin-debug/" + projectName + ".swc" ) );
	            	fbDep.setSourceAttachment( new File( "/" + projectName + "/src/main/flex/" ) );

	            }
	       
	            deps.add(fbDep);
	        }
	    }
 
    	return deps;
	}

    /**
	 * Convert IdeDependency to FbIdeDependency using the matching Artifact to fill in missing scope.
	 * @param artifacts
	 * @param ideDependency
	 * @return
	 * @throws MojoExecutionException 
	*/
	protected FbIdeDependency resolveFbIdeDependency( IdeDependency ideDependency )
    	throws MojoExecutionException
    {  	
    	
    	Set<Artifact> artifacts =
    		MavenUtils.getDependencyArtifacts( project,
    	         resolver,
    	         localRepository,
    	         remoteRepositories,
    	         artifactMetadataSource,
    	         artifactFactory );
    	
    	FbIdeDependency dep = null;
	    
    	Artifact artifact = null;
	    
    	if( getLog().isDebugEnabled() )
    		getLog().debug( String.format( "Searching for artifact matching IDE Depependecy %s:%s:%s:%s",
    						ideDependency.getGroupId(),
    						ideDependency.getArtifactId(),
    						ideDependency.getVersion(),
    						ideDependency.getType() ) );
	    
	    for( Iterator<Artifact> it = artifacts.iterator(); it.hasNext(); )
		{
	    	artifact = it.next();
		 
	    	if( getLog().isDebugEnabled() )
	    		getLog().debug( String.format( "Checking artifact %s:%s:%s:%s",
	    						artifact.getGroupId(),
	    						artifact.getArtifactId(),
	    						artifact.getVersion(),
	    						artifact.getType() ) );
		 
	    	// match referenced projects
	    	if( ideDependency.isReferencedProject() )
	    	{
	    		if( ideDependency.getGroupId().equals( artifact.getGroupId() ) && // match groupId
	    			ideDependency.getArtifactId().equals( artifact.getArtifactId() ) ) // match artifactId
	    		{
	    			break; // match found
	    		}
	    	}
	    	// match non referenced projects using files paths to avoid problems with SNAPSHOT version matching.
	    	else if ( artifact.getFile().equals( ideDependency.getFile() ) )
	    	{
	    		// match classifiers if needed.
	    		if( ideDependency.getClassifier() != null)
	    		{
	    			if( ideDependency.getClassifier().equals( artifact.getClassifier() ) )
	    			{
	    				break; // match found
	    			}
	    		}
	    		else
	    		{
	    			break; // match found
	    		}
	    	}
			 
	    	// artifact did not match. null and continue loop
	    	artifact = null;
		}
	    
	    if(artifact == null)
	    	getLog().warn("Unable to find artifact for IDE dependecy! "+ideDependency);
		
	    if(artifact != null)
	    {
	    	dep = new FbIdeDependency( ideDependency, artifact.getScope() );
	    }
				
		return dep;
    }
	
	/**
     * Attempts to resolve Flex Framework source paths.
     * @return
     */
    protected void resolveFlexSourcePath( FbIdeDependency dependency )
    {
    	resolveFlexSourcePathFlex3( dependency );
    }
    
    @FlexCompatibility( minVersion = "3", maxVersion = "3.4.1" )
    @IgnoreJRERequirement
    protected void resolveFlexSourcePathFlex3( FbIdeDependency dependency )
    {
    	String path = null;
    	
    	if( dependency.getGroupId().equals( "com.adobe.flex.framework" ) )
    	{
    		if( dependency.getArtifactId().equals( "airframework" ) ) {
    			path = "${PROJECT_FRAMEWORKS}/projects/airframework/src/";
    		} else if( dependency.getArtifactId().equals( "applicationupdater" ) ) {
    			path = "${PROJECT_FRAMEWORKS}/projects/air/ApplicationUpdater/src/ApplicationUpdater";
    		} else if( dependency.getArtifactId().equals( "applicationupdater_ui" ) ) {
    			path = "${PROJECT_FRAMEWORKS}/projects/air/ApplicationUpdater/src/ApplicationUpdaterDialogs";
    		} else if( dependency.getArtifactId().equals( "automation" ) ) {
    			path = "${PROJECT_FRAMEWORKS}/projects/automation/src";
    		} else if( dependency.getArtifactId().equals( "flex" ) ) {
    			path = "${PROJECT_FRAMEWORKS}/projects/flex/src";
    		} else if( dependency.getArtifactId().equals( "framework" ) ) {
    			path = "${PROJECT_FRAMEWORKS}/projects/framework/src";
    		} else if( dependency.getArtifactId().equals( "haloclassic" ) ) {
    			path = "${PROJECT_FRAMEWORKS}/projects/haloclassic/src";
    		} else if( dependency.getArtifactId().equals( "rpc" ) ) {
    			path = "${PROJECT_FRAMEWORKS}/projects/rpc/src";
    		} else if( dependency.getArtifactId().equals( "servicemonitor" ) ) {
    			path = "${PROJECT_FRAMEWORKS}/projects/air/ServiceMonitor/src";
    		} else if( dependency.getArtifactId().equals( "utilities" ) ) {
    			path = "${PROJECT_FRAMEWORKS}/projects/utilities/src";
    		}
    	}
    	
    	if( path != null )
    		dependency.setSourcePath( path );
    }

    public String getCompilerVersion()
    {
        Artifact compiler = MavenUtils.searchFor( pluginArtifacts, "com.adobe.flex", "compiler", null, "pom", null );
        return compiler.getVersion();
    }
    
    protected String getFlashBuilderSdkVersion()
    {
    	String value = "default";
    	
    	if(this.flexSDK == null || this.flexSDK.length() < 1)
    	{
    		value = "Flex ";
    		String[] version = getCompilerVersion().split("\\.");
    		for(int i=0; i<3; i++)
    		{
    			// take the first 2 digits as they are
    			if(i < 2)
    			{
    				value += version[i] + ".";
    			}
    			// if the last digit is not zero use it otherwise drop it.
    			else if( !version[i].equals("0") )
    			{
    				value += version[i] + ".";
    			}
    		}
    		
    		// remove the trailing . if it exists.
    		if( value.endsWith(".") )
    			value = value.substring(0, value.length()-1);
    	}
    	
    	return value;
 
    }

}
