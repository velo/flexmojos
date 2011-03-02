package org.sonatype.flexmojos.nexus;

import static java.util.Arrays.asList;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.ConfigurationIdGenerator;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.mapping.RepositoryPathMapping;
import org.sonatype.nexus.proxy.mapping.RepositoryPathMapping.MappingType;
import org.sonatype.nexus.proxy.mapping.RequestRepositoryMapper;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.TemplateProvider;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;
import org.sonatype.plexus.appevents.Event;

@Component( role = EventInspector.class, hint = "FlexmojosStartedEventInspector" )
public class FlexmojosStartedEventInspector
    extends AbstractEventInspector
    implements EventInspector
{

    @Requirement
    private RequestRepositoryMapper repositoryMapper;

    @Requirement( role = RepositoryRegistry.class )
    private RepositoryRegistry repositoryRegistry;

    @Requirement( role = TemplateProvider.class )
    private TemplateProvider templateProvider;

    @Requirement
    private NexusConfiguration nexusConfiguration;

    @Requirement
    private ConfigurationIdGenerator idGenerator;

    public boolean accepts( Event<?> evt )
    {
        return evt instanceof NexusStartedEvent;
    }

    public void inspect( Event<?> evt )
    {
        try
        {
            repositoryRegistry.getRepository( "flexmojos" );
        }
        catch ( NoSuchRepositoryException e )
        {
            // do not exists, creating it!
            Repository repo = createFlexmojosRepository();
            if ( repo != null )
            {
                GroupRepository group = setupPublicGroup( repo );
                if ( group != null )
                {
                    setupRouting( group, repo );
                }
            }

            try
            {
                nexusConfiguration.saveConfiguration();
            }
            catch ( IOException ex )
            {
                getLogger().error( "Unable to save flexmojos repository setup", ex );
            }

        }
    }

    private GroupRepository setupPublicGroup( Repository repo )
    {
        GroupRepository publicGroup;
        try
        {
            publicGroup = repositoryRegistry.getRepositoryWithFacet( "public", GroupRepository.class );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().error( "Public group not found", e );
            return null;
        }
        try
        {
            publicGroup.addMemberRepositoryId( repo.getId() );
            return publicGroup;
        }
        catch ( Exception e )
        {
            getLogger().error( "Unable to setup flexmojos repository properly", e );
            return null;
        }

    }

    private void setupRouting( GroupRepository group, Repository repo )
    {
        try
        {
            // to best performance go to flexmojos repo to get adobe and flexmojos artifacts
            repositoryMapper.addMapping( new RepositoryPathMapping( idGenerator.generateId(), MappingType.INCLUSION,
                                                                    group.getId(),
                                                                    asList( ".*/org/sonatype/flexmojos/.*" ),
                                                                    asList( repo.getId() ) ) );
            repositoryMapper.addMapping( new RepositoryPathMapping( idGenerator.generateId(), MappingType.INCLUSION,
                                                                    group.getId(), asList( ".*/com/adobe/.*" ),
                                                                    asList( repo.getId() ) ) );
            // flexmojos repo do not publish sources artifacts
            repositoryMapper.addMapping( new RepositoryPathMapping( idGenerator.generateId(), MappingType.BLOCKING,
                                                                    group.getId(),
                                                                    asList( ".*/com/adobe/.*-sources.jar" ), null ) );
        }
        catch ( ConfigurationException e )
        {
            getLogger().error( "Unable to setup flexmojos routing properly", e );
        }
    }

    private Repository createFlexmojosRepository()
    {
        getLogger().info( "Default Flexmojos repository is missing, creating it." );
        try
        {
            RepositoryTemplate template =
                (RepositoryTemplate) templateProvider.getTemplateById( "default_proxy_release" );

            template.getConfigurableRepository().setId( "flexmojos" );
            template.getConfigurableRepository().setName( "Flexmojos Repository" );

            Repository repo = template.create();
            repositoryRegistry.addRepository( repo );

            return repo;
        }
        catch ( Exception e )
        {
            getLogger().error( "Unable to setup flexmojos repository properly", e );
            return null;
        }
    }

}
