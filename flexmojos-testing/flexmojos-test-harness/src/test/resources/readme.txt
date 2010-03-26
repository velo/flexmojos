The configurations under this folders are for integration tests only.  Shouldn't be used on production.

mvn org.sonatype.plugins:bundle-publisher-maven-plugin:1.2-SNAPSHOT:deploy-bundle -Dbundle=D:\downloads\flex_sdk_4.0.0.14159.zip -Ddescriptor=compiler-descriptor.xml -Durl=http://repository.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=rso
mvn org.sonatype.plugins:bundle-publisher-maven-plugin:1.2-SNAPSHOT:deploy-bundle -Dbundle=D:\downloads\flex_sdk_4.0.0.14159.zip -Ddescriptor=framework-descriptor.xml -Durl=http://repository.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=rso
