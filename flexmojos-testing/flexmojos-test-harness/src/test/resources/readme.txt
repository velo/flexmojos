The configurations under this folders are for integration tests only.  Shouldn't be used on production.

mvn org.sonatype.plugins:bundle-publisher-maven-plugin:1.2-SNAPSHOT:deploy-bundle -Dbundle=C:\flex\flex_sdk_4.0.0.12575\flex_sdk_4.0.0.12575.zip -Ddescriptor=compiler-descriptor.xml -Durl=http://repository.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=rso
mvn org.sonatype.plugins:bundle-publisher-maven-plugin:1.2-SNAPSHOT:deploy-bundle -Dbundle=C:\flex\flex_sdk_4.0.0.12575\flex_sdk_4.0.0.12575.zip -Ddescriptor=framework-descriptor.xml -Durl=http://repository.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=rso
