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
package net.flexmojos.oss.plugin.font;

import static net.flexmojos.oss.util.PathUtil.path;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import net.flexmojos.oss.plugin.font.types.TranscoderType;
import org.apache.commons.io.FilenameUtils;
import org.apache.flex.utilities.converter.fontkit.FontkitConverter;
import org.apache.flex.utilities.converter.retrievers.download.DownloadRetriever;
import org.apache.flex.utilities.converter.retrievers.types.SdkType;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.IOUtil;
import net.flexmojos.oss.plugin.AbstractMavenMojo;

import flash.fonts.FontDescription;

/**
 * This goal compiles fonts into swf files
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @goal font2swf
 * @since 4.0
 */
public class Font2SwfMojo
    extends AbstractMavenMojo
{

    /**
     * Treat the font as italic
     * 
     * @parameter expression="${flex.font.italic}"
     */
    private boolean italic;

    /**
     * Treat the font as bold
     * 
     * @parameter expression="${flex.font.bold}"
     */
    private boolean bold;

    /**
     * Select the font transcoder. Valid values are <b>DEFINEFONT3</b> and <b>DEFINEFONT4</b> <BR>
     * Read more at: https://opensource.adobe.com/wiki/display/flexsdk/Gumbo+Font+Embedding
     * 
     * @parameter default-value="DEFINEFONT4" expression="${flex.font.transcoder}"
     */
    private TranscoderType transcoder;

    /**
     * Set the font alias (defaults to font file name)
     * 
     * @parameter expression="${flex.font.alias}"
     */
    private String alias;

    /**
     * DOCME no docs from adobe
     * 
     * @parameter expression="${flex.font.advancedAntiAliasing}"
     */
    private Boolean advancedAntiAliasing;

    /**
     * DOCME no docs from adobe
     * 
     * @parameter expression="${flex.font.compactFontFormat}"
     */
    private Boolean compactFontFormat;

    /**
     * Set a unicode character range
     * 
     * @parameter default-value="*" expression="${flex.font.unicodeRanges}"
     */
    private String unicodeRanges;

    /**
     * Font file to be compiled
     * 
     * @parameter expression="${flex.font}"
     * @required
     */
    private File font;

    /**
     * The name of the compiled file
     * 
     * @parameter default-name="${project.build.finalName}" expression="${flex.finalName}"
     */
    protected String finalName;

    /**
     * The name of the compiled file
     * 
     * @parameter expression="${flex.font.finalName}"
     */
    protected String fontSwfFinalName;

    public void fmExecute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !font.exists() )
        {
            throw new MojoFailureException( "Font source doesn't exists at " + path( font ) );
        }

        if ( alias == null )
        {
            alias = FilenameUtils.getBaseName( font.getName() );
        }

        FontDescription description = new FontDescription();
        if ( advancedAntiAliasing != null )
        {
            description.advancedAntiAliasing = advancedAntiAliasing;
        }
        if ( compactFontFormat != null )
        {
            description.compactFontFormat = compactFontFormat;
        }
        description.alias = alias;
        description.style = ( bold ? 1 : 0 ) + ( italic ? 2 : 0 );
        description.unicodeRanges = unicodeRanges;
        try
        {
            description.source = new URL( "file:///" + path( font ) );
        }
        catch ( MalformedURLException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }

        Class fontTranscoderType = getTranscoderType(getClass().getClassLoader());
        if(fontTranscoderType == null) {
            File mavenLocalRepoDir = new File(localRepository.getBasedir());
            try {
                DownloadRetriever downloadRetriever = new DownloadRetriever();
                File fontkitRoot = downloadRetriever.retrieve(SdkType.FONTKIT);
                FontkitConverter fontkitConverter = new FontkitConverter(fontkitRoot, mavenLocalRepoDir);
                fontkitConverter.convert();

                // Try to resolve the artifact again (This time it should work).
                Artifact fontkitArtifact = resolve(
                        "com.adobe", "fontkit", "1.0", null, "jar");

                // Create a new Classloader that knows about the fontkit jar and
                // re-try to load the transcoder.
                URLClassLoader classLoader = new URLClassLoader(new URL[]{fontkitArtifact.getFile().toURL()}, getClass().getClassLoader());
                fontTranscoderType = getTranscoderType(classLoader);
            } catch (Exception ce) {
                getLog().error("Caught exception while downloading and converting fontkit libraries.");
            }
        }

        if(fontTranscoderType == null) {
            throw new MojoExecutionException( "Failed to load the trancoder" );
        }

        if ( fontSwfFinalName == null )
        {
            fontSwfFinalName = finalName + "-" + alias;
        }

        OutputStream output = null;
        try
        {
            File outputFile = new File( getTargetDirectory(), fontSwfFinalName + ".swf" );
            output = new FileOutputStream( outputFile );
            Object fontTranscoderInstance = fontTranscoderType.newInstance();
            Method transcodeMethod = fontTranscoderType.getMethod("transcode", FontDescription.class, OutputStream.class);
            transcodeMethod.invoke(fontTranscoderInstance, description, output);
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to create the font swf", e );
        }
        finally
        {
            IOUtil.close( output );
        }
    }

    protected Class getTranscoderType(ClassLoader classloader) {
        try {
            if (transcoder == TranscoderType.DEFINEFONT3) {
                return Class.forName("com.adobe.fonts.transcoder.DefineFont3Transcoder",
                        true, classloader);
            } else if (transcoder == TranscoderType.DEFINEFONT4) {
                return Class.forName("com.adobe.fonts.transcoder.DefineFont4Transcoder",
                        true, classloader);
            }
        } catch (ClassNotFoundException e) {
            return null;
        }
        return null;
    }

}
