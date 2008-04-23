package info.rvin.flexmojos.encrypter;

import info.rvin.flexmojos.encrypter.encryptations.AesEncrypter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * Goal which encrypt swf files.
 * 
 * @goal encrypt-swf
 * @phase package
 */
public class EncrypterMojo extends AbstractMojo {

	/**
	 * The maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * @component
	 */
	protected MavenProjectHelper projectHelper;

	/**
	 * @parameter expression="${encrypt.key}"
	 */
	private String key;

	/**
	 * Initialization vector
	 * 
	 * @parameter expression="${encrypt.iv}"
	 */
	private String iv;

	@SuppressWarnings("unchecked")
	private List<Artifact> getGeneratedSwf() {
		List<Artifact> artifacts = new ArrayList<Artifact>();
		List<Artifact> attachedArtifacts = project.getAttachedArtifacts();
		for (Artifact artifact : attachedArtifacts) {
			if ("swf".equals(artifact.getType())) {
				artifacts.add(artifact);
			}
		}
		if ("swf".equals(project.getArtifact().getType())) {
			artifacts.add(project.getArtifact());
		}
		return artifacts;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		List<Artifact> artifacts = getGeneratedSwf();

		for (Artifact artifact : artifacts) {
			getLog().info("Encrypting artifact " + artifact);
			File enc = getDestinationFile(project.getBuild(), artifact);
			getLog().debug("Encrypted file: " + enc);
			encrypt(artifact, enc);
			projectHelper.attachArtifact(project, "eswf", artifact
					.getClassifier(), enc);
		}
	}

	private File getDestinationFile(Build build, Artifact artifact) {
		String fileName = project.getBuild().getFinalName();
		if (artifact.getClassifier() != null) {
			fileName += "-" + artifact.getClassifier();
		}
		fileName += ".eswf";

		return new File(project.getBuild().getDirectory(), fileName);
	}

	private void encrypt(Artifact artifact, File enc)
			throws MojoExecutionException {
		try {
			AesEncrypter.encrypt(key, iv, artifact.getFile(), enc);
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to encrypt artifact "
					+ artifact, e);
		}
	}

}
