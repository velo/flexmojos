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
package net.flexmojos.oss.plugin.air.packager;

import org.apache.maven.artifact.Artifact;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class DesktopPackager extends BasePackager {

    protected abstract File getOutputFile();

    @Override
    public boolean prepare() throws PackagingException {
        File workDir = request.getWorkDir();
        boolean init = !new File(workDir, "lib/nai/bin/naip").exists();
        if(init) {
            super.prepare();
            Artifact adt = request.getArtifact("adt");
            Artifact desktopResources = request.getResolver().resolve(adt.getGroupId(), adt.getArtifactId(),
                    adt.getVersion(), "desktop", "zip");
            if(desktopResources != null) {
                unpackArtifactToLocation(desktopResources, workDir);
                // Some files need to manually be made executable.
                makeExecutableIfExists(new File(workDir, "bin/adt"));
                makeExecutableIfExists(new File(workDir, "bin/adt.bat"));
                makeExecutableIfExists(new File(workDir, "bin/adl"));
                makeExecutableIfExists(new File(workDir, "bin/adl.exe"));
                makeExecutableIfExists(new File(workDir, "lib/nai/bin/naip"));
                makeExecutableIfExists(new File(workDir, "lib/nai/lib/naib.app/Contents/MacOS/naib"));
                makeExecutableIfExists(new File(workDir, "lib/nai/lib/CaptiveAppEntry"));
                File airMacRuntime = new File(workDir, "runtimes/air/mac/Adobe AIR.framework/Versions/1.0/Adobe AIR");
                makeExecutableIfExists(airMacRuntime);
                makeSymlink(new File(workDir, "runtimes/air/mac/Adobe AIR.framework/Versions/Current"),
                        new File(workDir, "runtimes/air/mac/Adobe AIR.framework/Versions/1.0"));
                makeSymlink(new File(workDir, "runtimes/air/mac/Adobe AIR.framework/Adobe AIR"),
                        new File(workDir, "runtimes/air/mac/Adobe AIR.framework/Versions/Current/Adobe AIR"));
                makeSymlink(new File(workDir, "runtimes/air/mac/Adobe AIR.framework/Headers"),
                        new File(workDir, "runtimes/air/mac/Adobe AIR.framework/Versions/Current/Headers"));
                makeSymlink(new File(workDir, "runtimes/air/mac/Adobe AIR.framework/Resources"),
                        new File(workDir, "runtimes/air/mac/Adobe AIR.framework/Versions/Current/Resources"));

                makeExecutableIfExists(new File(workDir, "lib/nai/bin/naip.exe"));
            }
        }
        return init;
    }

    @Override
    public File execute() throws PackagingException {
        File outputFile = getOutputFile();
        request.setOutputFile(outputFile);

        List<String> adtArgs = new ArrayList<String>();
        adtArgs.add("-package");
        adtArgs.add("-storetype");
        adtArgs.add(request.getStoretype());
        adtArgs.add("-keystore");
        adtArgs.add(request.getStorefile().getAbsolutePath());
        adtArgs.add("-storepass");
        adtArgs.add(request.getStorepass());
        adtArgs.add("-keypass");
        adtArgs.add(request.getStorepass());
        adtArgs.add("-target");
        adtArgs.add(request.isIncludeCaptiveRuntime() ? "bundle" : "native");
        adtArgs.add(request.getOutputFile().getAbsolutePath());
        adtArgs.add(request.getDescriptorFile().getAbsolutePath());
        adtArgs.add(request.getInputFile().getName());
        runAdt(adtArgs);

        if(!outputFile.exists()) {
            throw new PackagingException("Output file does not exist " + outputFile.getAbsolutePath());
        }

        return outputFile;
    }

}
