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
package org.sonatype.flexmojos.compiler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class hold SWF metadata
 * 
 * @author velo
 *
 * Vladimir Krivosheev, 16 Aug 2009: We must flip titles and description.
 * flex2.tools.oem.Configuration declare setSWFMetaData as Map "lang tag -> text", but OEMConfiguration implementation use reverced order,
 * because call flex2.compiler.common.MetadataConfiguration#cfgLocalizedTitle - method signature: String title, String lang
 * Run mvn test -Dtest=IT0014ConceptTest (flexmojos-test-harness project) for test workaround
 */
public class Metadata
{
    private static String DEFAULT_LANGUAGE_TAG = "x-default";

    /**
     * A contributor's name to store in the SWF metadata
     */
    private String[] contributors;

    /**
     * A creator's name to store in the SWF metadata
     */
    private String[] creators;

    /**
     * The creation date to store in the SWF metadata
     */
    private Date date;

    /**
     * The title to store in the SWF metadata
     */
    private String title;

    /**
     * The localized titles to store in the SWF metadata
     */
    private Map<String, String> titles;

    /**
     * The description to store in the SWF metadata
     */
    private String description;

    /**
     * The localized descriptions to store in the SWF metadata
     */
    private Map<String, String> descriptions;

    /**
     * The language code (RFC 4646) to store in the SWF metadata
     * @see http://www.ietf.org/rfc/rfc4646.txt
     * @see http://www.langtag.net/
     * @see http://unicode.org/cldr/utility/languageid.jsp
     */
    private String[] languages;

    /**
     * A publisher's name to store in the SWF metadata
     */
    private String[] publishers;

    public String[] getContributors()
    {
        return contributors;
    }

    public void setContributors( String[] contributors )
    {
        this.contributors = contributors;
    }

    public String[] getCreators()
    {
        return creators;
    }

    public void setCreators( String[] creators )
    {
        this.creators = creators;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate( Date date )
    {
        this.date = date;
    }

    public String getDescription()
    {
        return description;
    }

    public void addDescription( String description )
    {
        addDescription( DEFAULT_LANGUAGE_TAG, description );
    }

    public void addDescription( String locale, String description )
    {
        if ( descriptions == null )
        {
            descriptions = new HashMap<String, String>();
        }
        descriptions.put( description, locale );
    }

    public Map<String, String> getDescriptions()
    {
        return descriptions;
    }

    public String[] getLanguages()
    {
        return languages;
    }

    public void setLanguages( String[] languages )
    {
        this.languages = languages;
    }

    public String[] getPublishers()
    {
        return publishers;
    }

    public String getTitle()
    {
        return title;
    }

    public void addTitle( String title )
    {
        addTitle( DEFAULT_LANGUAGE_TAG, title );
    }

    public void addTitle( String locale, String title )
    {
        if ( titles == null )
        {
            titles = new HashMap<String, String>();
        }
        titles.put( title, locale );
    }

    public Map<String, String> getTitles()
    {
        return titles;
    }

    public void fixTitles()
    {
        titles = fixMap( titles );
    }

    public void fixDescriptions()
    {
        descriptions = fixMap( descriptions );
    }

    private Map<String, String> fixMap( Map<String, String> map )
    {
        HashMap<String, String> fixedMap = new HashMap<String, String>( map.size() );
        for ( Map.Entry<String, String> entry : map.entrySet() )
        {
            fixedMap.put( entry.getValue(), entry.getKey() );
        }
        return fixedMap;
    }
}