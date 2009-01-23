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

    void validate( File sourceFile, InputStream bundleDescriptor )
        throws PublishingException;

}
