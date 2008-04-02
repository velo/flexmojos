package info.rvin.mojo.flexmojo.compiler;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal compile-aswc
 * @requiresDependencyResolution
 * @phase compile
 */
public class AirLibraryMojo extends LibraryMojo {

	@Override
	protected String getConfigFileName() {
		return "air-config.xml";
	}

}
