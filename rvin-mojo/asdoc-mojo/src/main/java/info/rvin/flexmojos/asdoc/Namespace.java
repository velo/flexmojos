package info.rvin.flexmojos.asdoc;

import java.io.File;

public class Namespace {

	private String uri;

	private File manifest;

	public Namespace() {
		super();
	}

	public Namespace(String uri, File manifest) {
		super();
		this.uri = uri;
		this.manifest = manifest;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public File getManifest() {
		return manifest;
	}

	public void setManifest(File manifest) {
		this.manifest = manifest;
	}
}
