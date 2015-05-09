package net.flexmojos.oss.plugin.packager;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This goal packages swf and swc artifacts. Usually this is not needed, but
 * in case of FlexJS compilation some special packaging has to be performed.
 *
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @author edward.yakop@gmail.com
 * @goal package
 * @phase package
 * @since 7.1
 */
public class PackagerMojo extends AbstractMojo
{
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Name of the Flex Tool Group that should be used for the build.
     *
     * @parameter expression="${flex.compilerName}"
     */
    protected String compilerName;

    /**
     * Generates a movie that is suitable for debugging
     * <p>
     * Equivalent to -compiler.debug
     * </p>
     *
     * @parameter expression="${flex.debug}"
     */
    private Boolean debug;

    /**
     * @component
     * @readonly
     */
    private ArtifactHandlerManager handlerManager;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if("FlexJS".equals(compilerName)) {
            Artifact artifact = project.getArtifact();
            File flexjsOutputDirectory = artifact.getFile();
            if((flexjsOutputDirectory == null) || !flexjsOutputDirectory.exists() ||
                    !flexjsOutputDirectory.isDirectory()) {
                throw new MojoExecutionException("FlexJS compiler didn't create an output directory.");
            }

            final File targetFile = new File(
                    flexjsOutputDirectory.getParent(), flexjsOutputDirectory.getName() + ".zip");
            try {
                File sourceDir = new File(flexjsOutputDirectory,
                        debug ? "bin/js-debug" : "bin/js-release");
                JarOutputStream jar = new JarOutputStream(new FileOutputStream(targetFile));
                addFileToZip(jar, sourceDir, sourceDir);
                jar.close();

                ArtifactHandler handler = handlerManager.getArtifactHandler("zip");
                artifact = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(),
                        artifact.getScope(), "zip", artifact.getClassifier(), handler);
                artifact.setFile(targetFile);
                project.setArtifact(artifact);
            } catch (IOException e) {
                throw new MojoExecutionException("Error zipping up result", e);
            }
        }
    }

    private void addFileToZip(ZipOutputStream zipOutputStream, File inputFile, File rootDirectory) {
        if (inputFile == null) {
            return;
        }

        // If this is a directory, add all it's children.
        if (inputFile.isDirectory()) {
            final File directoryContent[] = inputFile.listFiles();
            if (directoryContent != null) {
                for (final File file : directoryContent) {
                    addFileToZip(zipOutputStream, file, rootDirectory);
                }
            }
        }
        // If this is a file, add it to the zips output.
        else {
            byte[] buf = new byte[1024];
            try {
                final FileInputStream in = new FileInputStream(inputFile);
                final String zipPath = inputFile.getAbsolutePath().substring(
                        rootDirectory.getAbsolutePath().length() + 1).replace("\\", "/");
                zipOutputStream.putNextEntry(new ZipEntry(zipPath));
                int len;
                while ((len = in.read(buf)) > 0) {
                    zipOutputStream.write(buf, 0, len);
                }
                zipOutputStream.closeEntry();
                in.close();
            } catch(IOException e) {
                throw new RuntimeException("Error adding files to zip.", e);
            }
        }
    }

}
