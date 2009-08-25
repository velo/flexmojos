====
    Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
    Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
