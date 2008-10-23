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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class hold SWF metadata
 * 
 * @author velo
 */
public class Metadata
{

    /**
     * A contributor's name to store in the SWF metadata (repeatable)
     */
    private String contributor;

    /**
     * A creator's name to store in the SWF metadata (repeatable)
     */
    private String creator;

    /**
     * The creation date to store in the SWF metadata
     */
    private Date date;

    /**
     * The default description to store in the SWF metadata
     */
    private Map<String, String> descriptions;

    /**
     * The language to store in the SWF metadata (i.e. EN, FR) (repeatable)
     */
    private String language;

    /**
     * A publisher's name to store in the SWF metadata (repeatable)
     */
    private List<String> publishers;

    /**
     * The default title to store in the SWF metadata
     */
    private Map<String, String> titles;

    public String getContributor()
    {
        return contributor;
    }

    public void setContributor( String contributor )
    {
        this.contributor = contributor;
    }

    public String getCreator()
    {
        return creator;
    }

    public void setCreator( String creator )
    {
        this.creator = creator;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate( Date date )
    {
        this.date = date;
    }

    public void addDescription( String locale, String description )
    {
        if ( descriptions == null )
        {
            descriptions = new HashMap<String, String>();
        }
        descriptions.put( locale, description );
    }

    public Map<String, String> getDescriptions()
    {
        return descriptions;
    }

    public void setDescriptions( Map<String, String> description )
    {
        this.descriptions = description;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage( String languages )
    {
        this.language = languages;
    }

    public List<String> getPublishers()
    {
        return publishers;
    }

    public void setPublishers( List<String> publishers )
    {
        this.publishers = publishers;
    }

    public void addTitle( String locale, String title )
    {
        if ( titles == null )
        {
            titles = new HashMap<String, String>();
        }
        titles.put( locale, title );
    }

    public Map<String, String> getTitles()
    {
        return titles;
    }

    public void setTitles( Map<String, String> titles )
    {
        this.titles = titles;
    }

}
