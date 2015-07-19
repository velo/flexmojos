/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.flexmojos.oss.plugin.air.packager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public abstract class BasePackager implements Packager {

    protected PackagingRequest request;

    public void setRequest(PackagingRequest request) {
        this.request = request;
    }

    protected List<String> getAdtCommand() {
        File binDir = new File(request.getWorkDir(), "bin");
        // Get the adt command.
        List<String> command = new ArrayList<String>();
        if (System.getProperty("os.name").startsWith("Win")) {
            command.add(binDir.getAbsolutePath() + File.separator + "adt.bat");
        } else {
            command.add(binDir.getAbsolutePath() + File.separator + "adt");
        }
        return command;
    }

    @Override
    public void validateConfiguration() throws PackagingException {
        if(StringUtils.isEmpty(request.getStoretype())) {
            throw new PackagingException("The 'storetype' parameter is required.");
        }
        if(StringUtils.isEmpty(request.getStorepass())) {
            throw new PackagingException("The 'storepass' parameter is required.");
        }
        if(request.getStorefile() == null) {
            throw new PackagingException("The 'storefile' parameter is required.");
        }
        if(!request.getStorefile().exists()) {
            throw new PackagingException("The 'storefile' parameter has to reference an existing file.");
        }
    }

    @Override
    public boolean prepare() throws PackagingException {
        File workDir = request.getWorkDir();
        if (!workDir.exists()) {
            copyArtifactToLocation(request.getArtifact("adt"), new File(workDir, "lib/adt.jar"));
            return true;
        }
        return false;
    }

    protected void runAdt(List<String> adtArgs) throws PackagingException {
        List<String> commandArgs = new ArrayList<String>();
        commandArgs.addAll(getAdtCommand());
        if (adtArgs != null) {
            commandArgs.addAll(adtArgs);
        }
        // Attach the resources.
        if((request.getIncludedFiles() != null) && !request.getIncludedFiles().isEmpty()) {
            for(Map.Entry<String, List<String>> entries : request.getIncludedFiles().entrySet()) {
                String path = entries.getKey();
                commandArgs.add("-C");
                commandArgs.add(path);
                for(String file : entries.getValue()) {
                    commandArgs.add(file);
                }
            }
        }

        if(request.getLog().isDebugEnabled()) {
            request.getLog().debug("Executing command: " + StringUtils.join(commandArgs, " "));
        }

        ProcessBuilder builder = new ProcessBuilder(commandArgs.toArray(new String[commandArgs.size()]));

        try {
            builder.directory(request.getInputFile().getParentFile());
            Process adtProcess = builder.start();
            IOUtils.copy(adtProcess.getErrorStream(), System.err);
            IOUtils.copy(adtProcess.getInputStream(), System.out);
            int errorCode = adtProcess.waitFor();
            if (errorCode != 0) {
                String msg;
                switch (errorCode) {
                    case 2:
                        msg = "Usage error (incorrect arguments)";
                        break;
                    case 5:
                        msg = "Unknown error";
                        break;
                    case 6:
                        msg = "Could not write to output directory";
                        break;
                    case 7:
                        msg = "Could not access certificate";
                        break;
                    case 8:
                        msg = "Invalid certificate";
                        break;
                    case 9:
                        msg = "Could not sign AIR file";
                        break;
                    case 10:
                        msg = "Could not create timestamp";
                        break;
                    case 11:
                        msg = "Certificate creation error";
                        break;
                    case 12:
                        msg = "Invalid input";
                        break;
                    default:
                        msg = "- unknown return code -";
                        break;
                }
                throw new PackagingException("Got return code " + errorCode + " from adt: " + msg);
            }
        } catch (InterruptedException e) {
            throw new PackagingException("Error running adt command.", e);
        } catch (IOException e) {
            throw new PackagingException("Error running adt command.", e);
        }
    }

    protected void copyArtifactToLocation(Artifact artifact, File target) throws PackagingException {
        File targetDir = new File(target.getParent());
        if (!targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                throw new PackagingException("Could not create target directory at " + targetDir.getAbsolutePath());
            }
        }
        if ((artifact.getFile() == null) || !artifact.getFile().exists()) {
            throw new PackagingException("Artifact file of the '" + artifact.getArtifactId() +
                    "' artifact is not available.");
        }
        try {
            FileUtils.copyFile(artifact.getFile(), target);
        } catch (IOException e) {
            throw new PackagingException("Could not copy artifact '" + artifact.getArtifactId() +
                    "' to its destination at " + target.getAbsolutePath(), e);
        }
    }

    protected void unpackArtifactToLocation(Artifact artifact, File target) throws PackagingException {
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(artifact.getFile()));
            ZipEntry ze = zis.getNextEntry();

            byte[] buffer = new byte[1024];

            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(target, fileName);
                if(!newFile.getParentFile().exists()) {
                    if (!newFile.getParentFile().mkdirs()) {
                        throw new PackagingException("Error unpacking zip. Could not create directory " +
                                newFile.getParentFile().getAbsolutePath());
                    }
                }
                if(!newFile.exists()) {
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (FileNotFoundException e) {
            throw new PackagingException("Error unpacking zip", e);
        } catch (IOException e) {
            throw new PackagingException("Error unpacking zip", e);
        }
    }

    protected void makeExecutableIfExists(File file) throws PackagingException {
        if(file.exists()) {
            if(!file.setExecutable(true)) {
                throw new PackagingException("Could not make file executable " + file.getAbsolutePath());
            }
        }
    }

    protected void makeSymlink(File source, File target) throws PackagingException {
        try {
            Files.createSymbolicLink(source.toPath(), target.toPath());
        } catch (IOException e) {
            throw new PackagingException("Could not create symlink from " + source.getAbsolutePath() +
                    " to " + target.getAbsolutePath(), e);
        }
    }

}
