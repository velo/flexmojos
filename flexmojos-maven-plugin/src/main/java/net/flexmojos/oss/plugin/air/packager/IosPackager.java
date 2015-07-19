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

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.component.annotations.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component( role = Packager.class, hint = "ios" )
public class IosPackager extends BasePackager {

    @Override
    public boolean prepare() throws PackagingException {
        File workDir = request.getWorkDir();
        boolean init = !new File(workDir, "lib/aot/bin").exists();
        if(init) {
            super.prepare();
            Artifact adt = request.getArtifact("adt");
            Artifact iosResources = request.getResolver().resolve(adt.getGroupId(), adt.getArtifactId(),
                    adt.getVersion(), "ios", "zip");
            if(iosResources != null) {
                unpackArtifactToLocation(iosResources, workDir);
                // Some files need to manually be made executable.
                makeExecutableIfExists(new File(workDir, "bin/adt"));
                makeExecutableIfExists(new File(workDir, "bin/adt.bat"));
                makeExecutableIfExists(new File(workDir, "bin/adl"));
                makeExecutableIfExists(new File(workDir, "bin/adl.exe"));
                makeExecutableIfExists(new File(workDir, "lib/aot/bin/compile-abc/compile-abc"));
                makeExecutableIfExists(new File(workDir, "lib/aot/bin/compile-abc/compile-abc-64"));
                makeExecutableIfExists(new File(workDir, "lib/aot/bin/ld64/ld64"));
            }
        }
        return init;
    }

    @Override
    public File execute() throws PackagingException {
        File outputFile = new File(request.getBuildDir(), request.getFinalName() +
                ((request.getClassifier() != null) ? "-" + request.getClassifier() : "")  +".ipa");
        request.setOutputFile(outputFile);

        List<String> adtArgs = new ArrayList<String>();
        adtArgs.add("-package");
        /*if(request.getIosPlatformSdk() != null) {
            adtArgs.add("-Xruntime");
            adtArgs.add(request.getIosPlatformSdk().getAbsolutePath());
        }*/
        adtArgs.add("-target");
        adtArgs.add(request.getIosPackagingType());
        adtArgs.add("-provisioning-profile");
        adtArgs.add(request.getIosProvisioningProfile().getAbsolutePath());
        adtArgs.add("-storetype");
        adtArgs.add(request.getStoretype());
        adtArgs.add("-keystore");
        adtArgs.add(request.getStorefile().getAbsolutePath());
        adtArgs.add("-storepass");
        adtArgs.add(request.getStorepass());
        adtArgs.add("-keypass");
        adtArgs.add(request.getStorepass());
        adtArgs.add(request.getOutputFile().getAbsolutePath());
        adtArgs.add(request.getDescriptorFile().getAbsolutePath());
        adtArgs.add(request.getInputFile().getName());
        if(request.getIosPlatformSdk() != null) {
            adtArgs.add("-platformsdk");
            adtArgs.add(request.getIosPlatformSdk().getAbsolutePath());
        }

        runAdt(adtArgs);

        if(!outputFile.exists()) {
            throw new PackagingException("Output file does not exist " + outputFile.getAbsolutePath());
        }

        return outputFile;
    }

}
