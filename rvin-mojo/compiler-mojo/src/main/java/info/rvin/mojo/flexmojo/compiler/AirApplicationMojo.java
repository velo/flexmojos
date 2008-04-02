package info.rvin.mojo.flexmojo.compiler;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal compile-aswf
 * @requiresDependencyResolution
 * @phase compile
 */
public class AirApplicationMojo extends ApplicationMojo {

	@Override
	protected String getConfigFileName() {
		return "air-config.xml";
	}

}
