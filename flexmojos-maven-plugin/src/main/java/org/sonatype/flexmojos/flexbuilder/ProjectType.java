package org.sonatype.flexmojos.flexbuilder;

import org.sonatype.flexmojos.common.FlexExtension;

public enum ProjectType
{
    FLEX, FLEX_LIBRARY, ACTIONSCRIPT, AIR, AIR_LIBRARY;

    public static ProjectType getProjectType( String packaging, boolean useApoloConfig, boolean actionScript )
    {
        if ( FlexExtension.SWF.equals( packaging ) && actionScript )
        {
            return ACTIONSCRIPT;
        }
        else if ( FlexExtension.AIR.equals( packaging ) )
        {
            return AIR;
        }
        else if ( FlexExtension.SWF.equals( packaging ) && !actionScript )
        {
            return FLEX;
        }
        else if ( FlexExtension.SWC.equals( packaging ) && !useApoloConfig )
        {
            return FLEX_LIBRARY;
        }
        else if ( FlexExtension.SWC.equals( packaging ) && useApoloConfig )
        {
            return AIR_LIBRARY;
        }
        else
        {
            return FLEX_LIBRARY;
        }
    }
}
