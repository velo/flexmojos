Run the maven build with "mvn clean install --fail-at-end" or the
build would stop at the first module failure.

In order to run the testsuite, you need a mavenized 4.13 FDK and access
to the Apache and the Sonatype SNAPSHOT repositories:

For Falcon 0.0.3-SNAPSHOT:
http://repository.apache.org/snapshots/

For Flexmojos 7.1.0-SNAPSHOT:
https://oss.sonatype.org/content/repositories/snapshots/