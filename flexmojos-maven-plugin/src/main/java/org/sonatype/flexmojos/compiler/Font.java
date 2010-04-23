/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.compiler;

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
