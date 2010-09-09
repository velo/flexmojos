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
package org.sonatype.flexmojos.plugin.compiler.attributes;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sonatype.flexmojos.compiler.ILocalizedDescription;
import org.sonatype.flexmojos.compiler.ILocalizedTitle;
import org.sonatype.flexmojos.compiler.IMetadataConfiguration;

public class MavenMetadataConfiguration
    implements IMetadataConfiguration
{

    private String[] contributors;

    private String[] creators;

    private String date;

    private String description;

    private String[] languages;

    private Map<String, String> localizedDescriptions;

    private Map<String, String> localizedTitles;

    private String[] publishers;

    private String title;

    public String[] getContributor()
    {
        return contributors;
    }

    public String[] getCreator()
    {
        return creators;
    }

    public String getDate()
    {
        return date;
    }

    public String getDescription()
    {
        return description;
    }

    public String[] getLanguage()
    {
        return languages;
    }

    public ILocalizedDescription[] getLocalizedDescription()
    {
        if ( localizedDescriptions == null )
        {
            return null;
        }

        int i = 0;
        ILocalizedDescription[] keys = new ILocalizedDescription[localizedDescriptions.size()];
        Set<Entry<String, String>> entries = localizedDescriptions.entrySet();
        for ( final Entry<String, String> entry : entries )
        {
            keys[i++] = new ILocalizedDescription()
            {
                public String lang()
                {
                    return entry.getKey();
                }

                public String text()
                {
                    return entry.getValue();
                }
            };
        }

        return keys;
    }

    public ILocalizedTitle[] getLocalizedTitle()
    {
        if ( localizedTitles == null )
        {
            return null;
        }

        int i = 0;
        ILocalizedTitle[] keys = new ILocalizedTitle[localizedTitles.size()];
        Set<Entry<String, String>> entries = localizedTitles.entrySet();
        for ( final Entry<String, String> entry : entries )
        {
            keys[i++] = new ILocalizedTitle()
            {
                public String lang()
                {
                    return entry.getKey();
                }

                public String title()
                {
                    return entry.getValue();
                }
            };
        }

        return keys;
    }

    public String[] getPublisher()
    {
        return publishers;
    }

    public String getTitle()
    {
        return title;
    }

}
