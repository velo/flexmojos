package org.sonatype.flexmojos.plugin.compiler.attributes;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
                public String text()
                {
                    return entry.getValue();
                }

                public String lang()
                {
                    return entry.getKey();
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
