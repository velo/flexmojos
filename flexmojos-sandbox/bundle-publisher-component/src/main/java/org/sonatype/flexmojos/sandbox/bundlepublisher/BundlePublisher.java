package org.sonatype.flexmojos.sandbox.bundlepublisher;

import java.io.File;
import java.io.InputStream;

import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;

public interface BundlePublisher
{

    String ROLE = BundlePublisher.class.getName();

    void publish( File sourceFile, InputStream bundleDescriptor, Repository repository )
        throws PublishingException, RepositoryException;

}
