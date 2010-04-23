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
 === Adding Sonatype public repository to settings.xml === 

 
1- Add the following profile to profiles section of settings.xml:
  <profiles>
  ...
	<profile>
	  <id>sonatype</id>
	  <repositories>
	    <repository>
	      <id>forge</id>
	      <url>http://repository.sonatype.org/content/groups/flexgroup</url>
	      <snapshots> <enabled>true</enabled> </snapshots>
	      <releases> <enabled>true</enabled> </releases>
	    </repository>
	  </repositories>
	  <pluginRepositories>
	    <pluginRepository>
	      <id>forge</id>
	      <url>http://repository.sonatype.org/content/groups/flexgroup</url>
	      <snapshots> <enabled>true</enabled> </snapshots>
	      <releases> <enabled>true</enabled> </releases>
	    </pluginRepository>
	  </pluginRepositories>
	</profile>
  </profiles>


2- Add profile activation
    <activeProfiles>
    ...
        <activeProfile>sonatype</activeProfile>
    </activeProfiles>


3- run mvn clean install on todolist project.



NOTE: settings.xml is can be located at:
  1. User Level. This settings.xml file provides configuration for a single user, 
                 and is normally provided in $HOME/.m2/settings.xml.
  2. Global Level. This settings.xml file provides configuration for all maven
                 users on a machine (assuming they're all using the same maven
                 installation). It's normally provided in 
                 ${maven.home}/conf/settings.xml.
