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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;

public class RuntimeMavenResolutionException
    extends RuntimeException
{

    private static final long serialVersionUID = -4816015188431985147L;

    private Artifact artifact;

    private ArtifactResolutionResult resolutionResult;

    public RuntimeMavenResolutionException( String msg, ArtifactResolutionResult res, Artifact artifact )
    {
        super( msg );
        this.resolutionResult = res;
        this.artifact = artifact;
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public ArtifactResolutionResult getResolutionResult()
    {
        return resolutionResult;
    }

}
