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
package org.sonatype.flexmojos.fbtests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AbstractFlexMojosFbTest
{
	protected static File projectsSource;
    protected static File projectsWorkdir;
    private static Properties props;
    private static File mavenHome;
    protected static PlexusContainer container;
    private static final ReadWriteLock copyProjectLock = new ReentrantReadWriteLock();
    private static final ReadWriteLock downloadArtifactsLock = new ReentrantReadWriteLock();
    
    protected static SWTWorkbenchBot bot;
    
    public static final String PACKAGE_EXPLORER_VIEW_ID = "org.eclipse.ui.navigator.ProjectExplorer";//"org.eclipse.jdt.ui.PackageExplorer";
    
    protected static enum ProjectType
    {
    	FLEX( "Flex Compiler" ),
    	FLEX_LIBRARY( "Flex Library Compiler" );
    	
    	private String compilerMenu;
    	
    	private ProjectType( String compilerMenu )
    	{
    		this.compilerMenu = compilerMenu;
    	}
    	
    	public String getCompilerMenu()
    	{
    		return compilerMenu;
    	}
    }
    
    @BeforeClass
    public static void beforeClass()
        throws IOException, PlexusContainerException
    {
    	// Initialize bot
    	bot = new SWTWorkbenchBot();
        if( "Welcome".equals( bot.activeView().getTitle() ) )
        	bot.activeView().close();
    	
    	// Initialize folders... only initialize once.
        if ( props != null )
            return;
        
        // Load properties from external file.
        props = new Properties();
        ClassLoader cl = AbstractFlexMojosFbTest.class.getClassLoader();
        InputStream is = cl.getResourceAsStream( "baseTest.properties" );
        if ( is != null )
        {
            try
            {
                props.load( is );
            }
            finally
            {
                is.close();
            }
        }

        // Set properties read from external file.
        projectsSource = new File( getProperty( "projects-source" ) );
        projectsWorkdir = new File( getProperty( "projects-target" ) );
        mavenHome = new File( getProperty( "fake-maven" ) );

        // Update memory settings for fake maven shell command.
        File mvn = new File( mavenHome, "bin/mvn" );
        updateMavenMemory( mvn, "\nMAVEN_OPTS=\"-Xmx512M -Duser.language=en -Duser.region=US\"\n" );
        File mvnBat = new File( mavenHome, "bin/mvn.bat" );
        updateMavenMemory( mvnBat, "\nset MAVEN_OPTS=-Xmx512M -Duser.language=en -Duser.region=US\n" );

        container = new DefaultPlexusContainer();
        
    }
    
    private static void updateMavenMemory( File mvn, String memString )
    	throws IOException
	{
	    String mvnContent = org.codehaus.plexus.util.FileUtils.fileRead( mvn );
	    if ( mvnContent.contains( memString ) )
	    {
	        return;
	    }
	    int i = mvnContent.indexOf( '\n' );
	    mvnContent = mvnContent.substring( 0, i ) + memString + mvnContent.substring( i );
	    org.codehaus.plexus.util.FileUtils.fileWrite( mvn.getAbsolutePath(), mvnContent );
	}

	protected static synchronized String getProperty( String key )
	{
	    return props.getProperty( key );
	}
	
	@SuppressWarnings( "unchecked" )
    protected static Verifier test( File projectDirectory, String goal, String... args )
        throws VerificationException
    {
        Verifier verifier = getVerifier( projectDirectory );
        verifier.getCliOptions().addAll( Arrays.asList( args ) );
        verifier.executeGoal( goal );
        // TODO there are some errors logged, but they are not my concern
        // verifier.verifyErrorFreeLog();
        return verifier;
    }
	
	@SuppressWarnings( "unchecked" )
    protected static Verifier getVerifier( File projectDirectory )
        throws VerificationException
    {
        System.setProperty( "maven.home", mavenHome.getAbsolutePath() );

        if ( new File( projectDirectory, "pom.xml" ).exists() )
        {
            downloadArtifactsLock.writeLock().lock();
            try
            {
                Verifier verifier = new Verifier( projectDirectory.getAbsolutePath() );
                verifier.getVerifierProperties().put( "use.mavenRepoLocal", "true" );
                verifier.setLocalRepo( getProperty( "fake-repo" ) );
                verifier.setAutoclean( false );
                verifier.getCliOptions().add( "-npu" );
                verifier.getCliOptions().add( "-B" );
                verifier.setLogFileName( getTestName() + ".resolve.log" );
                verifier.executeGoal( "dependency:go-offline" );
            }
            catch ( Throwable t )
            {
                t.printStackTrace();
                // this is not a real issue
            }
            finally
            {
                downloadArtifactsLock.writeLock().unlock();
            }
        }

        Verifier verifier = new Verifier( projectDirectory.getAbsolutePath() );
        // verifier.getCliOptions().add( "-s" + rootFolder.getAbsolutePath() + "/settings.xml" );
        // verifier.getCliOptions().add( "-o" );
        verifier.getCliOptions().add( "-npu" );
        verifier.getCliOptions().add( "-B" );
        verifier.getCliOptions().add( "-X" );
        verifier.getVerifierProperties().put( "use.mavenRepoLocal", "true" );
        verifier.setLocalRepo( getProperty( "fake-repo" ) );
        Properties sysProps = new Properties();
        sysProps.setProperty( "MAVEN_OPTS", "-Xmx512m" );
        verifier.setSystemProperties( sysProps );
        verifier.setLogFileName( getTestName() + ".log" );
        verifier.setAutoclean( false );
        return verifier;
    }

    private static String getTestName()
    {
        StackTraceElement[] stackTrace = new Exception().getStackTrace();
        for ( StackTraceElement stack : stackTrace )
        {
            Class<?> testClass;
            try
            {
                testClass = Class.forName( stack.getClassName() );
            }
            catch ( ClassNotFoundException e )
            {
                // nvm, and should never happen
                continue;
            }
            for ( Method method : testClass.getMethods() )
            {
                if ( method.getName().equals( stack.getMethodName() ) )
                {
                    if ( method.getAnnotation( Test.class ) != null )
                    {
                        return method.getName();
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings( "unchecked" )
    protected static File getProject( String projectName, String... filesToInterpolate )
        throws IOException
    {
        if ( filesToInterpolate == null || filesToInterpolate.length == 0 )
        {
            filesToInterpolate = new String[] { "pom.xml" };
        }

        copyProjectLock.writeLock().lock();
        try
        {
            File projectFolder = new File( projectsSource, projectName );
            Assert.assertTrue( "Project " + projectName + " folder not found.\n" + projectFolder.getAbsolutePath(),
                                    projectFolder.isDirectory() );

            File destDir = new File( projectsWorkdir, projectName + "_" + getTestName() );

            FileUtils.copyDirectory( projectFolder, destDir, HiddenFileFilter.VISIBLE );

            // projects filtering
            Collection<File> poms =
                FileUtils.listFiles( destDir, new WildcardFileFilter( filesToInterpolate ), TrueFileFilter.INSTANCE );
            for ( File pom : poms )
            {
                String pomContent = FileUtils.readFileToString( pom );
                pomContent = pomContent.replace( "%{flexmojos.version}", getFlexmojosVersion() );
                pomContent = pomContent.replace( "%{flex.version}", getFlexSDKVersion() );
                FileUtils.writeStringToFile( pom, pomContent );
            }

            return destDir;
        }
        finally
        {
            copyProjectLock.writeLock().unlock();
        }
    }

    protected static String getFlexSDKVersion()
    {
        return getProperty( "flex-version" );
    }

    protected static String getFlexmojosVersion()
    {
        return getProperty( "version" );
    }
    
    protected static void assertProjectFiles( String directory, ProjectType type )
    {
    	// Check all project files exist
    	File projectDir = new File( directory );
        File projectFile = new File( projectDir, ".project" );
        File asFile = new File( projectDir, ".actionScriptProperties" );
        File flexConfig = new File( projectDir, ".flexConfig.xml" );
        File flexProperties = new File( projectDir, ".flexProperties" );
        File flexLibProperties = new File( projectDir, ".flexLibProperties" );
        
        Assert.assertTrue( ".project file should exist for project type "+type.name(), projectFile.exists() );
        Assert.assertTrue( ".actionScriptProperties file should exist for project type "+type.name(), asFile.exists() );
        Assert.assertTrue( ".flexConfig.xml file should exist for project type "+type.name(), flexConfig.exists() );
        Assert.assertTrue( ".flexProperties file should exist for project type "+type.name(), flexProperties.exists() );
        
        if( type == ProjectType.FLEX_LIBRARY )
        	Assert.assertTrue( ".flexLibProperties file should exist for project type "+type.name(), flexLibProperties.exists() );
    }
    
    protected static void assertGeneralProjectConfig( String projectName, Double sdkVersion, boolean generateHtml )
    {
    	assertGeneralProjectConfig(projectName, sdkVersion, ProjectType.FLEX, generateHtml);
    }
    
    protected static void assertGeneralProjectConfig( String projectName, Double sdkVersion, ProjectType projectType, boolean generateHtml )
    {
    	
    	
    	// Check FlexBuilder config dialog is correct
        SWTBotTree tree = selectProject( projectName, true );
        ContextMenuHelper.clickContextMenu( tree, "Properties" );
        SWTBotShell shell = bot.shell("Properties for "+projectName );
        shell.activate();
        
        // Check values in "Flex Compiler" section
        bot.tree().select( projectType.getCompilerMenu() );
        
        if( projectType == ProjectType.FLEX )
        {
        	SWTBotCheckBox genHtmlCheckBox = bot.checkBox( "Generate HTML wrapper file" );
        	Assert.assertEquals( "Generate HTML wrapper file", generateHtml, genHtmlCheckBox.isChecked() );
        }
        
        // Check selected SDK
        String flexSdk = "Flex "+sdkVersion.toString();
        SWTBotCombo sdkCombo = bot.comboBox( 0 );
        Assert.assertEquals( "Use a specific SDK:", flexSdk, sdkCombo.selection() );
        
        // Close dialog
        bot.button( "Cancel" ).click();
    }
    
    protected static void importAndBuildProject( String directory )
    	throws IOException
    {
    	//bot.menu( "File" ).menu( "New" ).menu( "Project..." ).click();
    	bot.menu( "File" ).menu( "Import..." ).click();

        //SWTBotShell shell = bot.shell( "New Project" );
        //shell.activate();
        //bot.tree().select( "Java Project" );
        //bot.button( "Next >" ).click();

    	SWTBotShell shell = bot.shell( "Import" );
        shell.activate();
        bot.tree().select( "General" ).expandNode( "General" ).select( "Existing Projects into Workspace" );
        bot.button( "Next >" ).click();
    	
        //bot.textWithLabel( "Project name:" ).setText( "MyFirstProject" );
        //bot.radioWithLabel( "Select root directory:" ).click();
        bot.radio( "Select root directory:" ).click();
        bot.text(0).setText( new File( directory ).getCanonicalPath() );
        bot.button( "Refresh" ).click();
        bot.button( "Finish" ).click();
        
        JobHelpers.waitForJobsToComplete();
    }
    
    protected static SWTBotTree selectProject(String projectName, boolean searchForIt) {
		SWTBotTree tree = bot.viewById(PACKAGE_EXPLORER_VIEW_ID).bot().tree();
		SWTBotTreeItem treeItem = null;
		try {
			treeItem = tree.getTreeItem(projectName);
		} catch (WidgetNotFoundException ex) {
			if (searchForIt) {
				SWTBotTreeItem[] allItems = tree.getAllItems();
				for (SWTBotTreeItem item : allItems) {
					// workaround required due to SVN/CVS that does add extra
					// informations to project name
					if (item.getText().contains(projectName)) {
						treeItem = item;
						break;
					}
				}
			}

			if (treeItem == null) {
				throw ex;
			}
		}
		treeItem.select();
		return tree;
	}
    
    @AfterClass
    public static void sleep()
    {
       AbstractFlexMojosFbTest.bot.sleep( 2000 );
    }

}
