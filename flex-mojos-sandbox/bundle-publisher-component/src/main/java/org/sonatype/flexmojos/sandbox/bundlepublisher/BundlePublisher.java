package org.sonatype.flexmojos.sandbox.bundlepublisher;

import java.io.File;

import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;

public interface BundlePublisher
{

    String ROLE = BundlePublisher.class.getName();

    void publish( File sourceFile, File bundleDescriptor, Repository repository )
        throws PublishException, RepositoryException;

}
