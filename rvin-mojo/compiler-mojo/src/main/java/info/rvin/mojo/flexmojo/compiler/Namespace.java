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
package info.rvin.mojo.flexmojo.compiler;

import java.io.File;

public class Namespace
{

    private File manifest;

    private String uri;

    public Namespace()
    {
        super();
    }

    public Namespace( String uri, File manifest )
    {
        super();
        this.uri = uri;
        this.manifest = manifest;
    }

    public File getManifest()
    {
        return manifest;
    }

    public String getUri()
    {
        return uri;
    }

    public void setManifest( File manifest )
    {
        this.manifest = manifest;
    }

    public void setUri( String uri )
    {
        this.uri = uri;
    }
}
