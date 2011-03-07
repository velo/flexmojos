package org.sonatype.flexmojos.plugin;

import static ch.lambdaj.Lambda.selectFirst;
import static org.hamcrest.CoreMatchers.allOf;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.flexmojos.matcher.artifact.DependencyMatcher;

@Component( role = AbstractMavenLifecycleParticipant.class, hint = "DependencyOrder" )
public class DependencyOrderMavenExtension
    extends AbstractMavenLifecycleParticipant
{

    private static final String FLEXMOJOS = "org.sonatype.flexmojos:flexmojos-maven-plugin";

    @Override
    public void afterSessionStart( MavenSession session )
        throws MavenExecutionException
    {
        if ( session.getRequest().getUserProperties().containsKey( "flexmojos.ignore.dependency.order" ) )
        {
            return;
        }

        List<MavenProject> projects = session.getProjects();

        for ( MavenProject project : projects )
        {
            fixPlugin( project.getPlugin( FLEXMOJOS ) );
            fixPlugin( project.getPluginManagement().getPluginsAsMap().get( FLEXMOJOS ) );
        }
    }

    private void fixPlugin( Plugin fm )
    {
        if ( fm == null )
        {
            return;
        }

        List<Dependency> deps = fm.getDependencies();
        fm.setDependencies( fixDependencies( deps, fm.getVersion() ) );
    }

    private List<Dependency> fixDependencies( List<Dependency> deps, String fmVersion )
    {
        @SuppressWarnings( "unchecked" )
        Dependency compiler = selectFirst( deps, allOf( DependencyMatcher.groupId( "com.adobe.flex" ),//
                                                        DependencyMatcher.artifactId( "compiler" ),//
                                                        DependencyMatcher.type( "pom" ) )//
            );

        if ( compiler == null )
        {
            return deps;
        }

        List<Dependency> ad = new ArrayList<Dependency>();

        Dependency threadtoolkit = new Dependency();
        threadtoolkit.setGroupId( "org.sonatype.flexmojos" );
        threadtoolkit.setArtifactId( "flexmojos-threadlocaltoolkit-wrapper" );
        threadtoolkit.setVersion( fmVersion );
        ad.add( threadtoolkit );

        if ( compiler.getVersion().startsWith( "2" ) )
        {
            Dependency compatibility = new Dependency();
            compatibility.setGroupId( "org.sonatype.flexmojos" );
            compatibility.setArtifactId( "flexmojos-flex2-compatibility-layer" );
            compatibility.setVersion( fmVersion );
            ad.add( compatibility );
        }

        if ( compiler.getVersion().startsWith( "3" ) )
        {
            Dependency compatibility = new Dependency();
            compatibility.setGroupId( "org.sonatype.flexmojos" );
            compatibility.setArtifactId( "flexmojos-flex3-compatibility-layer" );
            compatibility.setVersion( fmVersion );
            ad.add( compatibility );
        }

        ad.addAll( deps );
        
        //make sure compiler is the last one
        ad.remove( compiler );
        ad.add( compiler );

        return ad;
    }

}
