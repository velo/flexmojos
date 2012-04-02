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
package net.flexmojos.oss.plugin;

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
import net.flexmojos.oss.matcher.artifact.DependencyMatcher;

@Component( role = AbstractMavenLifecycleParticipant.class, hint = "DependencyOrder" )
public class DependencyOrderMavenExtension
    extends AbstractMavenLifecycleParticipant
{

    private static final String FLEXMOJOS = "net.flexmojos.oss:flexmojos-maven-plugin";

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
        threadtoolkit.setGroupId( "net.flexmojos.oss" );
        threadtoolkit.setArtifactId( "flexmojos-threadlocaltoolkit-wrapper" );
        threadtoolkit.setVersion( fmVersion );
        ad.add( threadtoolkit );

        if ( compiler.getVersion().startsWith( "2" ) )
        {
            Dependency compatibility = new Dependency();
            compatibility.setGroupId( "net.flexmojos.oss" );
            compatibility.setArtifactId( "flexmojos-flex2-compatibility-layer" );
            compatibility.setVersion( fmVersion );
            ad.add( compatibility );
        }

        if ( compiler.getVersion().startsWith( "3" ) )
        {
            Dependency compatibility = new Dependency();
            compatibility.setGroupId( "net.flexmojos.oss" );
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
