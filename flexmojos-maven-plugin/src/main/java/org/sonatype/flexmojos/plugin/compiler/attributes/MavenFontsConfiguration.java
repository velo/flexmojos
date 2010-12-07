package org.sonatype.flexmojos.plugin.compiler.attributes;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.compiler.IFontsConfiguration;
import org.sonatype.flexmojos.compiler.ILanguageRange;
import org.sonatype.flexmojos.compiler.ILanguages;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;
import org.sonatype.flexmojos.util.PathUtil;

public class MavenFontsConfiguration
    implements IFontsConfiguration, ILanguages
{

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

    private File outputDirectory;

    public Boolean getAdvancedAntiAliasing()
    {
        return advancedAntiAliasing;
    }

    public Boolean getFlashType()
    {
        return flashType;
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

        File fontsSer = new File( outputDirectory, "fonts.ser" );
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

    public IFontsConfiguration toFontsConfiguration( File outputDirectory )
    {
        this.outputDirectory=outputDirectory;
        return this;
    }

}
