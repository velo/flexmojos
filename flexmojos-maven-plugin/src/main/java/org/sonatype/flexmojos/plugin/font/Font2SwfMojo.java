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
package org.sonatype.flexmojos.plugin.font;

import static org.sonatype.flexmojos.util.PathUtil.path;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.flexmojos.plugin.AbstractMavenMojo;

import com.adobe.fonts.transcoder.DefineFont3Transcoder;
import com.adobe.fonts.transcoder.DefineFont4Transcoder;
import com.adobe.fonts.transcoder.Font2SWF;
import com.adobe.fonts.transcoder.Font2SWF.DefineFontKind;
import com.adobe.fonts.transcoder.FontTranscoder;
import com.adobe.fonts.transcoder.FontTranscoderException;

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
    private DefineFontKind transcoder;

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

    public void execute()
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

        FontTranscoder fontTranscoder;
        if ( transcoder == Font2SWF.DefineFontKind.DEFINEFONT3 )
        {
            fontTranscoder = new DefineFont3Transcoder();
        }
        else if ( transcoder == Font2SWF.DefineFontKind.DEFINEFONT4 )
        {
            fontTranscoder = new DefineFont4Transcoder();
        }
        else
        {
            throw new IllegalStateException( "Unexpected font transcoder: " + transcoder );
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
            fontTranscoder.transcode( description, output );
        }
        catch ( FontTranscoderException e )
        {
            throw new MojoExecutionException( "Failed to create the font swf", e );
        }
        catch ( FileNotFoundException e )
        {
            throw new MojoExecutionException( "Failed to create the font swf", e );
        }
        finally
        {
            IOUtil.close( output );
        }
    }

}
