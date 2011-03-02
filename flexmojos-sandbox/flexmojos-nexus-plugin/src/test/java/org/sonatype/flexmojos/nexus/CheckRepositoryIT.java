package org.sonatype.flexmojos.nexus;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.testng.Assert.assertNotNull;

import org.hamcrest.Matcher;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.testng.annotations.Test;

public class CheckRepositoryIT
    extends AbstractNexusProxyIntegrationTest
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void download()
        throws Exception
    {
        RepositoryMessageUtil repoUtil =
            new RepositoryMessageUtil( this, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML );
        GroupMessageUtil groupUtil =
            new GroupMessageUtil( this, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML );

        RepositoryBaseResource flexmojos = repoUtil.getRepository( "flexmojos" );
        assertNotNull( flexmojos );

        RepositoryGroupResource publicGroup = groupUtil.getGroup( "public" );
        assertNotNull( publicGroup );
        Matcher<?> matcher =
            hasItem( having( on( RepositoryGroupMemberRepository.class ).getId(), equalTo( flexmojos.getId() ) ) );
        assertThat( publicGroup.getRepositories(), (Matcher<Iterable<RepositoryGroupMemberRepository>>) matcher );
    }
}
