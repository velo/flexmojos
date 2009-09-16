package org.sonatype.flexmojos.flexbuilder;

import static org.sonatype.flexmojos.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.common.FlexExtension.SWF;

import java.util.Date;
import java.util.List;

import org.apache.maven.plugin.eclipse.EclipseConfigFile;

/**
 * Generates AXDT configuration files for SWC and SWF projects.
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 3.4
 * @extendsPlugin eclipse
 * @extendsGoal eclipse
 * @goal axdt
 * @requiresDependencyResolution compile
 */
public class AxdtMojo
    extends AbstractIdeMojo

{
    protected static final String AXDT_NATURE = "org.axdt.as3.imp.nature";

    protected static final String AXDT_BUILD_COMMAND = "org.axdt.as3.imp.builder";

    @SuppressWarnings( "unchecked" )
    @Override
    protected void fillDefaultNatures( String packaging )
    {
        super.fillDefaultNatures( packaging );

        if ( SWF.equals( packaging ) || SWC.equals( packaging ) )
        {
            getProjectnatures().add( AXDT_NATURE );
        }
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected void fillDefaultBuilders( String packaging )
    {
        super.fillDefaultBuilders( packaging );

        if ( SWF.equals( packaging ) || SWC.equals( packaging ) )
        {
            getBuildcommands().add( AXDT_BUILD_COMMAND );
        }
    }

    @Override
    protected List<EclipseConfigFile> getExtraConfigs()
    {
        EclipseConfigFile axdtConfig = new EclipseConfigFile();
        axdtConfig.setName( ".settings/org.axdt.as3.prefs" );
        axdtConfig.setContent( getAxdtContent() );

        List<EclipseConfigFile> extraConfigs = super.getExtraConfigs();
        extraConfigs.add( axdtConfig );
        return extraConfigs;
    }

    private String getAxdtContent()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( '#' ).append( new Date().toString() ).append( '\n' );
        sb.append( "CONFIG_PATH=config" ).append( '\n' );
        sb.append( "DEPLOY_PATH=bin" ).append( '\n' );
        sb.append( "LIBRARY_PATHS=libs" ).append( '\n' );
        sb.append( "SOURCE_PATHS=" + plain( getRelativeSources() ) ).append( '\n' );
        return sb.toString();
    }

}
