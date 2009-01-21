package org.sonatype.flexmojos.sandbox.bundlepublisher;

import java.io.File;
import java.io.InputStream;

import org.apache.maven.artifact.repository.ArtifactRepository;

public interface BundlePublisher
{

    String ROLE = BundlePublisher.class.getName();

    void install( File sourceFile, InputStream bundleDescriptor, ArtifactRepository localRepository )
        throws PublishingException;

    void deploy( File sourceFile, InputStream bundleDescriptor, ArtifactRepository deploymentRepository,
                 ArtifactRepository localRepository )
        throws PublishingException;

}
