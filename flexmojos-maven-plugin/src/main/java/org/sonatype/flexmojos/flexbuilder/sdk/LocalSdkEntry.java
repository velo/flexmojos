package org.sonatype.flexmojos.flexbuilder.sdk;

import org.apache.maven.plugin.ide.IdeDependency;
import org.sonatype.flexmojos.common.FlexScopes;
import org.sonatype.flexmojos.flexbuilder.FbIdeDependency;
import org.sonatype.flexmojos.flexbuilder.ProjectType;

public class LocalSdkEntry
    implements Comparable<LocalSdkEntry>
{
    private String groupId;

    private String artifactId;

    private String path;

    private String sourcePath;

    private ProjectLinkTypeMap projectLinkTypeMap;

    private ProjectType projectType;

    public LocalSdkEntry( String groupId, String artifactId, String path, String sourcePath, ProjectType projectType,
                          ProjectLinkTypeMap projectLinkTypeMap )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.path = path;
        this.sourcePath = sourcePath;
        this.projectLinkTypeMap = projectLinkTypeMap;
        this.projectType = projectType;
    }

    public String getPath()
    {
        return path;
    }

    public String getSourcePath()
    {
        return sourcePath;
    }

    public boolean equals( IdeDependency dependency )
    {
        return ( groupId.equals( dependency.getGroupId() ) && artifactId.equals( dependency.getArtifactId() ) );
    }

    /**
     * Determines if this artifact is included in the current SDK version and project type where the project type is one
     * of (Flex, Flex Library, AIR, AIR Library or ActionScript).
     * 
     * @return
     */
    public boolean isProjectType()
    {
        boolean value = false;
        if ( projectLinkTypeMap != null && projectLinkTypeMap.contains( projectType ) )
            value = true;

        return value;
    }

    /**
     * Is included in base SDK configuration.
     * 
     * @return
     */
    public boolean isDefault()
    {
        return ( projectLinkTypeMap != null );
    }

    /**
     * Returns the link type of this artifact for the current SDK version and Project Type.
     * 
     * @return
     */
    public LinkType getLinkType()
    {
        if ( projectLinkTypeMap == null || projectType == null )
        {
            return LinkType.MERGE;
        }
        else
        {
            LinkType linkType = projectLinkTypeMap.get( projectType ).getLinkType();
            if ( linkType != null )
            {
                return linkType;
            }
            else
            {
                return LinkType.MERGE;
            }
        }
    }

    public Integer getIndex()
    {
        if ( projectLinkTypeMap == null )
        {
            return null;
        }
        else
        {
            return projectLinkTypeMap.get( projectType ).getIndex();
        }
    }

    /**
     * Checks for differences between the IDE dependency and the local Flex SDK entry.
     * 
     * @param dependency
     * @return
     */
    public boolean isModified( FbIdeDependency dependency )
    {
        if ( isLinkTypeModified( dependency ) )
            return true;

        return false;
    }

    /**
     * Evaluates any modifications done by the Maven dependency that don't match up to the local SDK entry and returns
     * an object containing the changes.
     * 
     * @param dependency
     * @return
     */
    public LocalSdkEntryMods getModifications( FbIdeDependency dependency )
    {
        LocalSdkEntryMods mods = new LocalSdkEntryMods();

        mods.setLinkType( getLinkTypeFromIdeDependency( dependency ) );

        return mods;
    }

    private LinkType getLinkTypeFromIdeDependency( FbIdeDependency dependency )
    {
        LinkType depLinkType = LinkType.MERGE;
        if ( FlexScopes.CACHING.equals( dependency.getScope() ) )
        {
            depLinkType = LinkType.RSL_DIGEST;
        }
        else if ( FlexScopes.RSL.equals( dependency.getScope() ) )
        {
            depLinkType = LinkType.RSL;
        }
        else if ( FlexScopes.MERGED.equals( dependency.getScope() ) )
        {
            depLinkType = LinkType.MERGE;
        }
        else if ( FlexScopes.EXTERNAL.equals( dependency.getScope() ) || "runtime".equals( dependency.getScope() ) )
        {
            depLinkType = LinkType.EXTERNAL;
        }
        // NOTE flex/flash builder doesn't support this link type so just merge.
        else if ( FlexScopes.INTERNAL.equals( dependency.getScope() ) )
        {
            depLinkType = LinkType.MERGE;
        }

        return depLinkType;
    }

    private boolean isLinkTypeModified( FbIdeDependency dependency )
    {
        LinkType depLinkType = getLinkTypeFromIdeDependency( dependency );

        return ( depLinkType != this.getLinkType() );
    }

    public int compareTo( LocalSdkEntry o )
    {
        Integer myDefaultIndex = this.getIndex();
        Integer theirDefaultIndex = o.getIndex();

        if ( myDefaultIndex == null && theirDefaultIndex == null )
        {
            return 0;
        }
        else if ( myDefaultIndex == null && theirDefaultIndex != null )
        {
            return 1;
        }
        else if ( myDefaultIndex != null && theirDefaultIndex == null )
        {
            return -1;
        }
        else
        {
            return myDefaultIndex - theirDefaultIndex;
        }
    }
}
