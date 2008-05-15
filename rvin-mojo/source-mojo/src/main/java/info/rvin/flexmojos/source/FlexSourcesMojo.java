package info.rvin.flexmojos.source;

import org.apache.maven.plugin.source.SourceJarMojo;

/**
 * Goal to create a JAR-package containing all the source files of a Flex
 * project.
 * 
 * @extendsPlugin source
 * @extendsGoal jar
 * 
 * @goal jar
 * @phase package
 */
public class FlexSourcesMojo extends SourceJarMojo {

}
