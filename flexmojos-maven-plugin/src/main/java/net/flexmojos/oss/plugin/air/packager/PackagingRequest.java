package net.flexmojos.oss.plugin.air.packager;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.*;

/**
 * Created by christoferdutz on 16.07.15.
 */
public class PackagingRequest {

    protected Log log;

    protected Map<String, Artifact> artifacts;

    protected String targetPlatform;

    protected boolean includeCaptiveRuntime;

    protected String iosPackagingType;

    protected File iosProvisioningProfile;

    protected File iosPlatformSdk;

    protected Resolver resolver;

    protected File storefile;

    protected String storetype;

    protected String storepass;

    protected File inputFile;

    protected File outputFile;

    protected File descriptorFile;

    protected File workDir;

    protected File buildDir;

    protected String finalName;

    protected String classifier;

    protected Map<String, List<String>> includedFiles;

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public Collection<Artifact> getArtifacts() {
        return artifacts.values();
    }

    public Artifact getArtifact(String artifactId) {
        if(artifacts != null) {
            return artifacts.get(artifactId);
        }
        return null;
    }

    public void setArtifacts(Collection<Artifact> artifacts) {
        this.artifacts = new HashMap<String, Artifact>(artifacts.size());
        for(Artifact artifact : artifacts) {
            this.artifacts.put(artifact.getArtifactId(), artifact);
        }
    }

    public void setArtifacts(Map<String, Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    public String getTargetPlatform() {
        return targetPlatform;
    }

    public void setTargetPlatform(String targetPlatform) {
        this.targetPlatform = targetPlatform;
    }

    public boolean isIncludeCaptiveRuntime() {
        return includeCaptiveRuntime;
    }

    public void setIncludeCaptiveRuntime(boolean includeCaptiveRuntime) {
        this.includeCaptiveRuntime = includeCaptiveRuntime;
    }

    public String getIosPackagingType() {
        return iosPackagingType;
    }

    public void setIosPackagingType(String iosPackagingType) {
        this.iosPackagingType = iosPackagingType;
    }

    public File getIosProvisioningProfile() {
        return iosProvisioningProfile;
    }

    public void setIosProvisioningProfile(File iosProvisioningProfile) {
        this.iosProvisioningProfile = iosProvisioningProfile;
    }

    public File getIosPlatformSdk() {
        return iosPlatformSdk;
    }

    public void setIosPlatformSdk(File iosPlatformSdk) {
        this.iosPlatformSdk = iosPlatformSdk;
    }

    public Resolver getResolver() {
        return resolver;
    }

    public void setResolver(Resolver resolver) {
        this.resolver = resolver;
    }

    public File getStorefile() {
        return storefile;
    }

    public void setStorefile(File storefile) {
        this.storefile = storefile;
    }

    public String getStoretype() {
        return storetype;
    }

    public void setStoretype(String storetype) {
        this.storetype = storetype;
    }

    public String getStorepass() {
        return storepass;
    }

    public void setStorepass(String storepass) {
        this.storepass = storepass;
    }

    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public File getDescriptorFile() {
        return descriptorFile;
    }

    public void setDescriptorFile(File descriptorFile) {
        this.descriptorFile = descriptorFile;
    }

    public File getWorkDir() {
        return workDir;
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    public File getBuildDir() {
        return buildDir;
    }

    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
    }

    public String getFinalName() {
        return finalName;
    }

    public void setFinalName(String finalName) {
        this.finalName = finalName;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public void addIncludedFile(String path, String filename) {
        if(includedFiles == null) {
            includedFiles = new HashMap<String, List<String>>();
        }
        if(!includedFiles.containsKey(path)) {
            includedFiles.put(path, new ArrayList<String>());
        }
        includedFiles.get(path).add(filename);
    }

    public Map<String, List<String>> getIncludedFiles() {
        return includedFiles;
    }
}
