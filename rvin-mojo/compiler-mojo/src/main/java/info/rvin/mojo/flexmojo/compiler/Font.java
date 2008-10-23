/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.rvin.mojo.flexmojo.compiler;

import java.io.File;
import java.util.Map;

public class Font
{

    /**
     * Enables advanced anti-aliasing for embedded fonts, which provides greater clarity for small fonts. This setting
     * can be overriden in CSS for specific fonts.
     */
    private boolean advancedAntiAliasing = true;

    /**
     * Enables FlashType for embedded fonts, which provides greater clarity for small fonts. This is equilvalent to
     * using the <code>compiler.fonts.flash-type</code> option for the mxmlc or compc compilers.
     */
    private boolean flashType = true;

    /**
     * Sets a range to restrict the number of font glyphs embedded into the application. This is equivalent to using the
     * <code>compiler.fonts.languages.language-range</code> option for the mxmlc or compc compilers.
     * <p>
     * For example:
     * 
     * <pre>
     * setFontLanguageRange( &quot;englishRange&quot;, &quot;U+0020-U+007E&quot; );
     * </pre>
     */
    private Map<String, String> languages;

    /**
     * File containing cached system font licensing information produced via java -cp mxmlc.jar flex2.tools.FontSnapshot
     * (fontpath) Will default to winFonts.ser on Windows XP and macFonts.ser on Mac OS X, so is commented out by
     * default.
     */
    private File localFontsSnapshot;

    /**
     * Compiler font manager classes, in policy resolution order
     */
    private String[] managers;

    /**
     * The number of embedded font faces that are cached.
     */
    private int maxCachedFonts = 20;

    /**
     * The number of character glyph outlines to cache for each font face.
     */
    private int maxGlyphsPerFace = 1000;

    public boolean isAdvancedAntiAliasing()
    {
        return advancedAntiAliasing;
    }

    public void setAdvancedAntiAliasing( boolean advancedAntiAliasing )
    {
        this.advancedAntiAliasing = advancedAntiAliasing;
    }

    public int getMaxCachedFonts()
    {
        return maxCachedFonts;
    }

    public void setMaxCachedFonts( int maxCachedFonts )
    {
        this.maxCachedFonts = maxCachedFonts;
    }

    public int getMaxGlyphsPerFace()
    {
        return maxGlyphsPerFace;
    }

    public void setMaxGlyphsPerFace( int maxGlyphsPerFace )
    {
        this.maxGlyphsPerFace = maxGlyphsPerFace;
    }

    public String[] getManagers()
    {
        return managers;
    }

    public void setManagers( String[] managers )
    {
        this.managers = managers;
    }

    public File getLocalFontsSnapshot()
    {
        return localFontsSnapshot;
    }

    public void setLocalFontsSnapshot( File localFontsSnapshot )
    {
        this.localFontsSnapshot = localFontsSnapshot;
    }

    public boolean isFlashType()
    {
        return flashType;
    }

    public void setFlashType( boolean flashType )
    {
        this.flashType = flashType;
    }

    public Map<String, String> getLanguages()
    {
        return languages;
    }

    public void setLanguages( Map<String, String> languages )
    {
        this.languages = languages;
    }

}
