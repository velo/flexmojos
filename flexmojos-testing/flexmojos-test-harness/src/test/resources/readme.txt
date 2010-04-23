====
      Copyright 2008 Marvin Herman Froeder
    -->
    <!--
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    -->

    <!--
        http://www.apache.org/licenses/LICENSE-2.0
    -->

    <!--
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
====
The configurations under this folders are for integration tests only.  Shouldn't be used on production.

mvn org.sonatype.plugins:bundle-publisher-maven-plugin:1.2-SNAPSHOT:deploy-bundle -Dbundle=C:\flex\flex_sdk_4.0.0.13875\flex_sdk_4.0.0.13875.zip -Ddescriptor=compiler-descriptor.xml -Durl=http://repository.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=rso
mvn org.sonatype.plugins:bundle-publisher-maven-plugin:1.2-SNAPSHOT:deploy-bundle -Dbundle=C:\flex\flex_sdk_4.0.0.13875\flex_sdk_4.0.0.13875.zip -Ddescriptor=framework-descriptor.xml -Durl=http://repository.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=rso
